package group.com;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

	// ── KONTAKT CRUD ──────────────────────────────────────────────────────────

	public static List<Kontakt> getAllKontakt() throws Exception {
		List<Kontakt> result = new ArrayList<>();
		Connection conn = DriverManager.getConnection(URL);
		Statement stmt = conn.createStatement();

		Map<Integer, Kontakt> mapa = new HashMap<>();
		ResultSet rs = stmt.executeQuery("SELECT * FROM Kontakty");
		while (rs.next()) {
			Kontakt k = new Kontakt(rs.getString("email"), rs.getString("imie"), rs.getString("nazwisko"),
					rs.getString("telefon"));
			k.setId(rs.getInt("id"));
			mapa.put(k.getId(), k);
			result.add(k);
		}

		Map<Integer, Zdarzenie> mapaZdarzen = new HashMap<>();
		ResultSet rsZ = stmt.executeQuery("SELECT * FROM Zdarzenia");
		while (rsZ.next()) {
			Zdarzenie z = new Zdarzenie(rsZ.getString("location"), LocalDateTime.parse(rsZ.getString("data")),
					rsZ.getString("opis"));
			z.setId(rsZ.getInt("id"));
			mapaZdarzen.put(z.getId(), z);
		}

		ResultSet rsRel = stmt.executeQuery("SELECT * FROM Zdarzenia_Kontakty");
		while (rsRel.next()) {
			Zdarzenie z = mapaZdarzen.get(rsRel.getInt("zdarzenie_id"));
			Kontakt k = mapa.get(rsRel.getInt("kontakt_id"));
			if (z != null && k != null) {
				z.addKontakt(k);
			}
		}

		stmt.close();
		conn.close();
		return result;
	}

	public static Kontakt getByIdKontakt(int id) throws Exception {
		Connection conn = DriverManager.getConnection(URL);

		PreparedStatement ps = conn.prepareStatement("SELECT * FROM Kontakty WHERE id = ?");
		ps.setInt(1, id);
		ResultSet rs = ps.executeQuery();

		Kontakt k = null;
		if (rs.next()) {
			k = new Kontakt(rs.getString("email"), rs.getString("imie"), rs.getString("nazwisko"),
					rs.getString("telefon"));
			k.setId(rs.getInt("id"));
		}

		if (k != null) {
			Map<Integer, Zdarzenie> mapaZdarzen = new HashMap<>();
			Statement stmt = conn.createStatement();
			ResultSet rsZ = stmt.executeQuery("SELECT * FROM Zdarzenia");
			while (rsZ.next()) {
				Zdarzenie z = new Zdarzenie(rsZ.getString("location"), LocalDateTime.parse(rsZ.getString("data")),
						rsZ.getString("opis"));
				z.setId(rsZ.getInt("id"));
				mapaZdarzen.put(z.getId(), z);
			}

			PreparedStatement psRel = conn
					.prepareStatement("SELECT zdarzenie_id FROM Zdarzenia_Kontakty WHERE kontakt_id = ?");
			psRel.setInt(1, id);
			ResultSet rsRel = psRel.executeQuery();
			while (rsRel.next()) {
				Zdarzenie z = mapaZdarzen.get(rsRel.getInt("zdarzenie_id"));
				if (z != null) {
					z.addKontakt(k);
				}
			}

			stmt.close();
			psRel.close();
		}

		ps.close();
		conn.close();
		return k;
	}

	public static Kontakt createKontakt(Kontakt k) throws Exception {
		Connection conn = DriverManager.getConnection(URL);
		PreparedStatement ps = conn.prepareStatement(
				"INSERT INTO Kontakty (email, imie, nazwisko, telefon) VALUES (?, ?, ?, ?)",
				Statement.RETURN_GENERATED_KEYS);
		ps.setString(1, k.getEmail());
		ps.setString(2, k.getImie());
		ps.setString(3, k.getNazwisko());
		ps.setString(4, k.getTelefon());
		ps.executeUpdate();

		ResultSet keys = ps.getGeneratedKeys();
		if (keys.next()) {
			k.setId(keys.getInt(1));
		}

		ps.close();
		conn.close();
		return k;
	}

	public static boolean updateByIdKontakt(int id, Kontakt k) throws Exception {
		Connection conn = DriverManager.getConnection(URL);

		PreparedStatement ps = conn
				.prepareStatement("UPDATE Kontakty SET email = ?, imie = ?, nazwisko = ?, telefon = ? WHERE id = ?");
		ps.setString(1, k.getEmail());
		ps.setString(2, k.getImie());
		ps.setString(3, k.getNazwisko());
		ps.setString(4, k.getTelefon());
		ps.setInt(5, id);
		int updated = ps.executeUpdate();

		ps.close();
		conn.close();
		return updated > 0;
	}

	public static boolean deleteByIdKontakt(int id) throws Exception {
		Connection conn = DriverManager.getConnection(URL);

		PreparedStatement psRel = conn.prepareStatement("DELETE FROM Zdarzenia_Kontakty WHERE kontakt_id = ?");
		psRel.setInt(1, id);
		psRel.executeUpdate();

		PreparedStatement ps = conn.prepareStatement("DELETE FROM Kontakty WHERE id = ?");
		ps.setInt(1, id);
		int deleted = ps.executeUpdate();

		psRel.close();
		ps.close();
		conn.close();
		return deleted > 0;
	}

	// ── ZDARZENIE CRUD ────────────────────────────────────────────────────────

	public static List<Zdarzenie> getAllZdarzenie() throws Exception {
		List<Zdarzenie> result = new ArrayList<>();
		Connection conn = DriverManager.getConnection(URL);
		Statement stmt = conn.createStatement();

		Map<Integer, Zdarzenie> mapaZ = new HashMap<>();
		ResultSet rsZ = stmt.executeQuery("SELECT * FROM Zdarzenia");
		while (rsZ.next()) {
			Zdarzenie z = new Zdarzenie(rsZ.getString("location"), LocalDateTime.parse(rsZ.getString("data")),
					rsZ.getString("opis"));
			z.setId(rsZ.getInt("id"));
			mapaZ.put(z.getId(), z);
			result.add(z);
		}

		Map<Integer, Kontakt> mapaK = new HashMap<>();
		ResultSet rsK = stmt.executeQuery("SELECT * FROM Kontakty");
		while (rsK.next()) {
			Kontakt k = new Kontakt(rsK.getString("email"), rsK.getString("imie"), rsK.getString("nazwisko"),
					rsK.getString("telefon"));
			k.setId(rsK.getInt("id"));
			mapaK.put(k.getId(), k);
		}

		ResultSet rsRel = stmt.executeQuery("SELECT * FROM Zdarzenia_Kontakty");
		while (rsRel.next()) {
			Zdarzenie z = mapaZ.get(rsRel.getInt("zdarzenie_id"));
			Kontakt k = mapaK.get(rsRel.getInt("kontakt_id"));
			if (z != null && k != null) {
				z.addKontakt(k);
			}
		}

		stmt.close();
		conn.close();
		return result;
	}

	public static Zdarzenie getByIdZdarzenie(int id) throws Exception {
		Connection conn = DriverManager.getConnection(URL);

		PreparedStatement ps = conn.prepareStatement("SELECT * FROM Zdarzenia WHERE id = ?");
		ps.setInt(1, id);
		ResultSet rs = ps.executeQuery();

		Zdarzenie z = null;
		if (rs.next()) {
			z = new Zdarzenie(rs.getString("location"), LocalDateTime.parse(rs.getString("data")),
					rs.getString("opis"));
			z.setId(rs.getInt("id"));
		}
		if (z != null) {
			Map<Integer, Kontakt> mapaK = new HashMap<>();
			Statement stmt = conn.createStatement();
			ResultSet rsK = stmt.executeQuery("SELECT * FROM Kontakty");
			while (rsK.next()) {
				Kontakt k = new Kontakt(rsK.getString("email"), rsK.getString("imie"), rsK.getString("nazwisko"),
						rsK.getString("telefon"));
				k.setId(rsK.getInt("id"));
				mapaK.put(k.getId(), k);
			}

			PreparedStatement psRel = conn
					.prepareStatement("SELECT kontakt_id FROM Zdarzenia_Kontakty WHERE zdarzenie_id = ?");
			psRel.setInt(1, id);
			ResultSet rsRel = psRel.executeQuery();
			while (rsRel.next()) {
				Kontakt k = mapaK.get(rsRel.getInt("kontakt_id"));
				if (k != null) {
					z.addKontakt(k);
				}
			}

			stmt.close();
			psRel.close();
		}

		ps.close();
		conn.close();
		return z;
	}

	public static Zdarzenie createZdarzenie(Zdarzenie z) throws Exception {
		Connection conn = DriverManager.getConnection(URL);

		PreparedStatement ps = conn.prepareStatement("INSERT INTO Zdarzenia (location, data, opis) VALUES (?, ?, ?)",
				Statement.RETURN_GENERATED_KEYS);
		ps.setString(1, z.getLocation().toString());
		ps.setString(2, z.getData().toString());
		ps.setString(3, z.getOpis());
		ps.executeUpdate();

		ResultSet keys = ps.getGeneratedKeys();
		if (keys.next()) {
			z.setId(keys.getInt(1));
		}

		PreparedStatement psRel = conn
				.prepareStatement("INSERT INTO Zdarzenia_Kontakty (zdarzenie_id, kontakt_id) VALUES (?, ?)");
		for (Kontakt k : z.getKontakty()) {
			psRel.setInt(1, z.getId());
			psRel.setInt(2, k.getId());
			psRel.executeUpdate();
		}

		ps.close();
		psRel.close();
		conn.close();
		return z;
	}

	public static boolean updateByIdZdarzenie(int id, Zdarzenie z) throws Exception {
		Connection conn = DriverManager.getConnection(URL);

		PreparedStatement ps = conn
				.prepareStatement("UPDATE Zdarzenia SET location = ?, data = ?, opis = ? WHERE id = ?");
		ps.setString(1, z.getLocation().toString());
		ps.setString(2, z.getData().toString());
		ps.setString(3, z.getOpis());
		ps.setInt(4, id);
		int updated = ps.executeUpdate();

		if (updated > 0) {
			PreparedStatement psDel = conn.prepareStatement("DELETE FROM Zdarzenia_Kontakty WHERE zdarzenie_id = ?");
			psDel.setInt(1, id);
			psDel.executeUpdate();

			PreparedStatement psRel = conn
					.prepareStatement("INSERT INTO Zdarzenia_Kontakty (zdarzenie_id, kontakt_id) VALUES (?, ?)");
			for (Kontakt k : z.getKontakty()) {
				psRel.setInt(1, id);
				psRel.setInt(2, k.getId());
				psRel.executeUpdate();
			}

			psDel.close();
			psRel.close();
		}

		ps.close();
		conn.close();
		return updated > 0;
	}

	public static boolean deleteByIdZdarzenie(int id) throws Exception {
		Connection conn = DriverManager.getConnection(URL);

		PreparedStatement psRel = conn.prepareStatement("DELETE FROM Zdarzenia_Kontakty WHERE zdarzenie_id = ?");
		psRel.setInt(1, id);
		psRel.executeUpdate();

		PreparedStatement ps = conn.prepareStatement("DELETE FROM Zdarzenia WHERE id = ?");
		ps.setInt(1, id);
		int deleted = ps.executeUpdate();

		psRel.close();
		ps.close();
		conn.close();
		return deleted > 0;
	}
}
