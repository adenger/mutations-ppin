package framework;

public final class Settings {
	// only activate after data has been retrieved once
	private static boolean LOCAL_MUTATION_DATA = false;
	private static boolean LOCAL_PROTEIN_DATA = true;
	// DDI databases for PPIN->DDIN conversion
	private static boolean DOMAIN_DATA_3DID = true;
	private static boolean DOMAIN_DATA_IPFAM = true;
	private static boolean DOMAIN_DATA_IDDI = true;
	private static boolean DOMAIN_DATA_DOMINE = true;
	// Only PDB-based data
	private static boolean DOMAIN_DATA_NO_PREDICTIONS = true;
	// logs
	private static boolean DISABLE_LOG = true;
	private static boolean DISABLE_LOG_FILE = false;
	// lower bound. higher percentage = more damaging
	private static double CUTOFF_PERCENTAGE = 0.80d;
	// higher for more closely related sequences
	private static BlosumMatrixName CLASSIFIER_BLOSUM_MATRIX = BlosumMatrixName.BLOSUM100;
	// score for mutation severity assessment
	private static ClassifierScore BINDING_SITE_CLASSIFIER = ClassifierScore.POLYPHEN_2_HC;

	public static ClassifierScore BINDING_SITE_CLASSIFIER() {
		return BINDING_SITE_CLASSIFIER;
	}

	public static BlosumMatrixName CLASSIFIER_BLOSUM_MATRIX() {
		return CLASSIFIER_BLOSUM_MATRIX;
	}

	public static double CUTOFF_PERCENTAGE() {
		return CUTOFF_PERCENTAGE;
	}

	public static boolean DISABLE_LOG() {
		return DISABLE_LOG;
	}

	public static boolean DISABLE_LOG_FILE() {
		return DISABLE_LOG_FILE;
	}

	public static boolean DOMAIN_DATA_3DID() {
		return DOMAIN_DATA_3DID;
	}

	public static boolean DOMAIN_DATA_DOMINE() {
		return DOMAIN_DATA_DOMINE;
	}

	public static boolean DOMAIN_DATA_IDDI() {
		return DOMAIN_DATA_IDDI;
	}

	public static boolean DOMAIN_DATA_IPFAM() {
		return DOMAIN_DATA_IPFAM;
	}

	public static boolean DOMAIN_DATA_NO_PREDICTIONS() {
		return DOMAIN_DATA_NO_PREDICTIONS;
	}

	public static boolean LOCAL_MUTATION_DATA() {
		return LOCAL_MUTATION_DATA;
	}

	public static boolean LOCAL_PROTEIN_DATA() {
		return LOCAL_PROTEIN_DATA;
	}

	public static void setBINDING_SITE_CLASSIFIER(ClassifierScore bINDING_SITE_CLASSIFIER) {
		BINDING_SITE_CLASSIFIER = bINDING_SITE_CLASSIFIER;
	}

	public static void setCLASSIFIER_BLOSUM_MATRIX(BlosumMatrixName cLASSIFIER_BLOSUM_MATRIX) {
		CLASSIFIER_BLOSUM_MATRIX = cLASSIFIER_BLOSUM_MATRIX;
	}

	public static void setCUTOFF_PERCENTAGE(double cUTOFF_PERCENTAGE) {
		CUTOFF_PERCENTAGE = cUTOFF_PERCENTAGE;
	}

	public static void setDISABLE_LOG(boolean dISABLE_LOG) {
		DISABLE_LOG = dISABLE_LOG;
	}

	public static void setDISABLE_LOG_FILE(boolean dISABLE_LOG_FILE) {
		DISABLE_LOG_FILE = dISABLE_LOG_FILE;
	}

	public static void setDOMAIN_DATA_3DID(boolean dOMAIN_DATA_3DID) {
		DOMAIN_DATA_3DID = dOMAIN_DATA_3DID;
	}

	public static void setDOMAIN_DATA_DOMINE(boolean dOMAIN_DATA_DOMINE) {
		DOMAIN_DATA_DOMINE = dOMAIN_DATA_DOMINE;
	}

	public static void setDOMAIN_DATA_IDDI(boolean dOMAIN_DATA_IDDI) {
		DOMAIN_DATA_IDDI = dOMAIN_DATA_IDDI;
	}

	public static void setDOMAIN_DATA_IPFAM(boolean dOMAIN_DATA_IPFAM) {
		DOMAIN_DATA_IPFAM = dOMAIN_DATA_IPFAM;
	}

	public static void setDOMAIN_DATA_NO_PREDICTIONS(boolean dOMAIN_DATA_NO_PREDICTIONS) {
		DOMAIN_DATA_NO_PREDICTIONS = dOMAIN_DATA_NO_PREDICTIONS;
	}

	public static void setLOCAL_MUTATION_DATA(boolean lOCAL_MUTATION_DATA) {
		LOCAL_MUTATION_DATA = lOCAL_MUTATION_DATA;
	}

	public static void setLOCAL_PROTEIN_DATA(boolean lOCAL_PROTEIN_DATA) {
		LOCAL_PROTEIN_DATA = lOCAL_PROTEIN_DATA;
	}

	private Settings() {
	}
}