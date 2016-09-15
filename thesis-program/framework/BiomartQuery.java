package framework;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;

public final class BiomartQuery {

	public synchronized static String getBiomartFasta(String xmlQuery) {
		StringBuilder sequenceBuilder = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(getStream(xmlQuery)))) {
			int val = 0;
			while ((val = reader.read()) != -1) {
				sequenceBuilder.append((char) val);
			}
		} catch (IOException e) {
			System.out.println("not able to reach biomart service, try again later");
			e.printStackTrace();
		}
		return sequenceBuilder.toString();
	}

	// returning a list so I don't have to use arrays.deepequals
	public synchronized static List<String[]> sendBiomartQuery(String xmlQuery) {
		List<String[]> results = new LinkedList<String[]>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(getStream(xmlQuery)))) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] result = line.trim().split("\t");
				results.add(result);
			}
		} catch (IOException e) {
			System.out.println("not able to reach biomart service, try again later");
			e.printStackTrace();
		}
		return results;
	}

	private synchronized static InputStream getStream(String xmlQuery) throws IOException {
		String body = "query=" + URLEncoder.encode(xmlQuery, "UTF-8");
		URL url = new URL("http://www.ensembl.org/biomart/martservice");
		// URL backup = new URL("http://www.biomart.org/biomart/martservice");
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("POST");
		connection.setDoInput(true);
		connection.setDoOutput(true);
		connection.setUseCaches(false);
		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		connection.setRequestProperty("Content-Length", String.valueOf(body.length()));
		try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream())) {
			writer.write(body);
			writer.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return connection.getInputStream();
	}

	private BiomartQuery() {
	}
}
