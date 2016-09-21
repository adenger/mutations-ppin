package framework;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

public class MutationClassifier {
	private final static Logger log = Logger.getLogger(MutationClassifier.class.getName());
	private final static String file3did = "/data/3did_flat.gz";
	private final static String file3didInterface = "/data/3did_interface_flat.gz";
	private Map<String, String> pfamToDomainSymbol = new HashMap<String, String>();
	private Map<String, String> domainSymbolToPfam = new HashMap<String, String>();
	private Map<String, Map<String, DomainInteractionSurface>> interfaces = new HashMap<String, Map<String, DomainInteractionSurface>>();
	private Mutation mutation;
	private ProteinNetwork network;
	private Map<String, Set<String>> domainInteractions;
	private Protein protein;
	private BlosumMatrix matrix = new BlosumMatrix(Settings.CLASSIFIER_BLOSUM_MATRIX());

	public MutationClassifier() {
		readPfamToDomainID();
		readInterfaces();
	}

	/**
	 * @param mutation
	 *            A mutation that needs to be classified, including all the
	 *            necessary data.
	 * @param network
	 *            A protein network that shows the interactions the mutated
	 *            protein partakes in. Includes proteins and their data.
	 * @param domainInteractions
	 *            A graph containing domain-pairs that are known to interact, as
	 *            Pfam-ID's
	 * @return Map that maps interaction partners of the protein the mutation
	 *         occurred in to whether they have been deleted (true) or
	 *         not(false).
	 */
	public Map<Protein, Boolean> classify(Mutation mutation, ProteinNetwork network,
			Map<String, Set<String>> domainInteractions) {
		String uniprotID = mutation.getUniprotID();
		Protein protein = network.getProtein(uniprotID);
		if (protein == null) {
			return null;
		}
		this.mutation = mutation;
		this.network = network;
		this.domainInteractions = domainInteractions;
		this.protein = protein;

		switch (mutation.getConsequence()) {
		case FRAMESHIFT:
			return classifyFrameshift();
		case MISSENSE:
			return classifyMissense();
		case NONSENSE:
			return classifyNonsense();
		case SYNOMYMOUS:
			return classifySynonymous();
		default:
			return null;
		}
	}

	private Map<Protein, Boolean> classifyFrameshift() {
		return deleteConnectionsAfterMutation();
	}

	private Map<Protein, Boolean> classifyMissense() {
		log.info("Classifying missense mutation with " + Settings.BINDING_SITE_CLASSIFIER().toString());
		Map<Domain, String> affectedDomains = getAffectedDomains();
		log.info("Number of affected domains: " + affectedDomains.size());
		Map<String, Map<String, Boolean>> classifiedDomainConnections = getClassifiedDomainConnections(affectedDomains);
		log.info("Number of affected interfaces with domain connections: " + classifiedDomainConnections.size());
		Map<Protein, Boolean> classifiedProteinConnections = getClassifiedProteinConnections(
				classifiedDomainConnections);
		log.info("Number of classified protein connections: " + classifiedProteinConnections.size());
		return classifiedProteinConnections;
	}

	private Map<Protein, Boolean> classifyNonsense() {
		return deleteConnectionsAfterMutation();
	}

	private Map<Protein, Boolean> classifySynonymous() {
		return deleteNoConnections();
	}

	// private Map<Protein, Boolean> deleteAllConnections() {
	// return setAllConnections(true);
	// }

	private Map<Protein, Boolean> deleteConnectionsAfterMutation() {
		Map<Protein, Boolean> classifiedConnections = new HashMap<Protein, Boolean>();
		if (protein.getDomains().isEmpty()) {
			return classifiedConnections;
		}
		long mutationPosition = mutation.getProteinPosition();
		Map<Domain, Boolean> classifiedDomains = new HashMap<Domain, Boolean>();
		for (Domain domain : protein.getDomains()) {
			classifiedDomains.put(domain, mutationPosition <= domain.getSequenceStart());
		}
		Set<Protein> interactingProteins = network.getInteractions(protein);
		if (interactingProteins == null) {
			return classifiedConnections;
		}
		for (Entry<Domain, Boolean> classifiedDomainsEntry : classifiedDomains.entrySet()) {
			Domain classifiedDomain = classifiedDomainsEntry.getKey();
			Boolean deleted = classifiedDomainsEntry.getValue();
			Set<String> interactingDomainIDs = domainInteractions.get(classifiedDomain.getPfamID());
			if (interactingDomainIDs == null) {
				continue;
			}
			for (Protein interactingProtein : interactingProteins) {
				for (Domain interactingDomain : interactingProtein.getDomains()) {
					if (interactingDomain.getPfamID().equals(classifiedDomain.getPfamID())) {
						classifiedConnections.put(interactingProtein, deleted);
						break;
					}
				}
			}
		}
		return classifiedConnections;
	}

