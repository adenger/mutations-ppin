package framework;

public enum BlosumMatrixName {
	BLOSUM30, BLOSUM35, BLOSUM40, BLOSUM45, BLOSUM50, BLOSUM55, BLOSUM60, BLOSUM62, BLOSUM65, BLOSUM70, BLOSUM75, BLOSUM80, BLOSUM90, BLOSUM100;
	public static BlosumMatrixName get(int number) {
		switch (number) {
		case 30:
			return BLOSUM30;
		case 35:
			return BLOSUM35;
		case 40:
			return BLOSUM40;
		case 45:
			return BLOSUM45;
		case 50:
			return BLOSUM50;
		case 55:
			return BLOSUM55;
		case 60:
			return BLOSUM60;
		case 62:
			return BLOSUM62;
		case 65:
			return BLOSUM65;
		case 70:
			return BLOSUM70;
		case 75:
			return BLOSUM75;
		case 80:
			return BLOSUM80;
		case 90:
			return BLOSUM90;
		case 100:
			return BLOSUM100;
		default:
			return BLOSUM100;
		}
	}

	public String getPath() {
		String string = this.toString();
		int number = Integer.parseInt(string.substring(6, string.length()));
		return "/matrices/blosum" + number + ".bla";
	}
}
