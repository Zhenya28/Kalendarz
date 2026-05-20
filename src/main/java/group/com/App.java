package group.com;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class App {

    static List<Kontakt> kontakty = new ArrayList<>();
    static List<Zdarzenie> zdarzenia = new ArrayList<>();
    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) throws Exception {
        System.out.println("--- Inicjalizacja programu ---");
        DbManager.odczytajZBazy(kontakty, zdarzenia);
        System.out.println("Wczytano " + kontakty.size() + " kontaktów i " + zdarzenia.size() + " zdarzeń z bazy.");

        boolean running = true;
        while (running) {
            System.out.println("\n=== MENU GŁÓWNE ===");
            System.out.println("1. Kontakty");
            System.out.println("2. Zdarzenia");
            System.out.println("3. Zapisz snapshot do XML");
            System.out.println("4. Wczytaj snapshot z XML");
            System.out.println("0. Wyjdź (zapisz do bazy)");
            System.out.print("> ");
            switch (sc.nextLine().trim()) {
                case "1": menuKontakty(); break;
                case "2": menuZdarzenia(); break;
                case "3":
                    XmlManager.zapisz(kontakty, zdarzenia, "kalendarz.xml");
                    System.out.println("Zapisano do kalendarz.xml");
                    break;
                case "4":
                    XmlManager.odczytaj(kontakty, zdarzenia, "kalendarz.xml");
                    System.out.println("Wczytano z kalendarz.xml (" + zdarzenia.size() + " zdarzeń)");
                    break;
                case "0": running = false; break;
                default: System.out.println("Nieznana opcja.");
            }
        }

        System.out.println("\n--- Zamykanie programu ---");
        DbManager.zapiszDoBazy(kontakty, zdarzenia);
        System.out.println("Dane zapisane do bazy. Do widzenia.");
    }

    // ─── KONTAKTY ────────────────────────────────────────────────────────────────

    static void menuKontakty() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- KONTAKTY ---");
            System.out.println("1. Wyświetl wszystkie");
            System.out.println("2. Dodaj kontakt");
            System.out.println("3. Edytuj kontakt");
            System.out.println("4. Usuń kontakt");
            System.out.println("0. Wstecz");
            System.out.print("> ");
            switch (sc.nextLine().trim()) {
                case "1": wyswietlKontakty(); break;
                case "2": dodajKontakt(); break;
                case "3": edytujKontakt(); break;
                case "4": usunKontakt(); break;
                case "0": back = true; break;
                default: System.out.println("Nieznana opcja.");
            }
        }
    }

    static void wyswietlKontakty() {
        if (kontakty.isEmpty()) { System.out.println("Brak kontaktów."); return; }
        Collections.sort(kontakty);
        for (int i = 0; i < kontakty.size(); i++) {
            Kontakt k = kontakty.get(i);
            List<String> opisyZdarzen = new ArrayList<>();
            for (Zdarzenie z : k.getZdarzenia()) opisyZdarzen.add(z.getOpis());
            System.out.printf("[%d] %s %s | %s | %s | zdarzenia: %s%n",
                i + 1, k.getImie(), k.getNazwisko(), k.getEmail(), k.getTelefon(), opisyZdarzen);
        }
    }

    static void dodajKontakt() {
        System.out.print("Imię: ");        String imie     = sc.nextLine().trim();
        System.out.print("Nazwisko: ");    String nazwisko = sc.nextLine().trim();
        System.out.print("Email: ");       String email    = sc.nextLine().trim();
        System.out.print("Telefon (np. 501234567): "); String tel = sc.nextLine().trim();
        try {
            kontakty.add(new Kontakt(email, imie, nazwisko, tel));
            System.out.println("Dodano kontakt.");
        } catch (Exception e) {
            System.out.println("Błąd: " + e.getMessage());
        }
    }

    static void edytujKontakt() {
        wyswietlKontakty();
        if (kontakty.isEmpty()) return;
        Kontakt k = wybierzKontakt();
        if (k == null) return;

        System.out.print("Nowe imię [" + k.getImie() + "]: ");
        String v = sc.nextLine().trim();
        if (!v.isEmpty()) k.setImie(v);

        System.out.print("Nowe nazwisko [" + k.getNazwisko() + "]: ");
        v = sc.nextLine().trim();
        if (!v.isEmpty()) k.setNazwisko(v);

        System.out.print("Nowy email [" + k.getEmail() + "]: ");
        v = sc.nextLine().trim();
        if (!v.isEmpty()) try { k.setEmail(new Email(v)); } catch (Exception e) { System.out.println("Błąd email: " + e.getMessage()); }

        System.out.print("Nowy telefon [" + k.getTelefon() + "]: ");
        v = sc.nextLine().trim();
        if (!v.isEmpty()) k.setTelefon(v);

        System.out.println("Zaktualizowano.");
    }

    static void usunKontakt() {
        wyswietlKontakty();
        if (kontakty.isEmpty()) return;
        Kontakt k = wybierzKontakt();
        if (k == null) return;
        for (Zdarzenie z : k.getZdarzenia()) z.getKontakty().remove(k);
        kontakty.remove(k);
        System.out.println("Usunięto kontakt.");
    }

    static Kontakt wybierzKontakt() {
        System.out.print("Numer kontaktu: ");
        try {
            int idx = Integer.parseInt(sc.nextLine().trim()) - 1;
            if (idx < 0 || idx >= kontakty.size()) { System.out.println("Zły numer."); return null; }
            return kontakty.get(idx);
        } catch (NumberFormatException e) { System.out.println("Zły numer."); return null; }
    }

    // ─── ZDARZENIA ───────────────────────────────────────────────────────────────

    static void menuZdarzenia() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- ZDARZENIA ---");
            System.out.println("1. Wyświetl wszystkie");
            System.out.println("2. Dodaj zdarzenie");
            System.out.println("3. Edytuj zdarzenie");
            System.out.println("4. Usuń zdarzenie");
            System.out.println("5. Usuń zdarzenia starsze niż data");
            System.out.println("0. Wstecz");
            System.out.print("> ");
            switch (sc.nextLine().trim()) {
                case "1": wyswietlZdarzenia(); break;
                case "2": dodajZdarzenie(); break;
                case "3": edytujZdarzenie(); break;
                case "4": usunZdarzenie(); break;
                case "5": usunStareZdarzenia(); break;
                case "0": back = true; break;
                default: System.out.println("Nieznana opcja.");
            }
        }
    }

    static void wyswietlZdarzenia() {
        if (zdarzenia.isEmpty()) { System.out.println("Brak zdarzeń."); return; }
        Collections.sort(zdarzenia, new ZdarzeniePoDacieComparator());
        for (int i = 0; i < zdarzenia.size(); i++) {
            Zdarzenie z = zdarzenia.get(i);
            List<String> nazwiska = new ArrayList<>();
            for (Kontakt k : z.getKontakty()) nazwiska.add(k.getNazwisko());
            System.out.printf("[%d] %s | %s | %s | uczestnicy: %s%n",
                i + 1, z.getData(), z.getOpis(), z.getLocation(), nazwiska);
        }
    }

    static void dodajZdarzenie() {
        System.out.print("Opis: ");
        String opis = sc.nextLine().trim();

        System.out.print("Data (yyyy-MM-ddTHH:mm, np. 2026-06-15T10:00): ");
        LocalDateTime data;
        try {
            data = LocalDateTime.parse(sc.nextLine().trim());
        } catch (DateTimeParseException e) {
            System.out.println("Zły format daty."); return;
        }

        System.out.print("Lokalizacja URI (np. https://maps.example.com/biuro): ");
        String loc = sc.nextLine().trim();

        Zdarzenie z;
        try {
            z = new Zdarzenie(loc, data, opis);
        } catch (Exception e) {
            System.out.println("Błąd: " + e.getMessage()); return;
        }

        if (!kontakty.isEmpty()) {
            System.out.println("Dodaj uczestników (0 = zakończ):");
            wyswietlKontakty();
            while (true) {
                System.out.print("Numer kontaktu: ");
                String line = sc.nextLine().trim();
                if (line.equals("0") || line.isEmpty()) break;
                try {
                    int idx = Integer.parseInt(line) - 1;
                    if (idx >= 0 && idx < kontakty.size()) {
                        z.addKontakt(kontakty.get(idx));
                        System.out.println("Dodano: " + kontakty.get(idx).getNazwisko());
                    } else {
                        System.out.println("Zły numer.");
                    }
                } catch (NumberFormatException ignored) {}
            }
        }

        zdarzenia.add(z);
        System.out.println("Dodano zdarzenie.");
    }

    static void edytujZdarzenie() {
        wyswietlZdarzenia();
        if (zdarzenia.isEmpty()) return;
        Zdarzenie z = wybierzZdarzenie();
        if (z == null) return;

        System.out.print("Nowy opis [" + z.getOpis() + "]: ");
        String v = sc.nextLine().trim();
        if (!v.isEmpty()) z.setOpis(v);

        System.out.print("Nowa data [" + z.getData() + "]: ");
        v = sc.nextLine().trim();
        if (!v.isEmpty()) try { z.setData(LocalDateTime.parse(v)); } catch (DateTimeParseException e) { System.out.println("Zły format, pominięto."); }

        System.out.print("Nowa lokalizacja [" + z.getLocation() + "]: ");
        v = sc.nextLine().trim();
        if (!v.isEmpty()) try { z.setLocation(v); } catch (Exception e) { System.out.println("Błąd URI, pominięto."); }

        System.out.println("Zaktualizowano.");
    }

    static void usunZdarzenie() {
        wyswietlZdarzenia();
        if (zdarzenia.isEmpty()) return;
        Zdarzenie z = wybierzZdarzenie();
        if (z == null) return;
        for (Kontakt k : z.getKontakty()) k.getZdarzenia().remove(z);
        zdarzenia.remove(z);
        System.out.println("Usunięto zdarzenie.");
    }

    static void usunStareZdarzenia() {
        System.out.print("Usuń zdarzenia starsze niż (yyyy-MM-dd): ");
        try {
            LocalDateTime granica = LocalDate.parse(sc.nextLine().trim()).atStartOfDay();
            List<Zdarzenie> doUsuniecia = new ArrayList<>();
            for (Zdarzenie z : zdarzenia)
                if (z.getData().isBefore(granica)) doUsuniecia.add(z);
            for (Zdarzenie z : doUsuniecia) {
                for (Kontakt k : z.getKontakty()) k.getZdarzenia().remove(z);
                zdarzenia.remove(z);
            }
            System.out.println("Usunięto " + doUsuniecia.size() + " zdarzeń.");
        } catch (DateTimeParseException e) {
            System.out.println("Zły format daty.");
        }
    }

    static Zdarzenie wybierzZdarzenie() {
        System.out.print("Numer zdarzenia: ");
        try {
            int idx = Integer.parseInt(sc.nextLine().trim()) - 1;
            if (idx < 0 || idx >= zdarzenia.size()) { System.out.println("Zły numer."); return null; }
            return zdarzenia.get(idx);
        } catch (NumberFormatException e) { System.out.println("Zły numer."); return null; }
    }
}
