package framework;

public final class Settings {
	// only activate after data has been retrieved once
	public static boolean LOCAL_MUTATION_DATA = false;
	public static boolean LOCAL_PROTEIN_DATA = true;
	// DDI databases for PPIN->DDIN conversion
	public static boolean DOMAIN_DATA_3DID = true;
	public static boolean DOMAIN_DATA_IPFAM = true;
	public static boolean DOMAIN_DATA_IDDI = true;
	public static boolean DOMAIN_DATA_DOMINE = true;
	// Only PDB-based data
	public static boolean DOMAIN_DATA_NO_PREDICTIONS = true;
	// logs
	public static boolean DISABLE_LOG = true;
	public static boolean DISABLE_LOG_FILE = false;
	// lower bound. higher percentage = more damaging
	public static double CUTOFF_PERCENTAGE = 0.80d;
	// higher for more closely related sequences
	public static BlosumMatrixName CLASSIFIER_BLOSUM_MATRIX = BlosumMatrixName.BLOSUM100;
	// score for mutation severity assessment
	public static ClassifierScore BINDING_SITE_CLASSIFIER = ClassifierScore.POLYPHEN_2_HC;
}