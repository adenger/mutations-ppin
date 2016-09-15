package framework;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Set;

public class BiomartXmlHandler {
	private final static String proteinsXMLFileName = "/xml/proteins.xml";
	private final static String uniprotIdXMLFileName = "/xml/uniprotids.xml";

	public static String getProteinsXML(Set<String> uniprotIDs) {
		return getXML(uniprotIDs, proteinsXMLFileName);
	}

	public static String getUniprotIDsXML(Set<String> geneIDs) {
		return getXML(geneIDs, uniprotIdXMLFileName);
	}

	private static String getXML(Set<String> iDs, String fileName) {
		String xml = readXML(fileName);
		String joinedIDs = joinIDs(iDs, ",");
		String newXML = xml.replace("INSERT", joinedIDs);
		return newXML;
	}

	private static String joinIDs(Set<String> iDs, String seperator) {
		StringBuilder builder = new StringBuilder();
		Iterator<String> iter = iDs.iterator();
		while (iter.hasNext()) {
			String id = iter.next();
			builder.append(id);
			if (iter.hasNext()) {
				builder.append(seperator);
			}
		}
		return builder.toString();
	}

	private static String readXML(String fileName) {
		StringBuilder builder = new StringBuilder();
		InputStream stream = BiomartXmlHandler.class.getResourceAsStream(fileName);
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
			int val = 0;
			while ((val = reader.read()) != -1) {
				builder.append((char) val);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return builder.toString();
	}

	private BiomartXmlHandler() {
	}
}
