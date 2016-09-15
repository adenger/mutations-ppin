package framework;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class ProteinNetworkFactory {
	public static ProteinNetwork getProteinNetwork(Map<String, Set<String>> idNetwork) {
		// retrieve protein data
		Set<Protein> proteins = getProteinData(idNetwork);
		// create network
		ProteinNetwork proteinNetwork = createProteinNetwork(proteins, idNetwork);
		return proteinNetwork;
	}

	private static ProteinNetwork createProteinNetwork(Set<Protein> proteins, Map<String, Set<String>> idNetwork) {
		ProteinNetwork proteinNetwork = new ProteinNetwork();
		Map<String, Protein> proteinMap = new HashMap<String, Protein>();
		for (Protein protein : proteins) {
			proteinMap.put(protein.getUniprotID(), protein);
		}
		for (Entry<String, Set<String>> networkEntry : idNetwork.entrySet()) {
			String id = networkEntry.getKey();
			Set<String> interactorIDs = networkEntry.getValue();
			Protein protein = proteinMap.get(id);
			if (protein == null)
				continue;
			for (String interactorID : interactorIDs) {
				Protein interactorProtein = proteinMap.get(interactorID);
				if (interactorProtein == null)
					continue;
				proteinNetwork.addInteraction(protein, interactorProtein);
			}
		}
		return proteinNetwork;
	}

	private static Set<Protein> getProteinData(Map<String, Set<String>> idNetwork) {
		Set<String> proteinIDs = new HashSet<String>();
		for (Entry<String, Set<String>> entry : idNetwork.entrySet()) {
			proteinIDs.add(entry.getKey());
			proteinIDs.addAll(entry.getValue());
		}
		Set<Protein> proteins = ProteinQueries.getProteinData(proteinIDs);
		return proteins;
	}

}
