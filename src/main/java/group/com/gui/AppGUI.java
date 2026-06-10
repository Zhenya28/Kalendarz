package group.com.gui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import group.com.DbManager;
import group.com.Kontakt;
import group.com.XmlManager;
import group.com.Zdarzenie;

public class AppGUI {

	private JFrame frame;
	private JTable tableKontakty;
	private JTable tableZdarzenia;
	private DefaultTableModel modelKontakty;
	private DefaultTableModel modelZdarzenia;

	private List<Kontakt> kontakty = new ArrayList<>();
	private List<Zdarzenie> zdarzenia = new ArrayList<>();

	private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
	// Gdy ustawione, tabela zdarzeń pokazuje tylko zdarzenia wcześniejsze niż ta data.
	private LocalDateTime filtrPrzedData = null;
	// Gdy ustawione (małymi literami), pokazywane są tylko zdarzenia zawierające ten tekst.
	private String filtrTekst = null;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					AppGUI window = new AppGUI();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public AppGUI() {
		initialize();
		loadData();
		registerSaveOnExit();
	}

	/**
	 * Register a shutdown hook that saves in-memory data back to the database
	 * when the application closes (connection is opened and closed automatically).
	 */
	private void registerSaveOnExit() {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				DbManager.zapiszDoBazy(kontakty, zdarzenia);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}));
	}

	/**
	 * Load data from database into tables.
	 */
	private void loadData() {
		try {
			DbManager.odczytajZBazy(kontakty, zdarzenia);
			// Automatyczne kasowanie zdarzeń starszych niż bieżąca data (przeterminowane)
			Zdarzenie.usunStarszeNiz(zdarzenia, LocalDateTime.now());
			refreshTableKontakty();
			refreshTableZdarzenia();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(frame, "Błąd odczytu z bazy:\n" + e.getMessage(), "Błąd",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Refresh contacts table from list.
	 */
	private void refreshTableKontakty() {
		modelKontakty.setRowCount(0);
		for (Kontakt k : kontakty) {
			modelKontakty.addRow(new Object[] { k.getId(), k.getImie(), k.getNazwisko(), k.getEmail(), k.getTelefon() });
		}
	}

	/**
	 * Refresh events table from list.
	 */
	private void refreshTableZdarzenia() {
		modelZdarzenia.setRowCount(0);
		for (Zdarzenie z : zdarzenia) {
			if (filtrPrzedData != null && !z.getData().isBefore(filtrPrzedData)) {
				continue;
			}
			List<String> nazwiska = new ArrayList<>();
			for (Kontakt k : z.getKontakty()) {
				nazwiska.add(k.getNazwisko());
			}
			String uczestnicy = String.join(", ", nazwiska);
			if (filtrTekst != null) {
				String haystack = (z.getOpis() + " " + z.getLocation() + " " + uczestnicy).toLowerCase();
				if (!haystack.contains(filtrTekst)) {
					continue;
				}
			}
			modelZdarzenia.addRow(new Object[] { z.getId(), z.getData().toString(), z.getOpis(),
					z.getLocation().toString(), uczestnicy });
		}
	}

	/**
	 * Returns the index of the selected contact in kontakty list, or -1 if none.
	 */
	private int getSelectedKontaktIndex() {
		int row = tableKontakty.getSelectedRow();
		if (row < 0) {
			return -1;
		}
		row = tableKontakty.convertRowIndexToModel(row); // uwzględnij sortowanie tabeli
		int id = (int) modelKontakty.getValueAt(row, 0);
		for (int i = 0; i < kontakty.size(); i++) {
			if (kontakty.get(i).getId() == id) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Returns the index of the selected event in zdarzenia list, or -1 if none.
	 */
	private int getSelectedZdarzenieIndex() {
		int row = tableZdarzenia.getSelectedRow();
		if (row < 0) {
			return -1;
		}
		row = tableZdarzenia.convertRowIndexToModel(row); // uwzględnij sortowanie tabeli
		int id = (int) modelZdarzenia.getValueAt(row, 0);
		for (int i = 0; i < zdarzenia.size(); i++) {
			if (zdarzenia.get(i).getId() == id) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 900, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("Kalendarz");

		// ── Menu Bar ──────────────────────────────────────────────────────────

		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);

		JMenu mnPlik = new JMenu("Plik");
		menuBar.add(mnPlik);

		JMenuItem mntmZapiszBaza = new JMenuItem("Zapisz do bazy danych");
		mntmZapiszBaza.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				zapiszDoBazy();
			}
		});
		mnPlik.add(mntmZapiszBaza);

		JMenuItem mntmOdczytajBaza = new JMenuItem("Odczytaj z bazy danych");
		mntmOdczytajBaza.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				odczytajZBazy();
			}
		});
		mnPlik.add(mntmOdczytajBaza);

		mnPlik.addSeparator();

		JMenuItem mntmZapiszXML = new JMenuItem("Zapisz do XML...");
		mntmZapiszXML.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				zapiszDoXML();
			}
		});
		mnPlik.add(mntmZapiszXML);

		JMenuItem mntmOdczytajXML = new JMenuItem("Odczytaj z XML...");
		mntmOdczytajXML.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				odczytajZXML();
			}
		});
		mnPlik.add(mntmOdczytajXML);

		mnPlik.addSeparator();

		JMenuItem mntmWyjdz = new JMenuItem("Wyjdź");
		mntmWyjdz.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
			}
		});
		mnPlik.add(mntmWyjdz);

		// ── Menu Kontakty ─────────────────────────────────────────────────────

		JMenu mnKontakty = new JMenu("Kontakty");
		menuBar.add(mnKontakty);

		JMenuItem mntmDodajKontakt = new JMenuItem("Dodaj kontakt...");
		mntmDodajKontakt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				KontaktDialog dialog = new KontaktDialog(frame, zdarzenia);
				dialog.setVisible(true);
				Kontakt nowyKontakt = dialog.getKontakt();
				if (nowyKontakt != null) {
					kontakty.add(nowyKontakt);
					refreshTableKontakty();
					refreshTableZdarzenia();
				}
			}
		});
		mnKontakty.add(mntmDodajKontakt);

		JMenuItem mntmEdytujKontakt = new JMenuItem("Edytuj kontakt...");
		mntmEdytujKontakt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int idx = getSelectedKontaktIndex();
				if (idx < 0) {
					JOptionPane.showMessageDialog(frame, "Wybierz kontakt z tabeli.", "Brak wyboru",
							JOptionPane.WARNING_MESSAGE);
					return;
				}
				KontaktDialog dialog = new KontaktDialog(frame, kontakty.get(idx), zdarzenia);
				dialog.setVisible(true);
				if (dialog.getKontakt() != null) {
					refreshTableKontakty();
					refreshTableZdarzenia();
				}
			}
		});
		mnKontakty.add(mntmEdytujKontakt);

		JMenuItem mntmUsunKontakt = new JMenuItem("Usuń kontakt");
		mntmUsunKontakt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int idx = getSelectedKontaktIndex();
				if (idx < 0) {
					JOptionPane.showMessageDialog(frame, "Wybierz kontakt z tabeli.", "Brak wyboru",
							JOptionPane.WARNING_MESSAGE);
					return;
				}
				int confirm = JOptionPane.showConfirmDialog(frame,
						"Czy na pewno chcesz usunąć wybrany kontakt?", "Potwierdzenie usunięcia",
						JOptionPane.YES_NO_OPTION);
				if (confirm != JOptionPane.YES_OPTION) {
					return;
				}
				Kontakt k = kontakty.remove(idx);
				for (Zdarzenie z : k.getZdarzenia()) {
					z.getKontakty().remove(k);
				}
				refreshTableKontakty();
				refreshTableZdarzenia();
			}
		});
		mnKontakty.add(mntmUsunKontakt);

		// ── Menu Zdarzenia ────────────────────────────────────────────────────

		JMenu mnZdarzenia = new JMenu("Zdarzenia");
		menuBar.add(mnZdarzenia);

		JMenuItem mntmDodajZdarzenie = new JMenuItem("Dodaj zdarzenie...");
		mntmDodajZdarzenie.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ZdarzenieDialog dialog = new ZdarzenieDialog(frame, kontakty);
				dialog.setVisible(true);
				Zdarzenie noweZdarzenie = dialog.getZdarzenie();
				if (noweZdarzenie != null) {
					zdarzenia.add(noweZdarzenie);
					refreshTableZdarzenia();
					refreshTableKontakty();
				}
			}
		});
		mnZdarzenia.add(mntmDodajZdarzenie);

		JMenuItem mntmEdytujZdarzenie = new JMenuItem("Edytuj zdarzenie...");
		mntmEdytujZdarzenie.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int idx = getSelectedZdarzenieIndex();
				if (idx < 0) {
					JOptionPane.showMessageDialog(frame, "Wybierz zdarzenie z tabeli.", "Brak wyboru",
							JOptionPane.WARNING_MESSAGE);
					return;
				}
				ZdarzenieDialog dialog = new ZdarzenieDialog(frame, zdarzenia.get(idx), kontakty);
				dialog.setVisible(true);
				if (dialog.getZdarzenie() != null) {
					refreshTableZdarzenia();
					refreshTableKontakty();
				}
			}
		});
		mnZdarzenia.add(mntmEdytujZdarzenie);

		JMenuItem mntmUsunZdarzenie = new JMenuItem("Usuń zdarzenie");
		mntmUsunZdarzenie.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int idx = getSelectedZdarzenieIndex();
				if (idx < 0) {
					JOptionPane.showMessageDialog(frame, "Wybierz zdarzenie z tabeli.", "Brak wyboru",
							JOptionPane.WARNING_MESSAGE);
					return;
				}
				int confirm = JOptionPane.showConfirmDialog(frame,
						"Czy na pewno chcesz usunąć wybrane zdarzenie?", "Potwierdzenie usunięcia",
						JOptionPane.YES_NO_OPTION);
				if (confirm != JOptionPane.YES_OPTION) {
					return;
				}
				Zdarzenie z = zdarzenia.remove(idx);
				for (Kontakt k : z.getKontakty()) {
					k.getZdarzenia().remove(z);
				}
				refreshTableZdarzenia();
				refreshTableKontakty();
			}
		});
		mnZdarzenia.add(mntmUsunZdarzenie);

		mnZdarzenia.addSeparator();

		JMenuItem mntmFiltruj = new JMenuItem("Filtruj: starsze niż datę...");
		mntmFiltruj.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LocalDateTime data = zapytajODate("Pokaż zdarzenia starsze niż");
				if (data != null) {
					filtrPrzedData = data;
					refreshTableZdarzenia();
				}
			}
		});
		mnZdarzenia.add(mntmFiltruj);

		JMenuItem mntmFiltrujTekst = new JMenuItem("Filtruj po tekście...");
		mntmFiltrujTekst.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String tekst = JOptionPane.showInputDialog(frame,
						"Szukany tekst (opis / lokalizacja / uczestnik):", "Filtruj zdarzenia",
						JOptionPane.QUESTION_MESSAGE);
				if (tekst != null) {
					tekst = tekst.trim();
					filtrTekst = tekst.isEmpty() ? null : tekst.toLowerCase();
					refreshTableZdarzenia();
				}
			}
		});
		mnZdarzenia.add(mntmFiltrujTekst);

		JMenuItem mntmPokazWszystkie = new JMenuItem("Pokaż wszystkie zdarzenia");
		mntmPokazWszystkie.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				filtrPrzedData = null;
				filtrTekst = null;
				refreshTableZdarzenia();
			}
		});
		mnZdarzenia.add(mntmPokazWszystkie);

		JMenuItem mntmUsunStarsze = new JMenuItem("Usuń zdarzenia starsze niż datę...");
		mntmUsunStarsze.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LocalDateTime data = zapytajODate("Usuń zdarzenia starsze niż");
				if (data == null) {
					return;
				}
				int confirm = JOptionPane.showConfirmDialog(frame,
						"Czy na pewno usunąć wszystkie zdarzenia starsze niż " + data.format(FMT) + "?",
						"Potwierdzenie usunięcia", JOptionPane.YES_NO_OPTION);
				if (confirm != JOptionPane.YES_OPTION) {
					return;
				}
				int usuniete = Zdarzenie.usunStarszeNiz(zdarzenia, data);
				refreshTableZdarzenia();
				refreshTableKontakty();
				JOptionPane.showMessageDialog(frame, "Usunięto " + usuniete + " zdarzeń.");
			}
		});
		mnZdarzenia.add(mntmUsunStarsze);

		// ── Menu Pomoc ────────────────────────────────────────────────────────

		JMenu mnPomoc = new JMenu("Pomoc");
		menuBar.add(mnPomoc);

		JMenuItem mntmOProgramie = new JMenuItem("O programie...");
		mntmOProgramie.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				oProgramie();
			}
		});
		mnPomoc.add(mntmOProgramie);

		// ── Split Pane ────────────────────────────────────────────────────────

		JSplitPane splitPane = new JSplitPane();
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		splitPane.setDividerLocation(280);
		frame.getContentPane().add(splitPane, BorderLayout.CENTER);

		// ── Contacts panel ────────────────────────────────────────────────────

		JScrollPane scrollPaneKontakty = new JScrollPane();
		splitPane.setTopComponent(scrollPaneKontakty);

		modelKontakty = new DefaultTableModel(new Object[][] {},
				new String[] { "ID", "Imię", "Nazwisko", "Email", "Telefon" }) {
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		tableKontakty = new JTable(modelKontakty);
		tableKontakty.setAutoCreateRowSorter(true); // sortowanie po kliknięciu nagłówka kolumny
		tableKontakty.getColumnModel().getColumn(0).setMaxWidth(50);
		scrollPaneKontakty.setViewportView(tableKontakty);

		// ── Events panel ──────────────────────────────────────────────────────

		JScrollPane scrollPaneZdarzenia = new JScrollPane();
		splitPane.setBottomComponent(scrollPaneZdarzenia);

		modelZdarzenia = new DefaultTableModel(new Object[][] {},
				new String[] { "ID", "Data", "Opis", "Lokalizacja", "Uczestnicy" }) {
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		tableZdarzenia = new JTable(modelZdarzenia);
		tableZdarzenia.setAutoCreateRowSorter(true); // sortowanie po kliknięciu nagłówka kolumny
		tableZdarzenia.getColumnModel().getColumn(0).setMaxWidth(50);
		tableZdarzenia.getColumnModel().getColumn(1).setPreferredWidth(130);
		scrollPaneZdarzenia.setViewportView(tableZdarzenia);

		scrollPaneKontakty.setBorder(BorderFactory.createTitledBorder("Kontakty"));
		scrollPaneZdarzenia.setBorder(BorderFactory.createTitledBorder("Zdarzenia"));
	}

	/**
	 * Ask the user for a date/time (format yyyy-MM-dd HH:mm).
	 * Returns null if cancelled or input is invalid.
	 */
	private LocalDateTime zapytajODate(String tytul) {
		String wejscie = JOptionPane.showInputDialog(frame, "Podaj datę (yyyy-MM-dd HH:mm):", tytul,
				JOptionPane.QUESTION_MESSAGE);
		if (wejscie == null || wejscie.trim().isEmpty()) {
			return null;
		}
		try {
			return LocalDateTime.parse(wejscie.trim(), FMT);
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(frame, "Nieprawidłowy format daty.", "Błąd", JOptionPane.ERROR_MESSAGE);
			return null;
		}
	}

	/**
	 * Show the "About" dialog.
	 */
	private void oProgramie() {
		String tekst = "Kalendarz\n\n"
				+ "Prosty organizer do zarządzania kontaktami i zdarzeniami.\n"
				+ "Dane przechowywane w bazie SQLite, z możliwością zapisu\n"
				+ "i odczytu w formacie XML. Aplikacja działa w trybie\n"
				+ "graficznym (-gui) oraz tekstowym (-txt).\n\n"
				+ "Projekt zespołowy, Uniwersytet Vizja\n\n"
				+ "Autorzy:\n"
				+ "  Yevhen Kapush\n"
				+ "  Serhii Narizhnyi";
		JOptionPane.showMessageDialog(frame, tekst, "O programie", JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Save in-memory data to the database (connection opened and closed inside).
	 */
	private void zapiszDoBazy() {
		try {
			DbManager.zapiszDoBazy(kontakty, zdarzenia);
			JOptionPane.showMessageDialog(frame, "Zapisano dane do bazy danych.");
		} catch (Exception e) {
			JOptionPane.showMessageDialog(frame, "Błąd zapisu do bazy:\n" + e.getMessage(), "Błąd",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Reload data from the database, replacing the current in-memory data.
	 */
	private void odczytajZBazy() {
		try {
			kontakty.clear();
			zdarzenia.clear();
			DbManager.odczytajZBazy(kontakty, zdarzenia);
			refreshTableKontakty();
			refreshTableZdarzenia();
			JOptionPane.showMessageDialog(frame, "Odczytano dane z bazy danych.");
		} catch (Exception e) {
			JOptionPane.showMessageDialog(frame, "Błąd odczytu z bazy:\n" + e.getMessage(), "Błąd",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Open file chooser and save data to XML.
	 */
	private void zapiszDoXML() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Zapisz do XML");
		fileChooser.setSelectedFile(new java.io.File("kalendarz.xml"));
		fileChooser.setFileFilter(new FileNameExtensionFilter("Pliki XML (*.xml)", "xml"));

		int result = fileChooser.showSaveDialog(frame);
		if (result == JFileChooser.APPROVE_OPTION) {
			try {
				XmlManager.zapisz(kontakty, zdarzenia, fileChooser.getSelectedFile().getAbsolutePath());
				JOptionPane.showMessageDialog(frame, "Zapisano do: " + fileChooser.getSelectedFile().getName());
			} catch (Exception e) {
				JOptionPane.showMessageDialog(frame, "Błąd zapisu XML:\n" + e.getMessage(), "Błąd",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	/**
	 * Open file chooser and load data from XML.
	 */
	private void odczytajZXML() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Odczytaj z XML");
		fileChooser.setFileFilter(new FileNameExtensionFilter("Pliki XML (*.xml)", "xml"));

		int result = fileChooser.showOpenDialog(frame);
		if (result == JFileChooser.APPROVE_OPTION) {
			try {
				kontakty.clear();
				zdarzenia.clear();
				XmlManager.odczytaj(kontakty, zdarzenia, fileChooser.getSelectedFile().getAbsolutePath());
				refreshTableKontakty();
				refreshTableZdarzenia();
				JOptionPane.showMessageDialog(frame, "Odczytano z: " + fileChooser.getSelectedFile().getName());
			} catch (Exception e) {
				JOptionPane.showMessageDialog(frame, "Błąd odczytu XML:\n" + e.getMessage(), "Błąd",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}