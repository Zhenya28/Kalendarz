package group.com;

import java.util.Comparator;

public class KontaktPoEmailuComparator implements Comparator<Kontakt> {
	public int compare(Kontakt a, Kontakt b) {
		return a.getEmail().compareTo(b.getEmail());
	}
}
