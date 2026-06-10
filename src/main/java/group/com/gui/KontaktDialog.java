package group.com.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;

import group.com.Email;
import group.com.Kontakt;
import group.com.Zdarzenie;

public class KontaktDialog extends JDialog {

	private JTextField textFieldImie;
	private JTextField textFieldNazwisko;
	private JTextField textFieldEmail;
	private JTextField textFieldTelefon;
	private JList<String> listZdarzenia;
	private DefaultListModel<String> listModelZdarzenia;

	private Kontakt kontakt;
	private List<Zdarzenie> wszystkieZdarzenia;
	private boolean confirmed = false;

	/**
	 * Create the dialog for adding a new contact.
	 */
	public KontaktDialog(java.awt.Frame parent, List<Zdarzenie> wszystkieZdarzenia) {
		this(parent, null, wszystkieZdarzenia);
	}

	/**
	 * Create the dialog for editing an existing contact.
	 */
	public KontaktDialog(java.awt.Frame parent, Kontakt kontakt, List<Zdarzenie> wszystkieZdarzenia) {
		super(parent, kontakt == null ? "Dodaj kontakt" : "Edytuj kontakt", true);
		this.kontakt = kontakt;
		this.wszystkieZdarzenia = wszystkieZdarzenia;
		initialize();
		if (kontakt != null) {
			fillFields();
		}
	}

	/**
	 * Initialize the contents of the dialog.
	 */
	private void initialize() {
		setBounds(100, 100, 480, 420);
		setLocationRelativeTo(getParent());
		getContentPane().setLayout(new BorderLayout());

		// ── Form panel ────────────────────────────────────────────────────────

		JPanel panelForm = new JPanel(new GridBagLayout());
		getContentPane().add(panelForm, BorderLayout.NORTH);

		GridBagConstraints gbcLabel = new GridBagConstraints();
		gbcLabel.anchor = GridBagConstraints.LINE_END;
		gbcLabel.insets = new Insets(6, 10, 6, 6);

		GridBagConstraints gbcField = new GridBagConstraints();
		gbcField.fill = GridBagConstraints.HORIZONTAL;
		gbcField.weightx = 1.0;
		gbcField.insets = new Insets(6, 0, 6, 10);

		gbcLabel.gridx = 0; gbcLabel.gridy = 0;
		panelForm.add(new JLabel("Imię:"), gbcLabel);
		gbcField.gridx = 1; gbcField.gridy = 0;
		textFieldImie = new JTextField();
		panelForm.add(textFieldImie, gbcField);

		gbcLabel.gridy = 1;
		panelForm.add(new JLabel("Nazwisko:"), gbcLabel);
		gbcField.gridy = 1;
		textFieldNazwisko = new JTextField();
		panelForm.add(textFieldNazwisko, gbcField);

		gbcLabel.gridy = 2;
		panelForm.add(new JLabel("Email:"), gbcLabel);
		gbcField.gridy = 2;
		textFieldEmail = new JTextField();
		panelForm.add(textFieldEmail, gbcField);

		gbcLabel.gridy = 3;
		panelForm.add(new JLabel("Telefon:"), gbcLabel);
		gbcField.gridy = 3;
		textFieldTelefon = new JTextField();
		panelForm.add(textFieldTelefon, gbcField);

		// ── Zdarzenia list ────────────────────────────────────────────────────

		listModelZdarzenia = new DefaultListModel<String>();
		for (Zdarzenie z : wszystkieZdarzenia) {
			listModelZdarzenia.addElement("[" + z.getId() + "] " + z.getOpis());
		}

		listZdarzenia = new JList<String>(listModelZdarzenia);
		listZdarzenia.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		JScrollPane scrollPaneZdarzenia = new JScrollPane(listZdarzenia);
		scrollPaneZdarzenia.setBorder(new TitledBorder("Zdarzenia (powiązane)"));
		getContentPane().add(scrollPaneZdarzenia, BorderLayout.CENTER);

		// ── Buttons panel ─────────────────────────────────────────────────────

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

	/**
	 * Fill form fields with existing contact data.
	 */
	private void fillFields() {
		textFieldImie.setText(kontakt.getImie());
		textFieldNazwisko.setText(kontakt.getNazwisko());
		textFieldEmail.setText(kontakt.getEmail());
		textFieldTelefon.setText(kontakt.getTelefon());

		for (int i = 0; i < wszystkieZdarzenia.size(); i++) {
			Zdarzenie z = wszystkieZdarzenia.get(i);
			if (kontakt.getZdarzenia().contains(z)) {
				listZdarzenia.addSelectionInterval(i, i);
			}
		}
	}

	/**
	 * Validate and apply form data.
	 */
	private void onOK() {
		String imie = textFieldImie.getText().trim();
		String nazwisko = textFieldNazwisko.getText().trim();
		String email = textFieldEmail.getText().trim();
		String telefon = textFieldTelefon.getText().trim();

		if (imie.isEmpty() || nazwisko.isEmpty() || email.isEmpty() || telefon.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Wszystkie pola są wymagane.", "Błąd walidacji", JOptionPane.WARNING_MESSAGE);
			return;
		}

		try {
			if (kontakt == null) {
				kontakt = new Kontakt(email, imie, nazwisko, telefon);
			} else {
				kontakt.setImie(imie);
				kontakt.setNazwisko(nazwisko);
				kontakt.setEmail(new Email(email));
				kontakt.setTelefon(telefon);
				// remove old many-to-many links
				for (Zdarzenie z : kontakt.getZdarzenia()) {
					z.getKontakty().remove(kontakt);
				}
				kontakt.getZdarzenia().clear();
			}

			// apply selected many-to-many links
			int[] selectedIndices = listZdarzenia.getSelectedIndices();
			for (int i : selectedIndices) {
				Zdarzenie z = wszystkieZdarzenia.get(i);
				kontakt.addZdarzenie(z);
			}

			confirmed = true;
			dispose();
		} catch (IllegalArgumentException ex) {
			JOptionPane.showMessageDialog(this, "Błąd walidacji:\n" + ex.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Returns the resulting contact, or null if cancelled.
	 */
	public Kontakt getKontakt() {
		return confirmed ? kontakt : null;
	}
}	