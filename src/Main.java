import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;
import java.util.Scanner;

import org.json.JSONObject;

public class Main {
	private static String getCardNameFromID(int id) {
		String url = "http://towerofsaviors.wikia.com/wiki/"; //concatenate the ID to the end of this URL, then fetch the title of the webpage for the card name.

		url += padZeroes(id);

		InputStream response = null;
		try {
			response = new URL(url).openStream();
			Scanner scanner = new Scanner(response);
			String responseBody = scanner.useDelimiter("\\A").next();
			String cardName = responseBody.substring(responseBody.indexOf("<title>") + 7, responseBody.indexOf("</title>"));
			cardName = cardName.substring(0, cardName.indexOf('|') - 1);
			scanner.close();
			return cardName;
		} catch (IOException e) {
			System.out.println("Card ID " + padZeroes(id) + " does not exist; skipping card.");
		} finally {
			try {
				response.close();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				// do nothing
			}
		}
		return null;
	}

	private static String padZeroes(int id) {
		if (id < 10) {
			return "00" + id;
		} else if (id >= 10 && id < 100) {
			return "0" + id;
		} else {
			return Integer.toString(id);
		}
	}

	public static void main(String[] args) {
		JSONObject jsonObject = new JSONObject();
		for (int i = 1; i <= 1718; i++) { //max 1718 inclusive
			System.out.println("Looking up card number " + i);
			jsonObject = jsonObject.put(padZeroes(i), getCardNameFromID(i));
			System.out.println("Added card number " + i);
		}

		try (Writer writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream("cards.json"), "utf-8"))) {
			writer.write(jsonObject.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
