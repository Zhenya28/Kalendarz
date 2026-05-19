package group.com;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class XmlManager {

  public static void zapisz(List<Kontakt> kontakty, List<Zdarzenie> zdarzenia, String plik) throws Exception {
    DocumentBuilder b = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    Document doc = b.newDocument();

    Element root = doc.createElement("kalendarz");
    doc.appendChild(root);

    Element eK = doc.createElement("kontakty");
    root.appendChild(eK);
    for (Kontakt k : kontakty) {
      Element e = doc.createElement("kontakt");
      e.setAttribute("id", String.valueOf(k.getId()));
      e.setAttribute("email", k.getEmail());
      e.setAttribute("imie", k.getImie());
      e.setAttribute("nazwisko", k.getNazwisko());
      e.setAttribute("telefon", k.getTelefon());
      eK.appendChild(e);
    }

    Element eZ = doc.createElement("zdarzenia");
    root.appendChild(eZ);
    for (Zdarzenie z : zdarzenia) {
      Element e = doc.createElement("zdarzenie");
      e.setAttribute("id", String.valueOf(z.getId()));
      e.setAttribute("location", z.getLocation().toString());
      e.setAttribute("data", z.getData().toString());
      e.setAttribute("opis", z.getOpis());

      for (Kontakt uczestnik : z.getKontakty()) {
        Element u = doc.createElement("uczestnik");
        u.setAttribute("kontaktId", String.valueOf(uczestnik.getId()));
        e.appendChild(u);
      }
      eZ.appendChild(e);
    }

    Transformer t = TransformerFactory.newInstance().newTransformer();
    t.setOutputProperty(OutputKeys.INDENT, "yes");
    t.transform(new DOMSource(doc), new StreamResult(new File(plik)));
  }

  public static void odczytaj(List<Kontakt> kontakty, List<Zdarzenie> zdarzenia, String plik) throws Exception {
    DocumentBuilder b = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    Document doc = b.parse(new File(plik));

    kontakty.clear();
    zdarzenia.clear();

    NodeList listaK = doc.getElementsByTagName("kontakt");
    for (int i = 0; i < listaK.getLength(); i++) {
      Element e = (Element) listaK.item(i);
      Kontakt k = new Kontakt(e.getAttribute("email"), e.getAttribute("imie"), e.getAttribute("nazwisko"),
          e.getAttribute("telefon"));

      String idStr = e.getAttribute("id");
      if (idStr != null && !idStr.isEmpty()) {
        k.setId(Integer.parseInt(idStr));
      }
      kontakty.add(k);
    }

    NodeList listaZ = doc.getElementsByTagName("zdarzenie");
    for (int i = 0; i < listaZ.getLength(); i++) {
      Element e = (Element) listaZ.item(i);

      Zdarzenie z = new Zdarzenie(e.getAttribute("location"), LocalDateTime.parse(e.getAttribute("data")),
          e.getAttribute("opis"));

      String idStr = e.getAttribute("id");
      if (idStr != null && !idStr.isEmpty()) {
        z.setId(Integer.parseInt(idStr));
      }

      NodeList uczestnicy = e.getElementsByTagName("uczestnik");
      for (int j = 0; j < uczestnicy.getLength(); j++) {
        Element u = (Element) uczestnicy.item(j);

        String kIdStr = u.getAttribute("kontaktId");
        if (kIdStr != null && !kIdStr.isEmpty()) {
          int targetId = Integer.parseInt(kIdStr);

          for (Kontakt k : kontakty) {
            if (k.getId() == targetId) {
              z.addKontakt(k);
              break;
            }
          }
        }
      }
      zdarzenia.add(z);
    }
  }
}