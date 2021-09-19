package at.diwh.generalnio.consumer;


import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Demo wie man eine Textdatei in eine CSV Datei verwandelt. Die Textdatei ist hier eine verunstaltete DEMO Datei Wie im
 * Original zeigt die Zeile, in der <i>record(s) selected.</i> steht, das Ende des Blocks und eine Zeile, die mit ---
 * beginnt zeigt den Anfang des Blocks an, der in CVS verwandelt werden soll.
 * 
 * @author diwh
 * @param <T> - Generic
 */
public class TransformTxtToCsvConsumer<T> implements Consumer<T> {

    private List<String> data = new ArrayList<>();
    private List<String> exceptions = new ArrayList<>();
    private Boolean breakLineGefunden = Boolean.FALSE;
    private static final String LAENGENMA = "------- -------------- ---------- ------------ --------------------------------------------- ------ ------------ ------------- ---------------- -------- ------------- ------------- ---------------------------------------------- -------------- -------------- ----------------- ---------------------------------------------- ------------------- ------------------- ---------------------------------- -------- ----------------------- ---------------------- ---------------- ---- ---- ------ ------ --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------";
    private static final String FELDNAMEN = "YAYTOPA 6              YUNAASOENA EUUNESNAEESO PE_NENA                                       APNR   EE           EUUNESANAETUN ANTOESSUNTSAETUN ARARTNRS ARARTNRSAETUN ASNR_PETRANT  NENA_PETRANT                                   TAYAET_PETRANT TASASO_PETRANT ASNR_AARSRASARTAR NENA_AARSRASARTAR                              TAYAET_AARSRASARTAR TASASO_AARSRASARTAR STREßA                             POE      ORT                     URSEASA                URANAAARSASUOAAN AERT ARET ARETU6 ARETU6 ARETTAAT";
    private String[] geteilteFelder = LAENGENMA.split(" ");
    private String[] geteilteFeldnamen = FELDNAMEN.split(" ");

    @Override
    public void accept(T t) {
        String input = (String) t;
        if (breakLineGefunden.booleanValue()) {
            if (input.contains("record(s) selected.")) {
                breakLineGefunden = Boolean.FALSE;
            } else {
                int feldnamenIndex = 0;
                StringBuilder csv = new StringBuilder();
                int startPos = 0;
                int endPos = 0;
                int dataStringLength = input.length();
                for (String element : geteilteFelder) {
                    int laengeFeld = element.length() + 1; // ((feldnamenIndex + 1) < geteilteFelder.length ? 1 : 0);
                    endPos = endPos + laengeFeld;
                    System.out.println("Scanne von Position " + startPos + " bis Position " + endPos);
                    // letzte Spalte wird nicht mit Leerzeichen gefüllt vom Sender, d.h. Sonderfall am Ende!
                    if (endPos > dataStringLength) {
                        endPos = dataStringLength;
                    }
                    String feld = input.substring(startPos, endPos);
                    csv.append(feld.trim());
                    csv.append(";");
                    startPos = endPos;
                    System.out.println("Feldname: " + geteilteFeldnamen[feldnamenIndex] + " -> [" + feld.trim() + "]");
                    feldnamenIndex++;
                }
                data.add(csv.toString());
            }
        } else {
            if (input.startsWith("---")) {
                breakLineGefunden = Boolean.TRUE;
            }
        }
    }

    /**
     * @return Liste von Exceptions als String
     */
    public List<String> getExceptions() {
        return new ArrayList<>(this.exceptions); // sodass der Aufrufer nicht ein getExceptions.add... machen kann
    }

    @SuppressWarnings("javadoc")
    public List<String> getData() {
        return data;
    }

    @SuppressWarnings("javadoc")
    public void setData(List<String> data) {
        this.data = data;
    }
}
