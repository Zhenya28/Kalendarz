package group.com;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class App {

	static Scanner sc = new Scanner(System.in);
	static DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	public static void main(String[] args) throws Exception {

		if (args.length > 0 && args[0].equals("-gui")) {
			group.com.gui.AppGUI.main(args);
			return;
		}

		List<Kontakt> kontakty = new ArrayList<>();
		List<Zdarzenie> zdarzenia = new ArrayList<>();
		
		
		DbManager.odczytajZBazy(kontakty, zdarzenia);

		// Automatyczne kasowanie zdarzeń starszych niż bieżąca data (przeterminowane)
		int usunieteAuto = Zdarzenie.usunStarszeNiz(zdarzenia, LocalDateTime.now());
		if (usunieteAuto > 0)
			System.out.println("Automatycznie usunieto " + usunieteAuto + " przeterminowanych zdarzen.");

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				DbManager.zapiszDoBazy(kontakty, zdarzenia);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}));

		int wybor = -1;
		while (wybor != 0) {
			System.out.println("\n1. Dodaj kontakt");
			System.out.println("2. Wyswietl kontakty");
			System.out.println("3. Edytuj kontakt");
			System.out.println("4. Usun kontakt");
			System.out.println("5. Dodaj zdarzenie");
			System.out.println("6. Wyswietl zdarzenia");
			System.out.println("7. Edytuj zdarzenie");
			System.out.println("8. Usun zdarzenie");
			System.out.println("9. Filtruj zdarzenia po dacie");
			System.out.println("10. Sortuj kontakty");
			System.out.println("11. Sortuj zdarzenia");
			System.out.println("12. Zapisz do XML");
			System.out.println("13. Odczytaj z XML");
			System.out.println("14. Usun zdarzenia starsze niz data");
			System.out.println("0. Wyjdz");
			System.out.print("Wybor: ");

			try {
				wybor = Integer.parseInt(sc.nextLine().trim());
			} catch (NumberFormatException e) {
				continue;
			}

			switch (wybor) {
			case 1:
				kontakty.add(dodajKontakt());
				break;
			case 2:
				if (kontakty.isEmpty()) {
					System.out.println("Brak kontaktow.");
				} else {
					for (int i = 0; i < kontakty.size(); i++)
						System.out.println(i + ". " + kontakty.get(i));
				}
				break;
			case 3:
				if (kontakty.isEmpty()) {
					System.out.println("Brak kontaktow.");
					break;
				}
				for (int i = 0; i < kontakty.size(); i++)
					System.out.println(i + ". " + kontakty.get(i).getImie() + " " + kontakty.get(i).getNazwisko());
				System.out.print("Indeks: ");
				int idxE = Integer.parseInt(sc.nextLine().trim());
				if (idxE < 0 || idxE >= kontakty.size()) break;
				Kontakt ke = kontakty.get(idxE);
				System.out.print("Nowy email (" + ke.getEmail() + "): ");
				String ne = sc.nextLine().trim();
				if (!ne.isEmpty()) ke.setEmail(new Email(ne));
				System.out.print("Nowe imie (" + ke.getImie() + "): ");
				String ni = sc.nextLine().trim();
				if (!ni.isEmpty()) ke.setImie(ni);
				System.out.print("Nowe nazwisko (" + ke.getNazwisko() + "): ");
				String nn = sc.nextLine().trim();
				if (!nn.isEmpty()) ke.setNazwisko(nn);
				System.out.print("Nowy telefon (" + ke.getTelefon() + "): ");
				String nt = sc.nextLine().trim();
				if (!nt.isEmpty()) ke.setTelefon(nt);
				System.out.println("Zaktualizowano.");
				break;
			case 4:
				if (kontakty.isEmpty()) {
					System.out.println("Brak kontaktow.");
					break;
				}
				for (int i = 0; i < kontakty.size(); i++)
					System.out.println(i + ". " + kontakty.get(i).getImie() + " " + kontakty.get(i).getNazwisko());
				System.out.print("Indeks: ");
				int idxD = Integer.parseInt(sc.nextLine().trim());
				if (idxD < 0 || idxD >= kontakty.size()) break;
				kontakty.remove(idxD);
				System.out.println("Usunieto.");
				break;
			case 5:
				zdarzenia.add(dodajZdarzenie(kontakty));
				break;
			case 6:
				if (zdarzenia.isEmpty()) {
					System.out.println("Brak zdarzen.");
				} else {
					for (int i = 0; i < zdarzenia.size(); i++)
						System.out.println(i + ". " + zdarzenia.get(i));
				}
				break;
			case 7:
				if (zdarzenia.isEmpty()) {
					System.out.println("Brak zdarzen.");
					break;
				}
				for (int i = 0; i < zdarzenia.size(); i++)
					System.out.println(i + ". " + zdarzenia.get(i).getOpis());
				System.out.print("Indeks: ");
				int idxZE = Integer.parseInt(sc.nextLine().trim());
				if (idxZE < 0 || idxZE >= zdarzenia.size()) break;
				Zdarzenie ze = zdarzenia.get(idxZE);
				System.out.print("Nowy opis (" + ze.getOpis() + "): ");
				String no = sc.nextLine().trim();
				if (!no.isEmpty()) ze.setOpis(no);
				System.out.print("Nowa lokalizacja (" + ze.getLocation() + "): ");
				String nl = sc.nextLine().trim();
				if (!nl.isEmpty()) ze.setLocation(nl);
				System.out.print("Nowa data (" + ze.getData() + "): ");
				String nd = sc.nextLine().trim();
				if (!nd.isEmpty()) ze.setData(LocalDateTime.parse(nd, fmt));
				System.out.println("Zaktualizowano.");
				break;
			case 8:
				if (zdarzenia.isEmpty()) {
					System.out.println("Brak zdarzen.");
					break;
				}
				for (int i = 0; i < zdarzenia.size(); i++)
					System.out.println(i + ". " + zdarzenia.get(i).getOpis());
				System.out.print("Indeks: ");
				int idxZD = Integer.parseInt(sc.nextLine().trim());
				if (idxZD < 0 || idxZD >= zdarzenia.size()) break;
				zdarzenia.remove(idxZD);
				System.out.println("Usunieto.");
				break;
			case 9:
				System.out.print("Zdarzenia przed data (yyyy-MM-dd HH:mm): ");
				LocalDateTime filtr = LocalDateTime.parse(sc.nextLine().trim(), fmt);
				boolean found = false;
				for (Zdarzenie z : zdarzenia) {
					if (z.getData().isBefore(filtr)) {
						System.out.println(z);
						found = true;
					}
				}
				if (!found) System.out.println("Brak zdarzen przed ta data.");
				break;
			case 10:
				System.out.println("1. Po imieniu");
				System.out.println("2. Po emailu");
				System.out.print("Wybor: ");
				int sk = Integer.parseInt(sc.nextLine().trim());
				if (sk == 1) Collections.sort(kontakty, new KontaktPoImieniuComparator());
				else if (sk == 2) Collections.sort(kontakty, new KontaktPoEmailuComparator());
				System.out.println("Posortowano.");
				break;
			case 11:
				System.out.println("1. Po dacie");
				System.out.println("2. Po kontakcie");
				System.out.print("Wybor: ");
				int sz = Integer.parseInt(sc.nextLine().trim());
				if (sz == 1) Collections.sort(zdarzenia, new ZdarzeniePoDacieComparator());
				else if (sz == 2) Collections.sort(zdarzenia, new ZdarzeniePoKontakcieComparator());
				System.out.println("Posortowano.");
				break;
			case 12:
				System.out.print("Nazwa pliku (kalendarz.xml): ");
				String plikZ = sc.nextLine().trim();
				if (plikZ.isEmpty()) plikZ = "kalendarz.xml";
				XmlManager.zapisz(kontakty, zdarzenia, plikZ);
				System.out.println("Zapisano do " + plikZ);
				break;
			case 13:
				System.out.print("Nazwa pliku (kalendarz.xml): ");
				String plikO = sc.nextLine().trim();
				if (plikO.isEmpty()) plikO = "kalendarz.xml";
				kontakty.clear();
				zdarzenia.clear();
				XmlManager.odczytaj(kontakty, zdarzenia, plikO);
				System.out.println("Odczytano z " + plikO);
				break;
			case 14:
				System.out.print("Usun zdarzenia starsze niz (yyyy-MM-dd HH:mm): ");
				LocalDateTime granica = LocalDateTime.parse(sc.nextLine().trim(), fmt);
				int usuniete = Zdarzenie.usunStarszeNiz(zdarzenia, granica);
				System.out.println("Usunieto " + usuniete + " zdarzen.");
				break;
			case 0:
				break;
			default:
				System.out.println("Nieznana opcja.");
			}
		}

		sc.close();
	}

	static Kontakt dodajKontakt() {
		System.out.print("Email: ");
		String email = sc.nextLine();
		System.out.print("Imie: ");
		String imie = sc.nextLine();
		System.out.print("Nazwisko: ");
		String nazwisko = sc.nextLine();
		System.out.print("Telefon: ");
		String telefon = sc.nextLine();
		return new Kontakt(email, imie, nazwisko, telefon);
	}

	static Zdarzenie dodajZdarzenie(List<Kontakt> kontakty) throws Exception {
		System.out.print("Opis: ");
		String opis = sc.nextLine();
		System.out.print("Lokalizacja: ");
		String location = sc.nextLine();
		System.out.print("Data (yyyy-MM-dd HH:mm): ");
		LocalDateTime data = LocalDateTime.parse(sc.nextLine(), fmt);

		Zdarzenie z = new Zdarzenie(location, data, opis);

		if (!kontakty.isEmpty()) {
			for (int i = 0; i < kontakty.size(); i++)
				System.out.println(i + ". " + kontakty.get(i).getImie() + " " + kontakty.get(i).getNazwisko());
			System.out.println("Podaj indeksy uczestnikow (pusty = koniec):");
			while (true) {
				System.out.print("Indeks: ");
				String line = sc.nextLine().trim();
				if (line.isBlank()) break;
				int idx = Integer.parseInt(line);
				if (idx >= 0 && idx < kontakty.size())
					z.addKontakt(kontakty.get(idx));
			}
		}
		return z;
	}
}