	private Map<Protein, Boolean> deleteNoConnections() {
		return setAllConnections(false);
	}

	/**
	 * @return A map containing all domains in the mutated protein that overlap
	 *         with the mutation, usually only one. They are mapped to the
	 *         domain symbols used by the 3did database that are needed by this
	 *         classifier.
	 */
	private Map<Domain, String> getAffectedDomains() {
		Map<Domain, String> affectedDomains = new HashMap<Domain, String>();
		Set<String> affectedDomainIDs = mutation.getAffectedPfamDomains();
		for (Domain domain : protein.getDomains()) {
			if (affectedDomainIDs.contains(domain.getPfamID())
					&& domain.getSequenceStart() <= mutation.getProteinPosition()
					&& mutation.getProteinPosition() <= domain.getSequenceEnd()) {
				String pfamID = domain.getPfamID();
				String domainSymbol = pfamToDomainSymbol.get(pfamID);
				affectedDomains.put(domain, domainSymbol);
			}
		}
		return affectedDomains;
	}

	private boolean getBindingSiteClassification() {
		char original = mutation.getProteinAllele().charAt(0), mutated = mutation.getProteinAllele().charAt(2);
		switch (Settings.BINDING_SITE_CLASSIFIER()) {
		case BLOSUM:
			return matrix.get(original, mutated) < 0;
		case BLOSUM_PERCENTAGE:
			return matrix.getPercentage(original, mutated) > Settings.CUTOFF_PERCENTAGE();
		case NULL:
			return true;
		case POLYPHEN_2:
			return mutation.getPolyphenPrediction().startsWith("p");
		case POLYPHEN_2_HC:
			return mutation.getPolyphenPrediction().equals("probably_damaging");
		case PROPERTIES:
			return getProperties(original, mutated);
		case SIFT:
			return mutation.getSiftPrediction().startsWith("deleterious");
		case SIFT_HC:
			return mutation.getSiftPrediction().equals("deleterious");
		case HOTSPOT_ENRICHED:
			return AminoAcidProperties.isHotspotEnriched(original) && getProperties(original, mutated);
		case POLYPHEN_2_SCORE:
			return mutation.getPolyphenScore() > Settings.CUTOFF_PERCENTAGE();
		case SIFT_SCORE:
			return 1 - mutation.getSiftScore() > Settings.CUTOFF_PERCENTAGE();
		case PSHC:
			return mutation.getPolyphenPrediction().equals("probably_damaging")
					|| mutation.getSiftPrediction().equals("deleterious");
		case HPSHC:
			return mutation.getPolyphenPrediction().equals("probably_damaging")
					|| mutation.getSiftPrediction().equals("deleterious")
					|| (AminoAcidProperties.isHotspotEnriched(original) && getProperties(original, mutated));

		default:
			return true;
		}
	}

	/**
	 * @param affectedDomains
	 *            A map containing all domains in the mutated protein that
	 *            overlap with the mutation mapped to the domain symbols used by
	 *            the 3did database that are needed by this classifier.
	 * @return A graph containing classified domain-domain interactions as pfam
	 *         IDs and whether they have been deleted (true) or not(false)
	 */
	private Map<String, Map<String, Boolean>> getClassifiedDomainConnections(Map<Domain, String> affectedDomains) {
		Map<String, Map<String, Boolean>> classifiedDomainConnections = new HashMap<String, Map<String, Boolean>>();
		for (Entry<Domain, String> affectedDomainsEntry : affectedDomains.entrySet()) {
			Domain affectedDomain = affectedDomainsEntry.getKey();
			// log.info("Classifying domain connections for " +
			// affectedDomain.getPfamID());
			String affectedDomainPfamID = affectedDomain.getPfamID();
			Boolean deleted = getBindingSiteClassification();
			Map<String, Boolean> mappedInteractingDomains = new HashMap<String, Boolean>();
			// temporary solution that ignores interface data
			Set<String> mappedDomains = domainInteractions.get(affectedDomainPfamID);
			if (mappedDomains == null)
				continue;
			for (String domainID : mappedDomains) {
				mappedInteractingDomains.put(domainID, deleted);
			}
			// TODO map protein to pdb structure, map mutation domain position
			// to pfam hmm position, get interface data from interfaces map
			classifiedDomainConnections.put(affectedDomainPfamID, mappedInteractingDomains);
		}
		return classifiedDomainConnections;
	}

