package group.com;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Zdarzenie implements Comparable<Zdarzenie> {
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
	public int compareTo(Zdarzenie z) {
		return this.data.compareTo(z.getData());
	}

	/**
	 * Usuwa z listy wszystkie zdarzenia wcześniejsze niż podana granica
	 * i odpina je od powiązanych kontaktów. Zwraca liczbę usuniętych zdarzeń.
	 */
	public static int usunStarszeNiz(List<Zdarzenie> zdarzenia, LocalDateTime granica) {
		int przed = zdarzenia.size();
		Iterator<Zdarzenie> it = zdarzenia.iterator();
		while (it.hasNext()) {
			Zdarzenie z = it.next();
			if (z.getData().isBefore(granica)) {
				for (Kontakt k : z.getKontakty()) {
					k.getZdarzenia().remove(z);
				}
				it.remove();
			}
		}
		return przed - zdarzenia.size();
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