package at.diwh.generalnio.main;

import java.awt.desktop.SystemSleepEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;

import at.diwh.generalnio.consumer.FullBinaryReadConsumer;
import at.diwh.generalnio.consumer.FullTextualReadConsumer;
import at.diwh.generalnio.consumer.ReadNxLinesTextConsumer;
import at.diwh.generalnio.consumer.TransformTxtToCsvConsumer;
import at.diwh.generalnio.core.GeneralNIOFileAccess;

/**
 * Dies ist ein Versuch, eine generelle File-Access-Klasse zu bauen; 
 * inspieriert durch FileAccessWithNIO 
 * @author diwh
 *
 */
public class DemoGeneralNIOFileAccess {

    /**
     * Die Main-Methode dient rein dem TESTEN bzw. der DEMONSTRATION der Verwendung.
     * 
     * @param args - Arguments
     * @throws IOException - hoffentlich nicht
     * @throws URISyntaxException - hoffentlich auch nicht
     */
    public static void main(String[] args) throws IOException, URISyntaxException {
        String LINIE = "-------------------------------------------------------------------------------------------------------------------------";

        // klassische Filesystemangabe
        String tempDirName = System.getenv("TEMP");
        if (tempDirName == null) {
        	tempDirName= System.getProperty("user.home");
        }
            
        System.out.println("Ich verwende das Temporärverzueichnis: " + tempDirName);
        String fileLocation = tempDirName + "/irgendeinfile.txt";
        String fileToWriteLocation = tempDirName +"/out.txt";

        // src/conf ist ein Resource-Ordner; damit ist /testdata der erste Ordner unter diesem "Resource-Root"
        String classpathLocation = "/testdata/irgendeinfile.txt";


        // Consumer, als Lambda, simples Beispiel; kann beliebig complex gestaltet werden
        Consumer<String> showStringOnConsole = t -> System.out.println(t);

        // Consumer, Zeilenzähler als Lambda, simples Beispiel; kann beliebig complex gestaltet werden
        class Zaehler {

            private Integer zaehler = Integer.valueOf(0);

            public Integer getZaehler() {
                return zaehler;
            }
            public void setZaehler(Integer zaehler) {
                this.zaehler = zaehler;
            }
        }
        Zaehler zaehler = new Zaehler();
        Consumer<String> countLines = t -> zaehler.setZaehler(Integer.valueOf(zaehler.getZaehler().intValue() + 1));

        // Consumer, als Lambda, simples Beispiel; kann beliebig complex gestaltet werden
        Consumer<byte[]> showByteArrayOnConsole = t -> System.out.print(new String(t));

        // Consumer für ganze Datei binär in ein Byte-Array lesen
        // gleich mit der konkreten Klasse typisiert
        // ACHTUNG: Das ist nir eine Demo. Denn eigentlich braucht man das nicht,
        // denn leseFile liest bei Blocksize=0 ohnehin das ganze File.
        FullBinaryReadConsumer<byte[]> readFileBinary = new FullBinaryReadConsumer<>();

        // Consumer für ganze Datei binär in ein Byte-Array lesen
        // diesmal mit dem Interface Consumer typisiert
        // ACHTUNG: Das ist nir eine Demo. Denn eigentlich braucht man das nicht,
        // denn leseFile liest bei Blocksize=0 ohnehin das ganze File.
        Consumer<String> readFileTextual = new FullTextualReadConsumer<>();

        // Sample für das Lesen eines Binärfiles in ein Byte-Array
        System.out.println("[ 0.a ]" + LINIE);
        // Parameter: 
        // classpathLocation : die Classpath-Location; ein String in der Notation eines Resource-Path-Eintrags
        // DemoGeneralNIOFileAccess.class weil resource brauchen wir eine Klasse
        // null: weil binär kein Encoding
        // 0: wir wollen das ganze File auf einmal einlesen
        // readFileBinary: der Consumer
        GeneralNIOFileAccess.leseFile(classpathLocation, DemoGeneralNIOFileAccess.class, null, 0, readFileBinary);
        System.out.println("Gelesen: " + readFileBinary.getData().length + " Byte!");

        // Sample für das Lesen eines Textfiles in einen String
        System.out.println("[ 0.b ]" + LINIE);
        // Parameter:
        // classpathLocation : die Classpath-Location; ein String in der Notation eines Resource-Path-Eintrags
        // DemoGeneralNIOFileAccess.class weil resource brauchen wir eine Klasse
        // StandardCharsets.UTF_8: weil textuelles Lesen brauchen wir das Encoding
        // 0: wir wollen das ganze File auf einmal einlesen
        // readFileTextual: der Consumer
        GeneralNIOFileAccess.leseFile(classpathLocation, DemoGeneralNIOFileAccess.class, StandardCharsets.UTF_8, 0,
            readFileTextual);
        // Achtung: Da wir den Consumer mit dem Interface typisiert haben, brauchen wir einen Cast
        // sonst können wir die getData() Methode natürlich nicht aufrufen.
        System.out.println("Gelesen: [" + ((FullTextualReadConsumer<String>) readFileTextual).getData() + "]");

        // Sample für Lesen vom Filesystem (Text) buffersize=1 für zeilenweises Lesen
        System.out.println("[ 1 ]" + LINIE);
        // Parameter:
        // fileLocation : die File-Location; ein String in der Notation eines klass. Files
        // null: weil es eine File-Location ist, brauchen wir keine Klasse
        // StandardCharsets.UTF_8: weil textuelles Lesen brauchen wir das Encoding
        // 1: wir wollen das File zeilenweise einlesen
        // showStringOnConsole: der Consumer (Ausgabe am Schirm pro Zeile während die Datei gelesen wird)
        GeneralNIOFileAccess.leseFile(fileLocation, null, StandardCharsets.UTF_8, 1, showStringOnConsole);
        GeneralNIOFileAccess.leseFile(fileLocation, null, StandardCharsets.UTF_8, 1, countLines);
        System.out.println("Das File " + fileLocation + " hat " + zaehler.getZaehler().intValue() + " Zeilen.");
        System.out.println();

        // Sample für Lesen vom Resource Path (Text) buffersize=1 für zeilenweises Lesen        
        System.out.println("[ 2 ]" + LINIE);
        GeneralNIOFileAccess.leseFile(classpathLocation, DemoGeneralNIOFileAccess.class, StandardCharsets.UTF_8, 1,
            showStringOnConsole);

        // Sample für Lesen vom Filesystem (Binär)
        System.out.println("[ 3 ]" + LINIE);
        GeneralNIOFileAccess.leseFile(fileLocation, null, null, 2048, showByteArrayOnConsole);

        // Sample für Lesen vom Resource Path (Binär)        
        System.out.println("[ 4 ]" + LINIE);
        GeneralNIOFileAccess.leseFile(classpathLocation, DemoGeneralNIOFileAccess.class, null, 2048,
            showByteArrayOnConsole);

        /*
         * Hier folgt ein Beispiel für einen Consumer-Einsatz im Falle dass man nur eine bestimmte Anzahl an Zeilen
         * einer Datei lesen will (typ. CSV-Dateien, die in der ersten Zeile Header-Info enthalten und nur die will
         * man). Es ist natürlich so, dass die Datei komplett "gelesen" wird, wenn man das so macht, nur werden die
         * Zeilen, die mehr sind als die gewünschten nicht im Memory abgelegt.
         */
        // wir initialisieren einen Consumer, der nur die erste Zeile zurück liefern soll
        ReadNxLinesTextConsumer<String> readNxLines = new ReadNxLinesTextConsumer<>(Integer.valueOf(1));
        // nun rufen wir leseFile
        System.out.println("[ 5 ]" + LINIE);
        // die 1 bedeutet eben zeilenweises Lesen; also alles >0 bedeutet das, weil der Parameter sonst eben die
        // Blocksize angibt, wenn man binär liest! Bei 0 bedeutet das: GANZES FILE lesen!
        GeneralNIOFileAccess.leseFile(classpathLocation, DemoGeneralNIOFileAccess.class, StandardCharsets.UTF_8, 1,
            readNxLines);
        // und nun schauen wir uns an, was der Consumer gespeichert hat:
        System.out.println("Anzahl Lines gelesen: " + readNxLines.getData().size());
        for (String element : readNxLines.getData()) {
            System.out.println(element);
        }


        // Sample für das Schreiben eines Textes in ein File, overwrite
        System.out.println("(Schreibtest)" + LINIE);
        GeneralNIOFileAccess.schreibeFile(fileToWriteLocation, "Das ist ein Test\n", Boolean.FALSE,
            StandardCharsets.UTF_8);

        // Sample für das Schreiben eines Binärinhalts in ein File, append
        System.out.println("(Schreibtest)" + LINIE);
        byte[] testArray = "Hinzu kommt diese Zeile!".getBytes(StandardCharsets.UTF_8);
        GeneralNIOFileAccess.schreibeFile(fileToWriteLocation, testArray, Boolean.TRUE, null);

        // Sample für das Auslesen des Verzeichnisbaums
        System.out.println("(Verzeichnisbaum)" + LINIE);
        List<Path> dateien = new ArrayList<>();
        List<Path> verzeichnisse = new ArrayList<>();
        String elementName = System.getenv("TEMP");
        System.out.println("Quellverezeichnis: " + elementName);
        dateien = GeneralNIOFileAccess.holeDateibaum(elementName, verzeichnisse);
        System.out.println("Verzeichnisse Anzahl Elemente: " + verzeichnisse.size());
        System.out.println("Dateien Anzahl Elemente: " + dateien.size());
        for (Path element : verzeichnisse) {
            if (element.getFileName() != null) {
                elementName = element.getFileName().toString();
            }
            int tabcounter = element.getNameCount() - 1;
            for (int i = 0; i < tabcounter; i++) {
                System.out.print("  ");
            }
            System.out.println("[" + elementName + "]");
        }
        System.out.println();

        // Sample für das Suchen nach spezifischen Files
        System.out.println("(Filesuche 'enthält')" + LINIE);
        List<String> dateinamen = new ArrayList<>();
        elementName = System.getenv("TEMP");
        String sucheNach = "1_3";
        System.out.println("Quellverezeichnis: " + elementName);

        dateinamen = GeneralNIOFileAccess.holeAlleFilesMitNameEnthaeltVonVerzeichnis(elementName, sucheNach);
        System.out.println("Suche nach dem String '" + sucheNach + "'");
        System.out.println("Dateien Anzahl Elemente: " + dateinamen.size());
        for (String element : dateinamen) {
            System.out.println("[" + element + "]");
        }
        System.out.println();

        // Sample für das Suchen nach spezifischen Files
        System.out.println("(Filesuche 'endet mit')" + LINIE);
        dateinamen = new ArrayList<>();
        elementName = System.getenv("TEMP");
        sucheNach = ".exe";
        System.out.println("Quellverezeichnis: " + elementName);

        dateinamen = GeneralNIOFileAccess.holeAlleFilesMitNameEndetVonVerzeichnis(elementName, sucheNach);
        System.out.println("Suche nach dem Ende des Filenamens: '" + sucheNach + "'");
        System.out.println("Dateien Anzahl Elemente: " + dateinamen.size());
        for (String element : dateinamen) {
            System.out.println("[" + element + "]");
        }
        System.out.println();

        // Sample für das Suchen nach spezifischen Verzeichnissen
        System.out.println("(Verzeichnissuche 'enthält')" + LINIE);
        dateinamen = new ArrayList<>();
        elementName = System.getenv("TEMP");
        sucheNach = "fake";
        System.out.println("Quellverezeichnis: " + elementName);

        dateinamen = GeneralNIOFileAccess.holeAlleUnterverzeichnisseMitNameEnthaeltVonVerzeichnis(elementName,
            sucheNach);
        System.out.println("Suche nach dem String '" + sucheNach + "'");
        System.out.println("Dateien Anzahl Elemente: " + dateinamen.size());
        for (String element : dateinamen) {
            System.out.println("[" + element + "]");
        }
        System.out.println();
        
        // Sample für den TransformTxtToCsvConsumer
        System.out.println("Sample für den TransformTxtToCsvConsumer" + LINIE);
        String ZEILENUMBRUCH = System.lineSeparator();
        String TEMPDIR = System.getenv("TEMP");
        String inFileName = "/testdata/demo.txt";
        String outFileName = TEMPDIR + "/demo.csv";
 
        TransformTxtToCsvConsumer<String> consumer = new TransformTxtToCsvConsumer<>();
        GeneralNIOFileAccess.leseFile(inFileName, DemoGeneralNIOFileAccess.class, StandardCharsets.ISO_8859_1, 1,
            consumer);
        StringBuilder sb = new StringBuilder();
        for (String element : consumer.getData()) {
            // System.out.println("Zeile: " + element);
            if (!(element.replace(";", "").trim().isEmpty())) {
                sb.append(element);
                sb.append(ZEILENUMBRUCH);
            }
        }
        System.out.println(sb.toString());
        System.out.println("\nSchreibe Datei " + outFileName);
        GeneralNIOFileAccess.schreibeFile(outFileName, sb.toString(), Boolean.FALSE, StandardCharsets.ISO_8859_1);

    }
}
