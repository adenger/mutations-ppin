package framework;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class MutationEvaluator {

	private final static Logger log = Logger.getLogger(MutationEvaluator.class.getName());

	private final Set<Mutation> mutations;
	private final ProteinNetwork network;

	private final Map<String, Set<String>> domainInteractions;

	private MutationClassifier classifier = new MutationClassifier();

	/**
	 * @param proteinNetwork
	 *            A graph of uniprot accession ID's
	 * @param snpIDs
	 *            A set of dbSNP ID's in the rs<SNP-ID> format
	 */
	public MutationEvaluator(Map<String, Set<String>> proteinNetwork, Set<String> snpIDs) {
		this.initializeLogger();
		log.info("Starting to retrieve data");
		log.info("Retrieving mutation data");
		this.mutations = MutationQuery.getMutations(snpIDs);
		log.info("Retrieved data for " + mutations.size() + " mutations");
		log.info("Retrieving protein network");
		this.network = ProteinNetworkFactory.getProteinNetwork(proteinNetwork);
		log.info("Retrieved interactions data for " + this.network.size() + " proteins");
		log.info("Retrieving domain interaction data");
		this.domainInteractions = DomainInteractionQuery.getInteractions();
		log.info("Building domain network");
		log.info("Finished retrieving data.");
	}

	/**
	 * @return A map containing the mutations as keys and the classified
	 *         interactions as values. Each value maps the interaction partners
	 *         of the protein the mutation occurred in to whether they have been
	 *         deleted (true) or not(false).
	 */
	public Map<Mutation, Map<Protein, Boolean>> getClassifiedInteractionPartners() {
		Map<Mutation, Map<Protein, Boolean>> classifiedInteractions = new HashMap<Mutation, Map<Protein, Boolean>>();
		for (Mutation mutation : mutations) {
			log.info("calculating deleted interactions for mutation " + mutation);
			String uniprotID = mutation.getUniprotID();
			Protein protein = network.getProtein(uniprotID);
			if (protein == null) {
				log.info("affected protein not found in network, skip");
				continue;
			}
			log.info("affected protein is " + protein);
			Map<Protein, Boolean> classified = classifier.classify(mutation, network, domainInteractions);
			if (classified == null) {
				log.info("Not able to classify lost protein connections");
				continue;
			}
			log.info("classified protein connections: " + classified);
			classifiedInteractions.put(mutation, classified);
		}
		return classifiedInteractions;
	}

	public ProteinNetwork getNetwork() {
		return network;
	}

	private void initializeLogger() {
		if (Settings.DISABLE_LOG)
			LogManager.getLogManager().reset();
		if (Settings.DISABLE_LOG_FILE)
			return;
		try {
			Logger globalLogger = Logger.getLogger("");
			Handler handler = new FileHandler("log.txt");
			globalLogger.addHandler(handler);
			Formatter formatter = new SimpleFormatter();
			handler.setFormatter(formatter);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

}
