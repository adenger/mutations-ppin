package framework;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class ProteinNetwork implements Iterable<Entry<Protein, Set<Protein>>> {
	private Map<Protein, Set<Protein>> network = new HashMap<Protein, Set<Protein>>();

	public ProteinNetwork() {

	}

	public void addInteraction(Protein p1, Protein p2) {
		addConnection(p1, p2);
		addConnection(p2, p1);
	}

	public void addInteractions(Protein protein, Set<Protein> interactors) {
		for (Protein interactor : interactors) {
			addInteraction(protein, interactor);
		}
	}

	public Set<Protein> getInteractions(Protein protein) {
		return network.get(protein);
	}

	public Set<Protein> getInteractions(String proteinID) {
		Protein protein = this.getProtein(proteinID);
		if (protein == null)
			return null;
		return getInteractions(protein);
	}

	public Protein getProtein(String proteinID) {
		for (Protein protein : network.keySet()) {
			if (protein.getUniprotID().equals(proteinID)) {
				return protein;
			}
		}
		return null;
	}

	public Set<Protein> getProteins() {
		return network.keySet();
	}

	@Override
	public Iterator<Entry<Protein, Set<Protein>>> iterator() {
		return network.entrySet().iterator();
	}

	public int size() {
		return network.keySet().size();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (Entry<Protein, Set<Protein>> entry : network.entrySet()) {
			for (Protein protein2 : entry.getValue()) {
				builder.append(entry.getKey().getUniprotID()).append("\t").append(protein2.getUniprotID()).append("\n");
			}
		}
		return builder.toString();
	}

	private void addConnection(Protein p1, Protein p2) {
		Set<Protein> interactors = network.get(p1);
		if (interactors == null) {
			interactors = new HashSet<Protein>();
			interactors.add(p2);
			network.put(p1, interactors);
		} else {
			interactors.add(p2);
		}
	}
}
