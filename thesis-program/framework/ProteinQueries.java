package framework;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;
import org.json.JSONTokener;

public final class ProteinQueries {
	private final static int proteinDataSize = 7;
	private final static String tmpFileName = "/protein_tmp.json";

	public static Set<Protein> getProteinData(Set<String> proteinIDs) {
		if (Settings.LOCAL_PROTEIN_DATA)
			return restoreFromLocalTmp(proteinIDs);
		String xml = BiomartXmlHandler.getProteinsXML(proteinIDs);
		List<String[]> results = BiomartQuery.sendBiomartQuery(xml);
		Set<Protein> proteins = processDomainData(results);
		saveAsJson(proteins);
		return proteins;
	}

	private static boolean isValidResult(String[] result, int length) {
		if (result.length != length) {
			return false;
		}
		// check if one of the strings is empty
		boolean emptyEntry = false;
		for (String string : result) {
			if (string.trim().isEmpty()) {
				emptyEntry = true;
				break;
			}
		}
		return !emptyEntry;
	}

	private static Set<Protein> processDomainData(List<String[]> results) {
		Set<Protein> proteins = new HashSet<Protein>();
		Map<String, Set<String>> uniprotToTranscripts = new HashMap<String, Set<String>>();
		Map<String, Integer> transcriptLengths = new HashMap<String, Integer>();
		Map<String, Set<Domain>> transcriptDomains = new HashMap<String, Set<Domain>>();
		for (String[] result : results) {
			if (!isValidResult(result, proteinDataSize)) {
				continue;
			}
			String biotype = result[6];
			if (!biotype.equals("protein_coding")) {
				continue;
			}
			String transcriptID = result[0];
			String uniprotID = result[1];
			String pfamID = result[2];
			int pfamStart = Integer.parseInt(result[3]);
			int pfamEnd = Integer.parseInt(result[4]);
			int transcriptLength = Integer.parseInt(result[5]);
			Domain domain = new Domain(pfamID, uniprotID, pfamStart, pfamEnd);
			Set<String> mappedTranscripts = uniprotToTranscripts.get(uniprotID);
			if (mappedTranscripts == null) {
				mappedTranscripts = new HashSet<String>();
				mappedTranscripts.add(transcriptID);
				uniprotToTranscripts.put(uniprotID, mappedTranscripts);
			} else {
				mappedTranscripts.add(transcriptID);
			}
			transcriptLengths.put(transcriptID, new Integer(transcriptLength));
			Set<Domain> mappedDomains = transcriptDomains.get(transcriptID);
			if (mappedDomains == null) {
				mappedDomains = new HashSet<Domain>();
				mappedDomains.add(domain);
				transcriptDomains.put(transcriptID, mappedDomains);
			} else {
				mappedDomains.add(domain);
			}
		}
		for (String uniprotID : uniprotToTranscripts.keySet()) {
			Set<String> transcriptSet = uniprotToTranscripts.get(uniprotID);
			if (transcriptSet == null || transcriptSet.isEmpty()) {
				proteins.add(new Protein(uniprotID, new HashSet<Domain>()));
				continue;
			}
			List<String> transcripts = new ArrayList<String>(transcriptSet);
			Collections.sort(transcripts, new Comparator<String>() {
				@Override
				public int compare(String transcript1, String transcript2) {
					Integer length1 = transcriptLengths.get(transcript1);
					Integer length2 = transcriptLengths.get(transcript2);
					return length2.compareTo(length1);
				}
			});
			String longestTranscript = transcripts.get(0);
			Set<Domain> domains = transcriptDomains.get(longestTranscript);
			if (domains == null) {
				domains = new HashSet<Domain>();
			}
			proteins.add(new Protein(uniprotID, domains));
		}
		return proteins;
	}

	private static Set<Protein> restoreFromLocalTmp(Set<String> proteinIDs) {
		Set<Protein> proteins = new HashSet<Protein>();
		try (InputStream stream = ProteinQueries.class.getResource(tmpFileName).openStream()) {
			JSONTokener tokener = new JSONTokener(stream);
			JSONObject rootObj = new JSONObject(tokener);
			for (String proteinID : proteinIDs) {
				if (!rootObj.has(proteinID))
					continue;
				JSONObject proteinJsonObj = rootObj.getJSONObject(proteinID);
				Protein protein = new Protein(proteinJsonObj);
				proteins.add(protein);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return proteins;
	}

	private static void saveAsJson(Set<Protein> proteins) {
		JSONObject allObj = new JSONObject();
		for (Protein protein : proteins) {
			allObj.put(protein.getUniprotID(), protein.toJsonObject());

		}
		try (FileWriter writer = new FileWriter(tmpFileName.substring(1))) {
			writer.write(allObj.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private ProteinQueries() {

	}

}
