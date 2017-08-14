package uk.ac.jisc.jats;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HTTPCall
{
	/**
	 * Performs a "GET" HTTP call to the given URL.
	 * @param urlString the URL
	 * @return a StringBuilder object containing the response
	 * @throws Exception
	 */
	public StringBuilder get(String urlString) throws Exception
	{
		URL url = new URL(urlString);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		// int responseCode = con.getResponseCode();
		// System.out.println("Response code: " + responseCode);
		BufferedReader in = new BufferedReader(new InputStreamReader(
				con.getInputStream()));
		String inputLine;
		StringBuilder response = new StringBuilder();
		while ((inputLine = in.readLine()) != null)
			response.append(inputLine);
		in.close();
		return response;
	}
}
