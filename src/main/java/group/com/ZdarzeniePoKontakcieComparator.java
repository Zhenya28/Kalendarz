package group.com;

import java.util.Comparator;
import java.util.List;

public class ZdarzeniePoKontakcieComparator implements Comparator<Zdarzenie> {
    
    @Override
    public int compare(Zdarzenie a, Zdarzenie b) {
        List<Kontakt> listaA = a.getKontakty();
        List<Kontakt> listaB = b.getKontakty();

        if (listaA.isEmpty() && listaB.isEmpty()) return 0;
        if (listaA.isEmpty()) return -1;
        if (listaB.isEmpty()) return 1;

        return listaA.get(0).compareTo(listaB.get(0));
    }
}