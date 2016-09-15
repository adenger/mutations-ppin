package framework;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public final class MutationQuery {
	private final static String server = "http://rest.ensembl.org";
	private final static String tmpFileName = "/mutation_tmp.json";
	// for http error 400 try changing these two
	private final static int MUTATION_MAX_POSTSIZE = 200;
	private final static int TRANSCRIPT_MAX_POSTSIZE = 1000;

	public static Set<Mutation> getMutations(Set<String> mutationIDs) {
		if (Settings.LOCAL_MUTATION_DATA) {
			return restoreFromLocalTmp(mutationIDs);
		}
		Set<Mutation> mutations = new HashSet<Mutation>();
		String ext = "/vep/human/id";
		int maxPostSize = MUTATION_MAX_POSTSIZE;
		for (Iterator<String> iter = mutationIDs.iterator(); iter.hasNext();) {
			StringJoiner postBodyJoiner = new StringJoiner("\", \"", "{ \"ids\" : [\"", "\" ], \"domains\":\"true\" }");
			for (int i = 0; i < maxPostSize && iter.hasNext(); i++) {
				postBodyJoiner.add(iter.next());
			}
			String postBody = postBodyJoiner.toString();
			JSONArray mutationData = new JSONArray(sendRequest(postBody, server, ext));
			mutations.addAll(processMutationData(mutationData));
		}
		saveLocalJsonObject(mutations);
		return mutations;
	}

	private static Mutation getMutation(String dbSNP, JSONObject transcriptJson, String consequenceString) {
		try {
			String biotype = transcriptJson.getString("biotype");
			if (!biotype.equals("protein_coding")) {
				return null;
			}
			Consequence consequence = Consequence.getConsequence(consequenceString);
			if (consequence == null)
				return null;
			JSONArray transcriptConsequenceJsonArray = transcriptJson.getJSONArray("consequence_terms"); // needs
			boolean containsCorrectConsequence = false;
			for (Object transcriptConsequenceObj : transcriptConsequenceJsonArray) {
				String transcriptConsequenceString = (String) transcriptConsequenceObj;
				if (transcriptConsequenceString.equals(consequenceString)) {
					containsCorrectConsequence = true;
					break;
				}
			}
			if (!containsCorrectConsequence) {
				return null;
			}
			String geneID = transcriptJson.getString("gene_id");
			String transcriptID = transcriptJson.getString("transcript_id");
			String aminoAcidAllele = transcriptJson.getString("amino_acids");
			long mutationProteinPosition = transcriptJson.getLong("protein_end");
			double polyphenScore = transcriptJson.getDouble("polyphen_score");
			String polyphenPrediction = transcriptJson.getString("polyphen_prediction");
			double siftScore = transcriptJson.getDouble("sift_score");
			String siftPrediction = transcriptJson.getString("sift_prediction");
			Set<String> mutationCrossingDomains = new HashSet<String>();
			JSONArray domainsJsonArray = transcriptJson.getJSONArray("domains");
			for (Object domainsObj : domainsJsonArray) {
				JSONObject domainJsonObj = (JSONObject) domainsObj;
				String domainDb = domainJsonObj.getString("db");
				if (domainDb.equals("Pfam_domain")) {
					String domainName = domainJsonObj.getString("name");
					mutationCrossingDomains.add(domainName);
				}
			}
			return new Mutation(dbSNP, geneID, transcriptID, aminoAcidAllele, mutationProteinPosition, polyphenScore,
					polyphenPrediction, siftScore, siftPrediction, consequence, mutationCrossingDomains);
		} catch (JSONException e) {
			return null;
		}
	}

	private static Set<Mutation> processMutationData(JSONArray mutationData) {
		Set<Mutation> mutations = new HashSet<Mutation>();
		Map<String, Set<Mutation>> mutationIdToMutations = new HashMap<String, Set<Mutation>>();
		for (Object mutationObj : mutationData) {
			JSONObject mutationJson = (JSONObject) mutationObj;
			String id = mutationJson.getString("id");
			String consequenceString = mutationJson.getString("most_severe_consequence");
			if (Consequence.getConsequence(consequenceString) == null)
				continue;
			JSONArray transcripts = mutationJson.getJSONArray("transcript_consequences");
			for (Object transcriptObj : transcripts) {
				JSONObject transcriptJson = (JSONObject) transcriptObj;
				Mutation mutation;
				mutation = getMutation(id, transcriptJson, consequenceString);
				if (mutation == null) {
					continue;
				}
				Set<Mutation> mappedMutations = mutationIdToMutations.get(id);
				if (mappedMutations == null) {
					mappedMutations = new HashSet<Mutation>();
					mappedMutations.add(mutation);
					mutationIdToMutations.put(id, mappedMutations);
				} else {
					mappedMutations.add(mutation);
				}
			}
		}
		setTranscriptLengths(mutationIdToMutations);
		setUniprotIDs(mutationIdToMutations);
		for (String mutationID : mutationIdToMutations.keySet()) {
			List<Mutation> possibleMutations = new ArrayList<Mutation>(mutationIdToMutations.get(mutationID));
			possibleMutations.removeIf(m -> m.getUniprotID() == null || m.getTranscriptLength() == null);
			if (possibleMutations.isEmpty())
				continue;
			possibleMutations.sort(null);
			Mutation bestMutation = possibleMutations.get(possibleMutations.size() - 1);
			mutations.add(bestMutation);
		}
		return mutations;
	}

	private static Set<Mutation> restoreFromLocalTmp(Set<String> mutationIDs) {
		Set<Mutation> mutations = new HashSet<Mutation>();
		try (InputStream stream = MutationQuery.class.getResource(tmpFileName).openStream()) {
			JSONTokener tokener = new JSONTokener(stream);
			JSONObject rootObj = new JSONObject(tokener);
			for (String mutationID : mutationIDs) {
				if (!rootObj.has(mutationID))
					continue;
				JSONObject mutationJsonObj = rootObj.getJSONObject(mutationID);
				Mutation mutation = new Mutation(mutationJsonObj);
				mutations.add(mutation);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return mutations;
	}

	private static void saveLocalJsonObject(Set<Mutation> mutations) {
		JSONObject allObj = new JSONObject();
		for (Mutation mutation : mutations) {
			allObj.put(mutation.getDbSNP(), mutation.toJsonObject());
		}
		try (FileWriter writer = new FileWriter(tmpFileName.substring(1))) {
			writer.write(allObj.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String sendRequest(String postBody, String server, String ext) {
		String output = "";
		try {
			URL url = new URL(server + ext);
			HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
			httpConnection.setRequestMethod("POST");
			httpConnection.setRequestProperty("Content-Type", "application/json");
			httpConnection.setRequestProperty("Accept", "application/json");
			httpConnection.setRequestProperty("Content-Length", Integer.toString(postBody.getBytes().length));
			httpConnection.setUseCaches(false);
			httpConnection.setDoInput(true);
			httpConnection.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(httpConnection.getOutputStream());
			wr.writeBytes(postBody);
			wr.flush();
			wr.close();
			InputStream response = httpConnection.getInputStream();
			int responseCode = httpConnection.getResponseCode();
			if (responseCode != 200) {
				throw new RuntimeException("Response code was not 200. Detected response was " + responseCode);
			}
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(response, "UTF-8"))) {
				StringBuilder builder = new StringBuilder();
				String line = "";
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
				output = builder.toString();
			} catch (IOException logOrIgnore) {
				logOrIgnore.printStackTrace();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return output;
	}

	private static void setTranscriptLengths(Map<String, Set<Mutation>> mutationIdToMutations) {
		Set<String> allTranscriptIDs = new HashSet<String>();
		for (Set<Mutation> mutations : mutationIdToMutations.values()) {
			for (Mutation mutation : mutations) {
				String transcriptID = mutation.getTranscriptID();
				allTranscriptIDs.add(transcriptID);
			}
		}
		String ext = "/lookup/id";
		int maxPostSize = TRANSCRIPT_MAX_POSTSIZE;
		for (Iterator<String> iter = allTranscriptIDs.iterator(); iter.hasNext();) {
			StringJoiner postBodyJoiner = new StringJoiner("\", \"", "{ \"ids\" : [\"", "\" ]}");
			for (int i = 0; i < maxPostSize && iter.hasNext(); i++) {
				postBodyJoiner.add(iter.next());
			}
			String postBody = postBodyJoiner.toString();
			JSONObject transcriptsJsonObj = new JSONObject(sendRequest(postBody, server, ext));
			for (Set<Mutation> mutations : mutationIdToMutations.values()) {
				for (Mutation mutation : mutations) {
					String transcriptID = mutation.getTranscriptID();
					if (transcriptsJsonObj.has(transcriptID)) {
						JSONObject transcriptJsonObj = transcriptsJsonObj.getJSONObject(transcriptID);
						Long start = transcriptJsonObj.getLong("start");
						Long end = transcriptJsonObj.getLong("end");
						mutation.setTranscriptLength(end - start);
					}
				}
			}
		}
	}

	private static void setUniprotIDs(Map<String, Set<Mutation>> mutationIdToMutations) {
		Set<String> geneIDs = new HashSet<String>();
		for (Set<Mutation> mutations : mutationIdToMutations.values()) {
			for (Mutation mutation : mutations) {
				geneIDs.add(mutation.getGeneID());
			}
		}
		Map<String, String> geneIdToUniprot = new HashMap<String, String>();
		String xml = BiomartXmlHandler.getUniprotIDsXML(geneIDs);
		List<String[]> results = BiomartQuery.sendBiomartQuery(xml);
		for (String[] result : results) {
			if (result.length != 2)
				continue;
			String geneID = result[0].trim();
			String uniprotID = result[1].trim();
			if (geneID.isEmpty() || uniprotID.isEmpty())
				continue;
			geneIdToUniprot.put(geneID, uniprotID);
		}
		for (Set<Mutation> mutations : mutationIdToMutations.values()) {
			for (Mutation mutation : mutations) {
				String uniprotID = geneIdToUniprot.get(mutation.getGeneID());
				mutation.setUniprotID(uniprotID);
			}
		}
	}

	private MutationQuery() {
	}
}
