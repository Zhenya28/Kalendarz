package group.com;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class App {
	public static void main(String[] args) throws Exception {
		List<Kontakt> kontakty = new ArrayList<>();
		List<Zdarzenie> zdarzenia = new ArrayList<>();

		System.out.println("--- Inicjalizacja programu ---");
		DbManager.odczytajZBazy(kontakty, zdarzenia);

		if (kontakty.isEmpty()) {
			System.out.println("Baza danych jest pusta. Generowanie danych startowych...");

			kontakty.add(new Kontakt("rafal.gorski@example.com", "Rafal", "Gorski", "501234567"));
			kontakty.add(new Kontakt("weronika.sikora@example.com", "Weronika", "Sikora", "602345678"));
			kontakty.add(new Kontakt("bartosz.pietrzak@example.com", "Bartosz", "Pietrzak", "703456789"));
			kontakty.add(new Kontakt("olga.stepien@example.com", "Olga", "Stepien", "504567890"));
			kontakty.add(new Kontakt("k.majewski@example.com", "Krzysztof", "Majewski", "605678901"));
			kontakty.add(new Kontakt("natalia.baran@example.com", "Natalia", "Baran", "706789012"));
			kontakty.add(new Kontakt("dominik.wrobel@example.com", "Dominik", "Wrobel", "507890123"));
			kontakty.add(new Kontakt("sylwia.chmiel@example.com", "Sylwia", "Chmiel", "608901234"));
			kontakty.add(new Kontakt("pawel.szymczak@example.com", "Pawel", "Szymczak", "709012345"));
			kontakty.add(new Kontakt("iwona.krawczyk@example.com", "Iwona", "Krawczyk", "500123456"));

			Zdarzenie z1 = new Zdarzenie("https://maps.example.com/dentysta", LocalDateTime.of(2026, 1, 14, 10, 0),
					"Wizyta u dentysty");
			z1.addKontakt(kontakty.get(3));
			zdarzenia.add(z1);

			Zdarzenie z2 = new Zdarzenie("https://maps.example.com/kawiarnia", LocalDateTime.of(2026, 2, 8, 18, 0),
					"Urodziny Bartka");
			z2.addKontakt(kontakty.get(2));
			zdarzenia.add(z2);

			Zdarzenie z3 = new Zdarzenie("https://maps.example.com/stodola", LocalDateTime.of(2026, 5, 21, 20, 0),
					"Koncert w Stodole");
			z3.addKontakt(kontakty.get(9));
			z3.addKontakt(kontakty.get(0));
			zdarzenia.add(z3);

			DbManager.zapiszDoBazy(kontakty, zdarzenia);
			System.out.println("Dane startowe zapisane do bazy. ID zostały wygenerowane.");
		}

		Collections.sort(kontakty);
		System.out.println("\n--- Kontakty w pamięci RAM (posortowane po email) ---");
		for (Kontakt k : kontakty) {
			System.out.println(k);
		}

		Collections.sort(zdarzenia, new ZdarzeniePoKontakcieComparator());
		System.out.println("\n--- Zdarzenia w pamięci RAM (posortowane po pierwszym kontakcie) ---");
		for (Zdarzenie z : zdarzenia) {
			System.out.println(z);
		}

		System.out.println("\n--- Operacje na pliku XML ---");
		XmlManager.zapisz(kontakty, zdarzenia, "kalendarz.xml");
		System.out.println("Dane tymczasowe zapisane do kalendarz.xml");

		List<Kontakt> kontaktyXml = new ArrayList<>();
		List<Zdarzenie> zdarzeniaXml = new ArrayList<>();
		XmlManager.odczytaj(kontaktyXml, zdarzeniaXml, "kalendarz.xml");
		System.out.println("Dane pomyślnie odczytane z XML (liczba zdarzeń: " + zdarzeniaXml.size() + ")");

		// 5. ЗАВЕРШЕНИЕ РАБОТЫ
		System.out.println("\n--- Zamykanie programu ---");
		DbManager.zapiszDoBazy(kontakty, zdarzenia);
		System.out.println("Wszystkie zmiany zostały zsynchronizowane z bazą danych SQL Lite.");
	}
}