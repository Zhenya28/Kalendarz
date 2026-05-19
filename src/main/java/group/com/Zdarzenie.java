package group.com;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Zdarzenie {
	private int id;
	private LocalDateTime data;
	private String opis;
	private URI location;

	private List<Kontakt> kontakty = new ArrayList<>();

	public Zdarzenie(String location, LocalDateTime data, String opis) {
		this.location = URI.create(location);
		this.data = data;
		this.opis = opis;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public URI getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = URI.create(location);
	}

	public LocalDateTime getData() {
		return data;
	}

	public void setData(LocalDateTime data) {
		this.data = data;
	}

	public String getOpis() {
		return opis;
	}

	public void setOpis(String opis) {
		this.opis = opis;
	}

	public List<Kontakt> getKontakty() {
		return kontakty;
	}

	public void setKontakty(List<Kontakt> kontakty) {
		this.kontakty = kontakty;
	}

	public void addKontakt(Kontakt k) {
		if (!this.kontakty.contains(k)) {
			this.kontakty.add(k);
			k.getZdarzenia().add(this);
		}
	}

	@Override
	public String toString() {
		List<String> nazwiska = new ArrayList<>();
		for (Kontakt k : kontakty)
			nazwiska.add(k.getNazwisko());

		return "Zdarzenie[id=" + id + ", data=" + data + ", opis=" + opis + ", location=" + location + ", uczestnicy="
				+ nazwiska + "]";
	}
}