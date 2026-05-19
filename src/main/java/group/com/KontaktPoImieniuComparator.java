package group.com;
import java.util.Comparator;

public class KontaktPoImieniuComparator implements Comparator<Kontakt> {
	public int compare(Kontakt a, Kontakt b) {
		return a.getImie().compareTo(b.getImie());
	}
}