package group.com;

import java.util.ArrayList;
import java.util.List;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

public class Kontakt implements Comparable<Kontakt> {
	private int id; 
	private Email email;
	private String imie;
	private String nazwisko;
	private Phone telefon;
	
	private List<Zdarzenie> zdarzenia = new ArrayList<>();

	public Kontakt(String email, String imie, String nazwisko, String telefon) {
		this.email = new Email(email);
		this.imie = imie;
		this.nazwisko = nazwisko;
		this.telefon = new Phone(telefon);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getEmail() {
		return email.getValue();
	}

	public void setEmail(Email email) {
		this.email = email;
	}

	public String getImie() {
		return imie;
	}

	public void setImie(String imie) {
		this.imie = imie;
	}

	public String getNazwisko() {
		return nazwisko;
	}

	public void setNazwisko(String nazwisko) {
		this.nazwisko = nazwisko;
	}

	public String getTelefon() {
		return PhoneNumberUtil.getInstance().format(this.telefon.getPhone(), PhoneNumberUtil.PhoneNumberFormat.E164);
	}

	public void setTelefon(String telefon) {
		this.telefon = new Phone(telefon);
	}

	public List<Zdarzenie> getZdarzenia() {
		return zdarzenia;
	}

	public void setZdarzenia(List<Zdarzenie> zdarzenia) {
		this.zdarzenia = zdarzenia;
	}

	public void addZdarzenie(Zdarzenie z) {
		if (!this.zdarzenia.contains(z)) {
			this.zdarzenia.add(z);
			z.getKontakty().add(this);
		}
	}

	public int compareTo(Kontakt k) {
		return this.email.getValue().compareTo(k.getEmail());
	}

	@Override
	public String toString() {
		List<String> opisyZdarzen = new ArrayList<>();
		for (Zdarzenie z : zdarzenia)
			opisyZdarzen.add(z.getOpis());

		return "Kontakt[id=" + id + ", email=" + email.getValue() + ", imie=" + imie + ", nazwisko=" + nazwisko
				+ ", telefon=" + getTelefon() + ", zdarzenia=" + opisyZdarzen + "]";
	}
}