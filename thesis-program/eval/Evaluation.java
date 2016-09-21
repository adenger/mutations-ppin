package eval;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringJoiner;

import framework.ClassifierScore;
import framework.Mutation;
import framework.MutationEvaluator;
import framework.Protein;
import framework.Settings;

public class Evaluation {
	public static void main(String[] args) {
		Settings.setLOCAL_PROTEIN_DATA(true);
		Settings.setLOCAL_MUTATION_DATA(false);
		Settings.setDISABLE_LOG(false);
		Evaluation eval = new Evaluation();
		eval.evaluate();
	}

	private final String networkFile = "/eval/network_wildtype.txt";
	private final String mutationsFile = "/eval/mutation_consequences.txt";
	private Map<String, Set<String>> network = new HashMap<String, Set<String>>();
	private Map<String, Map<String, Boolean>> snpData = new HashMap<String, Map<String, Boolean>>();

	public Evaluation() {
		readProteinNetwork();
		readSnpData();
	}

	public void evaluate() {
		MutationEvaluator eval = new MutationEvaluator(getNetwork(), getSnpData().keySet());
		StringBuilder binaryFileBuilder = new StringBuilder(
				"\\begin{tabular*}{\\linewidth}{@{\\extracolsep{\\fill}}lrrrrllll}\n"
						+ "\\toprule\nClassifier&TP&FP&TN&FN&Prec.&Acc.&Sens.&Spec.\\\\\n\\midrule\n");
		for (ClassifierScore score : ClassifierScore.values()) {
			Settings.setBINDING_SITE_CLASSIFIER(score);
			if (score.isBinaryScore()) {
				this.evaluateBinary(score, eval, binaryFileBuilder);
			} else {
				// this.evaluatePercentage(score, eval);
			}
		}
		this.writeFile(binaryFileBuilder.append("\\bottomrule\n\\end{tabular*}\n").toString(), "binary_scores.tex");
		System.out.println(binaryFileBuilder.toString());
	}

	private void evaluateBinary(ClassifierScore score, MutationEvaluator eval, StringBuilder builder) {
		Map<Mutation, Map<Protein, Boolean>> results = eval.getClassifiedInteractionPartners();
		Integer tp = 0, fp = 0, tn = 0, fn = 0;
		for (Entry<Mutation, Map<Protein, Boolean>> entry : results.entrySet()) {
			Mutation mutation = entry.getKey();
			Map<Protein, Boolean> classified = entry.getValue();
			String mutationID = mutation.getDbSNP();
			Map<String, Boolean> deleted = getSnpData().get(mutationID);
			for (Entry<Protein, Boolean> classifiedEntry : classified.entrySet()) {
				Protein classifiedInteractionPartner = classifiedEntry.getKey();
				Boolean deletedBool = deleted.get(classifiedInteractionPartner.getUniprotID());
				Boolean predictedDeleted = classifiedEntry.getValue();
				if (deletedBool == null)
					continue;
				if (deletedBool && predictedDeleted)
					tp++;
				else if (deletedBool && !predictedDeleted)
					fn++;
				else if (!deletedBool && predictedDeleted)
					fp++;
				else if (!deletedBool && !predictedDeleted)
					tn++;
			}
		}
		Double precision = round((double) tp / (double) (tp + fp));
		Double accuracy = round((double) (tp + tn) / (double) (tp + tn + fn + fp));
		Double sensitivity = round((double) tp / (double) (tp + fn));
		Double specificity = round((double) tn / (double) (tn + fp));
		// double TPR = sensitivity;
		// double FPR = 1.0d-specificity;
		StringJoiner joiner = new StringJoiner("&").add(score.toString().replace('_', ' ')).add(tp.toString())
				.add(fp.toString()).add(tn.toString()).add(fn.toString()).add(precision.toString())
				.add(accuracy.toString()).add(sensitivity.toString()).add(specificity.toString());
		builder.append(joiner.toString() + "\\\\\n");
	}

	// private void evaluatePercentage(ClassifierScore score, MutationEvaluator
	// eval) {
	// StringJoiner percentages = new StringJoiner(",");
	// StringJoiner booleans = new StringJoiner(",");
	// Settings.CUTOFF_PERCENTAGE = 0.0; // TODO ??
	// Map<Mutation, Map<Protein, Boolean>> results =
	// eval.getClassifiedInteractionPartners();
	// for (Entry<Mutation, Map<Protein, Boolean>> entry : results.entrySet()) {
	// Mutation mutation = entry.getKey();
	// Map<Protein, Boolean> classified = entry.getValue();
	// String mutationID = mutation.getDbSNP();
	// Map<String, Boolean> deleted = getSnpData().get(mutationID);
	// for (Entry<Protein, Boolean> classifiedEntry : classified.entrySet()) {
	// Protein classifiedInteractionPartner = classifiedEntry.getKey();
	// Boolean deletedBool =
	// deleted.get(classifiedInteractionPartner.getUniprotID());
	// if (deletedBool == null)
	// continue;
	// percentages.add(Double.toString(mutation.getPolyphenScore())); // TODO
	// booleans.add(deletedBool ? Integer.toString(1) : Integer.toString(0));
	// }
	// }
	//
	// writeFile(percentages.toString() + "\n" + booleans.toString(),
	// score.toString() + ".txt");
	// }

	private Map<String, Set<String>> getNetwork() {
		return network;
	}

	private Map<String, Map<String, Boolean>> getSnpData() {
		return snpData;
	}

	private void readProteinNetwork() {
		InputStream stream = Evaluation.class.getResourceAsStream(networkFile);
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (line.trim().isEmpty())
					continue;
				String[] proteins = line.split("\t");
				if (proteins.length != 2)
					continue;
				String proteinA = proteins[0];
				String proteinB = proteins[1];
				Set<String> mappedProteins = network.get(proteinA);
				if (mappedProteins == null) {
					mappedProteins = new HashSet<String>();
					mappedProteins.add(proteinB);
					network.put(proteinA, mappedProteins);
				} else {
					mappedProteins.add(proteinB);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void readSnpData() {
		InputStream stream = Evaluation.class.getResourceAsStream(mutationsFile);
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (line.trim().isEmpty())
					continue;
				String[] values = line.split("\t");
				String snpID = values[0];
				String interactorID = values[2];
				Boolean connectionStatus = null;
				if (values[3].equals("1")) {
					connectionStatus = true;
				} else if (values[3].equals("0")) {
					connectionStatus = false;
				}
				if (connectionStatus == null)
					continue;
				Map<String, Boolean> table = snpData.get(snpID);
				if (table == null) {
					table = new HashMap<String, Boolean>();
					table.put(interactorID, connectionStatus);
					snpData.put(snpID, table);
				} else {
					table.put(interactorID, connectionStatus);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private double round(double value) {
		return new BigDecimal(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
	}

	private void writeFile(String string, String name) {
		try {
			PrintWriter writer = new PrintWriter(name);
			writer.write(string);
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
