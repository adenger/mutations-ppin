package framework;

public enum Consequence {
	MISSENSE, SYNOMYMOUS, NONSENSE, FRAMESHIFT;

	public static Consequence getConsequence(String sequenceOntology) {
		switch (sequenceOntology) {
		case "stop_gained":
			return Consequence.NONSENSE;
		case "frameshift_variant":
			return Consequence.FRAMESHIFT;
		case "missense_variant":
			return Consequence.MISSENSE;
		case "synonymous_variant":
			return Consequence.SYNOMYMOUS;
		default:
			return null;
		}
	}

	public String toSequenceOntology() {
		switch (this) {
		case NONSENSE:
			return "stop_gained";
		case FRAMESHIFT:
			return "frameshift_variant";
		case MISSENSE:
			return "missense_variant";
		case SYNOMYMOUS:
			return "synonymous_variant";
		default:
			return null;
		}
	}
}
