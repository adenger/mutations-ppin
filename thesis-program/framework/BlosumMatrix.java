package framework;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class BlosumMatrix {
	private int[][] table = new int[24][];
	private char[] index = { 'A', 'R', 'N', 'D', 'C', 'Q', 'E', 'G', 'H', 'I', 'L', 'K', 'M', 'F', 'P', 'S', 'T', 'W',
			'Y', 'V', 'B', 'Z', 'X', '*' };
	private BlosumMatrixName name;
	private int min = 1;

	public BlosumMatrix(BlosumMatrixName name) {
		this.name = name;
		this.read();
	}

	public int get(char first, char second) {
		int firstIndex = -1, secondIndex = -1;
		for (int i = 0; i < index.length; i++) {
			if (index[i] == first) {
				firstIndex = i;
			}
			if (index[i] == second) {
				secondIndex = i;
			}
		}
		return table[firstIndex][secondIndex];
	}

	public BlosumMatrixName getName() {
		return name;
	}

	public double getPercentage(char first, char second) {
		int score = get(first, second);
		if (score >= 0 || min >= 0)
			return 0;
		return (double) score / (double) min;
	}

	public void setName(BlosumMatrixName name) {
		this.name = name;
	}

	private void read() {
		String path = name.getPath();
		InputStream stream = BlosumMatrix.class.getResourceAsStream(path);
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
			String line = null;
			int currentColumn = 0;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.startsWith("#") || line.startsWith("A"))
					continue;
				String[] values = line.split("\\s+");
				int[] scores = new int[24];
				for (int i = 0; i < values.length; i++) {
					int score = Integer.parseInt(values[i]);
					scores[i] = score;
					if (score < min)
						min = score;
				}
				table[currentColumn] = scores;
				currentColumn++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
