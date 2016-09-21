package framework;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.GZIPInputStream;

public class DomainInteractionQuery {

	private static Map<String, Set<String>> interactions = new HashMap<String, Set<String>>();
	private static final String iddiFile = "/data/iddi_1.0.txt.gz";
	private static final String iPfamFileHeteroDomain = "/data/heterodomain_interaction.csv.gz";
	private static final String iPfamFileHomoDomain = "/data/homodomain_interaction.csv.gz";
	private static final String domineFile = "/data/domine_2.0.txt.gz";
	private static final String file3did = "/data/3did_flat.gz";

	public static Map<String, Set<String>> getInteractions() {
		if (Settings.DOMAIN_DATA_3DID())
			read3did();
		if (Settings.DOMAIN_DATA_DOMINE())
			readDomine();
		if (Settings.DOMAIN_DATA_IPFAM())
			readIpfam();
		if (Settings.DOMAIN_DATA_IDDI())
			readIddi();
		return interactions;
	}

	private static void addDirectedEdge(String source, String target) {
		Set<String> interactors = interactions.get(source);
		if (interactors == null) {
			interactors = new HashSet<String>();
			interactors.add(target);
			interactions.put(source, interactors);
		} else {
			interactors.add(target);
		}
	}

	private static void addUndirectedEdge(String interactorA, String interactorB) {
		addDirectedEdge(interactorA, interactorB);
		addDirectedEdge(interactorB, interactorA);
	}

	private static BufferedReader getReader(String filename) throws Exception {
		InputStream stream = DomainInteractionQuery.class.getResourceAsStream(filename);
		return new BufferedReader(new InputStreamReader(new GZIPInputStream(stream)));
	}

	private static void read3did() {
		try (BufferedReader reader = getReader(file3did)) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (!line.isEmpty() && line.startsWith("#=ID")) {
					String[] values = line.split("\\s+");
					String idA = values[3].replace("(", "").split("\\.")[0];
					String idB = values[4].split("\\.")[0];
					addUndirectedEdge(idA, idB);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void readDomine() {
		// NA = observed, HC = high confidence
		try (BufferedReader reader = getReader(domineFile)) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] values = line.split("\\|");
				String confidence = values[values.length - 2];
				if (Settings.DOMAIN_DATA_NO_PREDICTIONS() ? confidence.equals("NA")
						: confidence.equals("NA") || confidence.equals("HC")) {
					String idA = values[0];
					String idB = values[1];
					addUndirectedEdge(idA, idB);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void readIddi() {
		// 0.329 for 98% accuracy, 0.102 for 90%
		try (BufferedReader reader = getReader(iddiFile)) {
			if (reader.ready())
				reader.readLine();
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (line.trim().length() == 0)
					continue;
				String[] values = line.split("\t");
				double score = Double.parseDouble(values[values.length - 1]);
				double threshold = Settings.DOMAIN_DATA_NO_PREDICTIONS() ? 0.329d : 0.102d;
				if (score >= threshold) {
					String idA = values[0];
					String idB = values[1];
					addUndirectedEdge(idA, idB);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void readIpfam() {
		try (BufferedReader reader = getReader(iPfamFileHeteroDomain)) {
			if (reader.ready())
				reader.readLine();
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (line.trim().length() == 0)
					continue;
				String[] values = line.split("\\s+");
				String idA = values[0];
				String idB = values[2];
				addUndirectedEdge(idA, idB);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		try (BufferedReader reader = getReader(iPfamFileHomoDomain)) {
			if (reader.ready())
				reader.readLine();
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (line.trim().length() == 0)
					continue;
				String[] values = line.split("\\s+");
				String id = values[0];
				addDirectedEdge(id, id);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (Entry<String, Set<String>> entry : interactions.entrySet()) {
			for (String domains2 : entry.getValue()) {
				builder.append(entry.getKey()).append("\t").append(domains2).append("\n");
			}
		}
		return builder.toString();
	}
}
