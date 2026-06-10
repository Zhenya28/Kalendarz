package group.com.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerDateModel;
import javax.swing.border.TitledBorder;

import com.toedter.calendar.JDateChooser;

import group.com.Kontakt;
import group.com.Zdarzenie;

public class ZdarzenieDialog extends JDialog {

	private JTextField textFieldOpis;
	private JTextField textFieldLokalizacja;
	private JDateChooser dateChooserData;
	private JSpinner spinnerCzas;
	private JList<String> listKontakty;
	private DefaultListModel<String> listModelKontakty;

	private Zdarzenie zdarzenie;
	private List<Kontakt> wszystkieKontakty;
	private boolean confirmed = false;

	public ZdarzenieDialog(java.awt.Frame parent, List<Kontakt> wszystkieKontakty) {
		this(parent, null, wszystkieKontakty);
	}

	public ZdarzenieDialog(java.awt.Frame parent, Zdarzenie zdarzenie, List<Kontakt> wszystkieKontakty) {
		super(parent, zdarzenie == null ? "Dodaj zdarzenie" : "Edytuj zdarzenie", true);
		this.zdarzenie = zdarzenie;
		this.wszystkieKontakty = wszystkieKontakty;
		initialize();
		if (zdarzenie != null) {
			fillFields();
		}
	}

	private void initialize() {
		setBounds(100, 100, 480, 450);
		setLocationRelativeTo(getParent());
		getContentPane().setLayout(new BorderLayout());

		JPanel panelForm = new JPanel(new GridBagLayout());
		getContentPane().add(panelForm, BorderLayout.NORTH);

		GridBagConstraints gbcLabel = new GridBagConstraints();
		gbcLabel.anchor = GridBagConstraints.LINE_END;
		gbcLabel.insets = new Insets(6, 10, 6, 6);

		GridBagConstraints gbcField = new GridBagConstraints();
		gbcField.fill = GridBagConstraints.HORIZONTAL;
		gbcField.weightx = 1.0;
		gbcField.insets = new Insets(6, 0, 6, 10);

		gbcLabel.gridx = 0;
		gbcLabel.gridy = 0;
		panelForm.add(new JLabel("Opis:"), gbcLabel);
		gbcField.gridx = 1;
		gbcField.gridy = 0;
		textFieldOpis = new JTextField();
		panelForm.add(textFieldOpis, gbcField);

		gbcLabel.gridy = 1;
		panelForm.add(new JLabel("Lokalizacja:"), gbcLabel);
		gbcField.gridy = 1;
		textFieldLokalizacja = new JTextField();
		panelForm.add(textFieldLokalizacja, gbcField);

		gbcLabel.gridy = 2;
		panelForm.add(new JLabel("Data:"), gbcLabel);
		gbcField.gridy = 2;
		dateChooserData = new JDateChooser();
		dateChooserData.setDateFormatString("yyyy-MM-dd");
		dateChooserData.setDate(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
		panelForm.add(dateChooserData, gbcField);

		gbcLabel.gridy = 3;
		panelForm.add(new JLabel("Czas:"), gbcLabel);
		gbcField.gridy = 3;
		spinnerCzas = new JSpinner(new SpinnerDateModel());
		JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(spinnerCzas, "HH:mm");
		spinnerCzas.setEditor(timeEditor);
		spinnerCzas.setValue(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
		panelForm.add(spinnerCzas, gbcField);

		listModelKontakty = new DefaultListModel<String>();
		for (Kontakt k : wszystkieKontakty) {
			listModelKontakty.addElement("[" + k.getId() + "] " + k.getImie() + " " + k.getNazwisko());
		}

		listKontakty = new JList<String>(listModelKontakty);
		listKontakty.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		JScrollPane scrollPaneKontakty = new JScrollPane(listKontakty);
		scrollPaneKontakty.setBorder(new TitledBorder("Uczestnicy (powiązane kontakty)"));
		getContentPane().add(scrollPaneKontakty, BorderLayout.CENTER);

		JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(panelButtons, BorderLayout.SOUTH);

		JButton btnOK = new JButton("OK");
		btnOK.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onOK();
			}
		});
		panelButtons.add(btnOK);
		getRootPane().setDefaultButton(btnOK);

		JButton btnAnuluj = new JButton("Anuluj");
		btnAnuluj.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		panelButtons.add(btnAnuluj);
	}

	private void fillFields() {
		textFieldOpis.setText(zdarzenie.getOpis());
		textFieldLokalizacja.setText(zdarzenie.getLocation().toString());
		
		Date eventDate = Date.from(zdarzenie.getData().atZone(ZoneId.systemDefault()).toInstant());
		dateChooserData.setDate(eventDate);
		spinnerCzas.setValue(eventDate);

		for (int i = 0; i < wszystkieKontakty.size(); i++) {
			Kontakt k = wszystkieKontakty.get(i);
			if (zdarzenie.getKontakty().contains(k)) {
				listKontakty.addSelectionInterval(i, i);
			}
		}
	}

	private void onOK() {
		String opis = textFieldOpis.getText().trim();
		String lokalizacja = textFieldLokalizacja.getText().trim();
		Date selectedDate = dateChooserData.getDate();
		Date selectedTime = (Date) spinnerCzas.getValue();

		if (opis.isEmpty() || lokalizacja.isEmpty() || selectedDate == null || selectedTime == null) {
			JOptionPane.showMessageDialog(this, "Wszystkie pola są wymagane.", "Błąd walidacji", JOptionPane.WARNING_MESSAGE);
			return;
		}

		LocalDate localDate = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		LocalTime localTime = selectedTime.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
		LocalDateTime data = LocalDateTime.of(localDate, localTime);

		try {
			if (zdarzenie == null) {
				zdarzenie = new Zdarzenie(lokalizacja, data, opis);
			} else {
				zdarzenie.setOpis(opis);
				zdarzenie.setLocation(lokalizacja);
				zdarzenie.setData(data);
				
				for (Kontakt k : zdarzenie.getKontakty()) {
					k.getZdarzenia().remove(zdarzenie);
				}
				zdarzenie.getKontakty().clear();
			}

			int[] selectedIndices = listKontakty.getSelectedIndices();
			for (int i : selectedIndices) {
				Kontakt k = wszystkieKontakty.get(i);
				zdarzenie.addKontakt(k);
			}

			confirmed = true;
			dispose();
		} catch (IllegalArgumentException ex) {
			JOptionPane.showMessageDialog(this, "Błąd:\n" + ex.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
		}
	}

	public Zdarzenie getZdarzenie() {
		return confirmed ? zdarzenie : null;
	}
}