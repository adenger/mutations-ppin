package framework;

public enum ClassifierScore {
	PROPERTIES, BLOSUM, BLOSUM_PERCENTAGE, POLYPHEN_2_HC, POLYPHEN_2, POLYPHEN_2_SCORE, SIFT_HC, SIFT, SIFT_SCORE, NULL, HOTSPOT_ENRICHED, PSHC, HPSHC;
	public static ClassifierScore get(String string) {
		switch (string) {
		case "properties":
			return ClassifierScore.PROPERTIES;
		case "blosum":
			return BLOSUM;
		case "polyphen2hc":
			return ClassifierScore.POLYPHEN_2_HC;
		case "polyphen2":
			return ClassifierScore.POLYPHEN_2;
		case "sifthc":
			return SIFT_HC;
		case "sift":
			return ClassifierScore.SIFT;
		case "null":
			return NULL;
		case "hotspot":
			return HOTSPOT_ENRICHED;
		case "pshc":
			return PSHC;
		case "hpshc":
			return ClassifierScore.HPSHC;
		default:
			return null;
		}
	}

	public boolean isBinaryScore() {
		switch (this) {
		case BLOSUM:
			return true;
		case BLOSUM_PERCENTAGE:
			return false;
		case HOTSPOT_ENRICHED:
			return true;
		case NULL:
			return true;
		case POLYPHEN_2:
			return true;
		case POLYPHEN_2_HC:
			return true;
		case POLYPHEN_2_SCORE:
			return false;
		case PROPERTIES:
			return true;
		case SIFT:
			return true;
		case SIFT_HC:
			return true;
		case SIFT_SCORE:
			return false;
		case PSHC:
			return true;
		case HPSHC:
			return true;
		default:
			return true;
		}
	}
}
