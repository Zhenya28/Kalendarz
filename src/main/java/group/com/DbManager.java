package group.com;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DbManager {
	private static final String URL = "jdbc:sqlite:kalendarz.db";

	public static void odczytajZBazy(List<Kontakt> outKontakty, List<Zdarzenie> outZdarzenia) throws Exception {
		Connection conn = DriverManager.getConnection(URL);
		Statement stmt = conn.createStatement();

		Map<Integer, Kontakt> mapaKontaktow = new HashMap<>();
		Map<Integer, Zdarzenie> mapaZdarzen = new HashMap<>();

		ResultSet rsKontakty = stmt.executeQuery("SELECT * FROM Kontakty");
		while (rsKontakty.next()) {
			Kontakt k = new Kontakt(rsKontakty.getString("email"), rsKontakty.getString("imie"),
					rsKontakty.getString("nazwisko"), rsKontakty.getString("telefon"));
			k.setId(rsKontakty.getInt("id"));
			mapaKontaktow.put(k.getId(), k);
			outKontakty.add(k);
		}

		ResultSet rsZdarzenia = stmt.executeQuery("SELECT * FROM Zdarzenia");
		while (rsZdarzenia.next()) {
			Zdarzenie z = new Zdarzenie(rsZdarzenia.getString("location"),
					LocalDateTime.parse(rsZdarzenia.getString("data")), rsZdarzenia.getString("opis"));
			z.setId(rsZdarzenia.getInt("id"));
			mapaZdarzen.put(z.getId(), z);
			outZdarzenia.add(z);
		}

		ResultSet rsRelacje = stmt.executeQuery("SELECT * FROM Zdarzenia_Kontakty");
		while (rsRelacje.next()) {
			Zdarzenie z = mapaZdarzen.get(rsRelacje.getInt("zdarzenie_id"));
			Kontakt k = mapaKontaktow.get(rsRelacje.getInt("kontakt_id"));
			if (z != null && k != null) {
				z.addKontakt(k);
			}
		}

		stmt.close();
		conn.close();
	}

	public static void zapiszDoBazy(List<Kontakt> kontakty, List<Zdarzenie> zdarzenia) throws Exception {
		Connection conn = DriverManager.getConnection(URL);
		Statement stmt = conn.createStatement();

		stmt.execute("DELETE FROM Zdarzenia_Kontakty");
		stmt.execute("DELETE FROM Zdarzenia");
		stmt.execute("DELETE FROM Kontakty");

		PreparedStatement psKontakt = conn.prepareStatement(
				"INSERT INTO Kontakty (id, email, imie, nazwisko, telefon) VALUES (?, ?, ?, ?, ?)",
				Statement.RETURN_GENERATED_KEYS);

		for (Kontakt k : kontakty) {
			if (k.getId() == 0)
				psKontakt.setNull(1, java.sql.Types.INTEGER);
			else
				psKontakt.setInt(1, k.getId());

			psKontakt.setString(2, k.getEmail());
			psKontakt.setString(3, k.getImie());
			psKontakt.setString(4, k.getNazwisko());
			psKontakt.setString(5, k.getTelefon());
			psKontakt.executeUpdate();

			if (k.getId() == 0) {
				ResultSet keys = psKontakt.getGeneratedKeys();
				if (keys.next())
					k.setId(keys.getInt(1));
			}
		}

		PreparedStatement psZdarzenie = conn.prepareStatement(
				"INSERT INTO Zdarzenia (id, location, data, opis) VALUES (?, ?, ?, ?)",
				Statement.RETURN_GENERATED_KEYS);
		PreparedStatement psRelacja = conn
				.prepareStatement("INSERT INTO Zdarzenia_Kontakty (zdarzenie_id, kontakt_id) VALUES (?, ?)");

		for (Zdarzenie z : zdarzenia) {
			if (z.getId() == 0)
				psZdarzenie.setNull(1, java.sql.Types.INTEGER);
			else
				psZdarzenie.setInt(1, z.getId());

			psZdarzenie.setString(2, z.getLocation().toString());
			psZdarzenie.setString(3, z.getData().toString());
			psZdarzenie.setString(4, z.getOpis());
			psZdarzenie.executeUpdate();

			if (z.getId() == 0) {
				ResultSet keys = psZdarzenie.getGeneratedKeys();
				if (keys.next())
					z.setId(keys.getInt(1));
			}

			for (Kontakt uczestnik : z.getKontakty()) {
				psRelacja.setInt(1, z.getId());
				psRelacja.setInt(2, uczestnik.getId());
				psRelacja.executeUpdate();
			}
		}

		stmt.close();
		psKontakt.close();
		psZdarzenie.close();
		psRelacja.close();
		conn.close();
	}
}