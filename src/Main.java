import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

import org.json.JSONObject;

public class Main {
	private static int MAX_ID = 2200; //highest card ID that exists, inclusive

	private static String getCardNameFromID(int id) {
		String url = "https://towerofsaviors.fandom.com/wiki/"; //concatenate the ID to the end of this URL, then fetch the title of the webpage for the card name.

		url += padZeroes(id);

		InputStream response = null;
		try {
			response = new URL(url).openStream();
			Scanner scanner = new Scanner(response, "utf-8");
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
	
	public static void storeInJSON() {
		JSONObject jsonObject = new JSONObject();
		for (int i = 1; i <= MAX_ID; i++) {
			System.out.println("[JSON] Looking up card number " + i);
			jsonObject = jsonObject.put(padZeroes(i), getCardNameFromID(i));
			System.out.println("[JSON] Processed card \"" + getCardNameFromID(i) + "\" (number " + i + ")");
		}

		try (Writer writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream("cards.json"), "utf-8"))) {
			writer.write(jsonObject.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void storeInSQL() throws ClassNotFoundException, SQLException {
		Connection conn = null;
		Statement stmt = null;
		
		Class.forName("org.sqlite.JDBC");
		conn = DriverManager.getConnection("jdbc:sqlite:cards.db");
		stmt = conn.createStatement();
		
		String sql = "CREATE TABLE IF NOT EXISTS Cards " +
				"(ID INTEGER PRIMARY KEY AUTOINCREMENT     NOT NULL," +
				" CardID          INTEGER    NOT NULL," +
				" CardName        TEXT       NOT NULL)";
		stmt.executeUpdate(sql);
		
		for (int i = 1; i <= MAX_ID; i++) {
			System.out.println("[SQL] Looking up card number " + i);
			String cardName = getCardNameFromID(i).replaceAll("'", "''");
			sql = "INSERT INTO Cards (CardID, CardName) VALUES (" + padZeroes(i) + ", '" + cardName + "');";
			stmt.executeUpdate(sql);
			System.out.println("[SQL] Processed card \"" + getCardNameFromID(i) + "\" (number " + i + ")");
		}
		
		stmt.close();
	}

	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		if (args.length != 2){
			System.out.println("Usage: java -jar carddata.jar <max_card_id> <json/sql>");
		} else {
			MAX_ID = Integer.parseInt(args[0]);
			String instruction = args[1].toLowerCase();
			if (instruction.equals("json")) {
				storeInJSON();
			} else if (instruction.equals("sql")) {
				storeInSQL();
			} else {
				System.out.println("Usage: java -jar carddata.jar <max_card_id> <json/sql>");
			}
		}
	}
}
