package framework;

import java.util.HashSet;
import java.util.Set;

public class AminoAcidProperties {
	private static Set<Character> hydrophobic = newHashSet(new Character[] { 'V', 'I', 'L', 'F', 'W', 'Y', 'M' });
	private static Set<Character> aromatic = newHashSet(new Character[] { 'F', 'W', 'Y' });
	private static Set<Character> aliphatic = newHashSet(new Character[] { 'V', 'I', 'L', 'M' });
	private static Set<Character> small = newHashSet(new Character[] { 'P', 'G', 'A', 'S' });
	private static Set<Character> hydrophilic = newHashSet(
			new Character[] { 'S', 'T', 'H', 'N', 'Q', 'E', 'D', 'K', 'R' });
	private static Set<Character> positive = newHashSet(new Character[] { 'K', 'R' });
	private static Set<Character> negative = newHashSet(new Character[] { 'D', 'E' });
	private static Set<Character> hotspotEnriched = newHashSet(
			new Character[] { 'R', 'W', 'Y', 'K', 'H', 'D', 'I', 'P' });

	public static boolean isAliphatic(char aa) {
		return aliphatic.contains(aa);
	}

	public static boolean isAromatic(char aa) {
		return aromatic.contains(aa);
	}

	public static boolean isCharged(char aa) {
		return isNegative(aa) || isPositive(aa);
	}

	// Bogan and Thorn 1998
	public static boolean isHotspotEnriched(char aa) {
		return hotspotEnriched.contains(aa);
	}

	public static boolean isHydrophilic(char aa) {
		return hydrophilic.contains(aa);
	}

	public static boolean isHydrophobic(char aa) {
		return hydrophobic.contains(aa);
	}

	public static boolean isNegative(char aa) {
		return negative.contains(aa);
	}

	public static boolean isPositive(char aa) {
		return positive.contains(aa);
	}

	public static boolean isSmall(char aa) {
		return small.contains(aa);
	}

	private static Set<Character> newHashSet(Character... aminoAcids) {
		Set<Character> newSet = new HashSet<Character>();
		for (Character aa : aminoAcids) {
			newSet.add(aa);
		}
		return newSet;
	}

	private AminoAcidProperties() {

	}
}
