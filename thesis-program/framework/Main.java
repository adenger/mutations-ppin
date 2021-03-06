package framework;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringJoiner;
import java.util.zip.GZIPInputStream;

public class Main {
	/**
	 * See Readme.md for parameters
	 */
	public static void main(String[] args) {
		String mutationFile = "";
		String ppinFile = "";
		String outputFile = "";
		try {
			for (int i = 0; i < args.length; i++) {
				switch (args[i]) {
				case "-m":
					mutationFile = args[i + 1];
					i++;
					break;
				case "-p":
					ppinFile = args[i + 1];
					i++;
					break;
				case "-o":
					outputFile = args[i + 1];
					i++;
					break;
				case "-update_ppin":
					Settings.setLOCAL_PROTEIN_DATA(false);
					break;
				case "-localmutations":
					Settings.setLOCAL_MUTATION_DATA(true);
					break;
				case "-nologfile":
					Settings.setDISABLE_LOG_FILE(true);
					break;
				case "-printlog":
					Settings.setDISABLE_LOG(false);
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error: Invalid arguments");
		}
		new Main(mutationFile, ppinFile, outputFile);
	}

	public Main(String mutationFile, String ppinFile, String outputFile) {
		Set<String> mutationIDs = readMutations(mutationFile);
		Map<String, Set<String>> ppin = readPPIN(ppinFile);
		MutationEvaluator evaluator = new MutationEvaluator(ppin, mutationIDs);
		List<String> scores = new LinkedList<String>();
		Map<Mutation, Map<Protein, List<Boolean>>> outputMap = new HashMap<Mutation, Map<Protein, List<Boolean>>>();
		getResults(evaluator, scores, outputMap);
		writeResults(outputMap, scores, outputFile);
	}

	private void addToMap(Map<String, Set<String>> map, String a, String b) {
		Set<String> mapped = map.get(a);
		if (mapped == null) {
			mapped = new HashSet<String>();
			mapped.add(b);
			map.put(a, mapped);
		} else {
			mapped.add(b);
		}
	}

	private void getResults(MutationEvaluator evaluator, List<String> scores,
			Map<Mutation, Map<Protein, List<Boolean>>> outputMap) {
		for (ClassifierScore score : ClassifierScore.values()) {
			if (!score.isBinaryScore()) {
				continue;
			}
			Settings.setBINDING_SITE_CLASSIFIER(score);
			Map<Mutation, Map<Protein, Boolean>> results = evaluator.getClassifiedInteractionPartners();
			scores.add(score.toString());
			for (Entry<Mutation, Map<Protein, Boolean>> resultsEntry : results.entrySet()) {
				Mutation mutation = resultsEntry.getKey();
				Map<Protein, Boolean> classifiedProteinInteractors = resultsEntry.getValue();
				Map<Protein, List<Boolean>> output = outputMap.get(mutation);
				if (output == null) {
					output = new HashMap<Protein, List<Boolean>>();
					outputMap.put(mutation, output);
				}
				for (Entry<Protein, Boolean> classifiedProteinInteractorsEntry : classifiedProteinInteractors
						.entrySet()) {
					Protein classifiedInteractor = classifiedProteinInteractorsEntry.getKey();
					Boolean deleted = classifiedProteinInteractorsEntry.getValue();
					List<Boolean> mappedClassificationsList = output.get(classifiedInteractor);
					if (mappedClassificationsList == null) {
						mappedClassificationsList = new LinkedList<Boolean>();
						output.put(classifiedInteractor, mappedClassificationsList);
					}
					mappedClassificationsList.add(deleted);
				}
			}
		}
	}

	private Set<String> readMutations(String mutationFile) {
		Set<String> mutationIDs = new HashSet<String>();
		if (!mutationFile.startsWith("/"))
			mutationFile = "/" + mutationFile;
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
				mutationFile.trim().endsWith(".gz") ? new GZIPInputStream(Main.class.getResourceAsStream(mutationFile))
						: Main.class.getResourceAsStream(mutationFile)))) {
			String line = "";
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("rs"))
					mutationIDs.add(line.trim());
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error while reading mutations file");
		}
		return mutationIDs;
	}

	private Map<String, Set<String>> readPPIN(String ppinFile) {
		if (ppinFile.equals("")) {
			ppinFile = "/data/consensus_network.txt.gz";
		} else if (!ppinFile.startsWith("/"))
			ppinFile = "/" + ppinFile;
		Map<String, Set<String>> ppin = new HashMap<String, Set<String>>();
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(new GZIPInputStream(Main.class.getResourceAsStream(ppinFile))))) {
			String line = "";
			while ((line = reader.readLine()) != null) {
				String[] values = line.trim().split("\t");
				if (values.length != 2) {
					continue;
				}
				addToMap(ppin, values[0], values[1]);
				addToMap(ppin, values[1], values[0]);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error while reading ppin file");
		}
		return ppin;
	}

	private void writeResults(Map<Mutation, Map<Protein, List<Boolean>>> outputMap, List<String> scores,
			String outputFile) {
		try (BufferedWriter writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(new File(outputFile))))) {
			StringJoiner header = new StringJoiner("\t", "", "\n").add("Mutation").add("MutatedProtein")
					.add("InteractingProtein");
			for (String score : scores) {
				header.add(score);
			}
			header.add("MutatedDomains").add("Polyphen2").add("SIFT").add("Polyphen2-Pred").add("SIFT-Pred")
					.add("EnsemblGeneID").add("EnsemblTranscriptID").add("ProteinAllele").add("ProteinPosition")
					.add("Consequence");
			writer.write(header.toString());
			for (Entry<Mutation, Map<Protein, List<Boolean>>> resultsEntry : outputMap.entrySet()) {
				Mutation mutation = resultsEntry.getKey();
				String prefix = mutation.getDbSNP() + "\t" + mutation.getUniprotID();
				StringJoiner domains = new StringJoiner(":");
				for (String domainID : mutation.getAffectedPfamDomains()) {
					domains.add(domainID);
				}
				String suffix = new StringJoiner("\t").add(domains.toString())
						.add(Double.toString(mutation.getPolyphenScore())).add(Double.toString(mutation.getSiftScore()))
						.add(mutation.getPolyphenPrediction()).add(mutation.getSiftPrediction())
						.add(mutation.getGeneID()).add(mutation.getTranscriptID()).add(mutation.getProteinAllele())
						.add(Long.toString(mutation.getProteinPosition())).add(mutation.getConsequence().toString())
						.toString();
				for (Entry<Protein, List<Boolean>> interactorEntry : resultsEntry.getValue().entrySet()) {
					String interactingProteinID = interactorEntry.getKey().getUniprotID();
					StringJoiner line = new StringJoiner("\t", "", "\n").add(prefix).add(interactingProteinID);
					for (Boolean deleted : interactorEntry.getValue()) {
						Integer deletedInt = deleted ? 1 : 0;
						line.add(deletedInt.toString());
					}
					line.add(suffix);
					writer.write(line.toString());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error while writing results file");
		}
	}
}
