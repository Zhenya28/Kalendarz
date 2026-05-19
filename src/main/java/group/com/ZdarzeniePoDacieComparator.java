package group.com;

import java.util.Comparator;

public class ZdarzeniePoDacieComparator implements Comparator<Zdarzenie> {
	public int compare(Zdarzenie a, Zdarzenie b) {
		return a.getData().compareTo(b.getData());
	}
}