	/**
	 * @param classifiedDomainConnections
	 *            A graph containing classified domain-domain interactions as
	 *            pfam IDs and whether they have been deleted (true) or
	 *            not(false)
	 * @return The protein-protein interaction partners of the mutated protein
	 *         and whether they have been deleted (true) or not(false) based on
	 *         the provided domain interaction deletions
	 */
	private Map<Protein, Boolean> getClassifiedProteinConnections(
			Map<String, Map<String, Boolean>> classifiedDomainConnections) {
		Map<Protein, Boolean> classifiedProteinConnections = new HashMap<Protein, Boolean>();
		Set<Protein> interactingProteins = network.getInteractions(protein);
		log.info("Total amount of interacting proteins: " + interactingProteins.size());
		for (Entry<String, Map<String, Boolean>> classifiedDomainConnectionsEntry : classifiedDomainConnections
				.entrySet()) {
			String mutatedProteinDomainID = classifiedDomainConnectionsEntry.getKey();
			if (!mutation.getAffectedPfamDomains().contains(mutatedProteinDomainID)) {
				continue;
			}
			Map<String, Boolean> classifiedInteractingDomains = classifiedDomainConnectionsEntry.getValue();
			for (Entry<String, Boolean> classifiedInteractingDomainsEntry : classifiedInteractingDomains.entrySet()) {
				String domainInteractionPartner = classifiedInteractingDomainsEntry.getKey();
				Boolean deleted = classifiedInteractingDomainsEntry.getValue();
				for (Protein interactingProtein : interactingProteins) {
					for (Domain interactingProteinDomain : interactingProtein.getDomains()) {
						if (domainInteractionPartner.equals(interactingProteinDomain.getPfamID())) {
							classifiedProteinConnections.put(interactingProtein, deleted);
						}
					}
				}
			}
		}
		return classifiedProteinConnections;
	}

	// TODO Optimize
	private boolean getProperties(char aminoAcid1, char aminoAcid2) {
		return (AminoAcidProperties.isNegative(aminoAcid1) ^ AminoAcidProperties.isNegative(aminoAcid2))
				|| (AminoAcidProperties.isPositive(aminoAcid1) ^ AminoAcidProperties.isPositive(aminoAcid2))
				|| (AminoAcidProperties.isAliphatic(aminoAcid1) ^ AminoAcidProperties.isAliphatic(aminoAcid2))
				|| (AminoAcidProperties.isAromatic(aminoAcid1) ^ AminoAcidProperties.isAromatic(aminoAcid2))
				|| (AminoAcidProperties.isHydrophilic(aminoAcid1) ^ AminoAcidProperties.isHydrophilic(aminoAcid2))
				|| (AminoAcidProperties.isHydrophobic(aminoAcid1) ^ AminoAcidProperties.isHydrophobic(aminoAcid2))
				|| (AminoAcidProperties.isSmall(aminoAcid1) ^ AminoAcidProperties.isSmall(aminoAcid2))
				|| (AminoAcidProperties.isCharged(aminoAcid1) ^ AminoAcidProperties.isCharged(aminoAcid2));
	}

	// Test
	private void readInterfaces() {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
				new GZIPInputStream(MutationClassifier.class.getResourceAsStream(file3didInterface))))) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (!line.isEmpty() && line.startsWith("#=ID")) {
					String[] values = line.split("\\s+");
					String symbolA = values[1], symbolB = values[2];
					DomainInteractionSurface surface = new DomainInteractionSurface(symbolA, symbolB);
					while (!(line = reader.readLine()).startsWith("//")) {
						surface.add3didInterfaceLine(line);
					}
					Map<String, DomainInteractionSurface> mappedSurfaces = interfaces.get(symbolA);
					if (mappedSurfaces == null) {
						mappedSurfaces = new HashMap<String, DomainInteractionSurface>();
						mappedSurfaces.put(symbolB, surface);
						interfaces.put(symbolA, mappedSurfaces);
					} else {
						mappedSurfaces.put(symbolB, surface);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void readPfamToDomainID() {
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(new GZIPInputStream(MutationClassifier.class.getResourceAsStream(file3did))))) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (!line.isEmpty() && line.startsWith("#=ID")) {
					String[] values = line.split("\\s+");
					String symbolA = values[1];
					String symbolB = values[2];
					String pfamA = values[3].replace("(", "").split("\\.")[0];
					String pfamB = values[4].split("\\.")[0];
					pfamToDomainSymbol.put(pfamA, symbolA);
					pfamToDomainSymbol.put(pfamB, symbolB);
					domainSymbolToPfam.put(symbolA, pfamA);
					domainSymbolToPfam.put(symbolB, pfamB);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Map<Protein, Boolean> setAllConnections(boolean value) {
		Map<Protein, Boolean> classified = new HashMap<Protein, Boolean>();
		Set<Protein> interactingProteins = network.getInteractions(protein);
		for (Protein interactingProtein : interactingProteins) {
			classified.put(interactingProtein, value);
		}
		return classified;
	}
}
