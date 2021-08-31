package at.diwh.generalnio.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Dies ist ein Versuch, eine generelle File-Access-Klasse zu bauen; 
 * inspieriert durch FileAccessWithNIO 
 * @author 246J
 *
 */
public class GeneralNIOFileAccess {

    /**
     * Diesen privaten Konstruktor gibt es nur, weil SonarLint sonst an der Klasse meckert, man solle gefälligst einen
     * privaten Konstruktor erstellen. Das ergibt nicht den geringsten Sinn und dieser Konstruktor hier ist so nützlich
     * wie ein Kaugummi mit Reißnägeln aber SonarLint will es so.
     */
    private GeneralNIOFileAccess() {
        super();
    }

    /**
     * Dies ist eine generische Lesemethode für Files. <br/>
     * Diese Files dürfen als Resource (z.B. weil sie unter src/conf liegen) oder als klassisches Filesystem-File
     * vorliegen.<br/>
     * Die Methode leitet das daraus ab, ob <i>clazz</i> gleich <i>null</i> ist, dann ist es ein klassisches File,
     * ansonsten wird es als Resource-Path File gesehen.<br/>
     * Ebenso ist es egal, ob das File textuell ist oder binär, die Methode leitet auch das aus dem Aufruf ab, wenn
     * <i>charset</i> gleich <i>null</i> ist, dann nimmt sie an, es ist ein Binärfile.<br/>
     * <br/>
     * <b>Achtung: Parameter blocksize</b> bedeutet: <br/>
     * Wenn 0 oder kleiner 0: Lies die Datei als <b>ganze</b> Datei. Egal ob Text oder binär. Die <b>gesamte Datei</b>
     * wird gelesen und an den Consumer übergeben. <br/>
     * Wenn größer als 0 ist es abhängig vom Typ des Files: <br/>
     * Bei einem Textfile ist die Zahl egal, wenn sie größer 0 ist wird zeilenweise gelesen und jede Zeile an den
     * Consumer übergeben <br/>
     * Bei einem Binärfile wird in der angebenen Blockgröße gelesen und jeder Block an den Consumer übergeben.
     * 
     * @param <T> - wird abgeleitet aus dem Typ, den der Consumer erwartet
     * @param f - das File als String, also z.B. "d:/WorkD/SVB Schulungen/Diverses/xml.txt" oder als Resource
     *            "/META-INF/irgendeinfile.txt"
     * @param clazz - wenn wir eine Resourcepath-Resource haben; die aufrufende Klasse, also z.B. MeineGuteKlasse.class
     * @param charset - wenn wir ein Textfile lesen, ein Encoding, z.B. java.nio.charset.StandardCharsets.UTF_8
     * @param consumer - eine Implementierung eines java.util.function.Consumer<T>
     * @param blocksize - die Blockgröße; 0 oder eine neg. Zahl bedeutet immer: File im Ganzen lesen. Details siehe
     *            oben.
     * @throws IOException - Fehler
     * @throws URISyntaxException - Fehler
     */
    @SuppressWarnings({"unchecked", "resource"}) // muss leider sein, weil Generics
    public static <T> void leseFile(String f, Class<?> clazz, Charset charset,  int blocksize, Consumer<T> consumer) throws IOException, URISyntaxException {
        Path p = null;
        if (clazz == null) {    // Filesystem-Quelle
            p = Paths.get(f);
        } else {                // Resourcepath-Quelle
            p = Paths.get(clazz.getResource(f).toURI());
        }
        if (charset != null) {  // wenn wir ein Charset haben, haben wir ein Textfile vor uns
            if (blocksize > 0) { // blocksize > 0 heißt zeilenweises Lesen
                BufferedReader bReader = Files.newBufferedReader(p, charset);
                String line = null;
                while ((line = bReader.readLine()) != null) {
                    consumer.accept((T) line);
                }
                bReader.close();
            } else { // blocksize <= 0 heißt: Lies File als ganzes
                consumer.accept((T) new String(Files.readAllBytes(p), charset));
            }
        } else {                // kein Charset bedeutet: Binärfile
            if (blocksize > 0) { // blocksize > 0 heißt: Lies in Blöcken der angegeben blocksize
                InputStream is = Files.newInputStream(p);
                byte[] inBuffer = new byte[blocksize];
                int anzahlBytesRead = 0;
                while ((anzahlBytesRead = is.read(inBuffer)) != -1) {
                    byte[] retBuffer =  Arrays.copyOf(inBuffer, anzahlBytesRead); // sonst hat man ggf. am letzten Block Zeros hinten dran
                    consumer.accept((T) retBuffer);
                }
                is.close();
            } else { // blocksize <= 0 heißt: Lies die Datei als ganzes
                consumer.accept((T) Files.readAllBytes(p));
            }
        }
    }

    /**
     * Diese Methode beschreibt ein beliebiges File im Filesystem mit entweder Bytes oder Strings.<br/>
     * Die Methode erkennt dabei am vorhandensein von <i>charset</i> (also wenn es ungleich <i>null</i> ist),
     * ob der Aufrufer einen Text oder ein Byte-Array speichern wird wollen.
     * @param <T> - der Typ des Datenblocks, String bzw. byte[] sind möglich
     * @param f - das File als String, also z.B. "d:/WorkD/SVB Schulungen/Diverses/xml.txt" (Filesystem Filename) 
     * @param dataToWrite - der zu schreibende Datenblock
     * @param append - true, wenn an eine bestehende Datei angehängt werden soll, false: Datei wird überschrieben
     * @param charset - wenn wir ein Textfile beschreiben, ein Encoding, z.B. java.nio.charset.StandardCharsets.UTF_8
     * @throws IOException - Fehler
     */
    @SuppressWarnings("resource")
    public static <T> void schreibeFile(String f, T dataToWrite, Boolean append, Charset charset) throws IOException {
        Path p = Paths.get(f);
        if (charset != null) {  // Textfile, sonst wäre ein Charset ziemlich sinnlos
            BufferedWriter bWriter;
            if (!append.booleanValue()) { // knallhart überschreiben
                bWriter = Files.newBufferedWriter(p, charset);
            } else {
                bWriter = Files.newBufferedWriter(p, charset, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            }
            bWriter.write((String) dataToWrite);
            bWriter.close();

        } else {                // binäres File
            if (!append.booleanValue()) { // knallhart überschreiben
                Files.write(p, (byte[]) dataToWrite);
            } else {
                Files.write(p, (byte[]) dataToWrite, StandardOpenOption.CREATE, StandardOpenOption.APPEND); 
            }
        }
    }

    /**
     * Liefert als Return-Liste alle Dateien, belegt die übergebene Liste baum mit dem Verzeichnisbaum, also nur den
     * Directory-Einträgen
     * 
     * @param verzeichnis - String, der Ausgangspunkt, z.B. "c:/temp"
     * @param baum - java.nio.file.Path Liste, in der die Verzeichnisse zurück kommen
     * @return - die Liste an java.nio.file.Path mit den Dateien
     * @throws IOException - ja.
     */
    public static List<Path> holeDateibaum(String verzeichnis, List<Path> baum) throws IOException {
        List<Path> result = new ArrayList<>();
        HashSet<FileVisitOption> options = new HashSet<>();
        // options.add(FileVisitOption.FOLLOW_LINKS);
        Files.walkFileTree(Paths.get(verzeichnis), options, 256, new FileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                // System.out.println("preVisitDirectory: " + dir);
                baum.add(dir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                // System.out.println("visitFile: " + file);
                result.add(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                // System.out.println("visitFileFailed: " + file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                // System.out.println("postVisitDirectory: " + dir);
                return FileVisitResult.CONTINUE;
            }
        });
        return result;
    }

    /**
     * @param verzeichnis - String, der das Verzeichnis zum Starten angibt
     * @param nameEnthaelt - String, nach dem im Dateinamen gesucht werden soll
     * @return - eine Liste von Treffern
     * @throws IOException - ja, hoffentlich nicht
     */
    public static List<String> holeAlleFilesMitNameEnthaeltVonVerzeichnis(String verzeichnis, String nameEnthaelt)
            throws IOException {
        List<String> result = null;
        @SuppressWarnings("resource")
        Stream<Path> walk = Files.walk(Paths.get(verzeichnis));
        result = walk.filter(f -> f.toFile().isFile()).map(x -> x.toString()).filter(f -> f.contains(nameEnthaelt))
                .collect(Collectors.toList());
        walk.close();
        return result;
    }

    /**
     * @param verzeichnis - String, der das Verzeichnis zum Starten angibt
     * @param nameEnthaelt - String, nach dem im Verzeichnisnamen gesucht werden soll
     * @return - eine Liste von Treffern
     * @throws IOException - ja, hoffentlich nicht
     */
    public static List<String> holeAlleUnterverzeichnisseMitNameEnthaeltVonVerzeichnis(String verzeichnis,
        String nameEnthaelt) throws IOException {
        List<String> result = null;
        @SuppressWarnings("resource")
        Stream<Path> walk = Files.walk(Paths.get(verzeichnis));
        result = walk.filter(f -> f.toFile().isDirectory()).map(x -> x.toString()).filter(f -> f.contains(nameEnthaelt))
                .collect(Collectors.toList());
        walk.close();
        return result;
    }

    /**
     * @param verzeichnis - String, der das Verzeichnis zum Starten angibt
     * @param nameEndet - String, mit dem der Dateinamen enden soll
     * @return - eine Liste von Treffern
     * @throws IOException - ja, hoffentlich nicht
     */
    public static List<String> holeAlleFilesMitNameEndetVonVerzeichnis(String verzeichnis, String nameEndet)
            throws IOException {
        List<String> result = null;
        @SuppressWarnings("resource")
        Stream<Path> walk = Files.walk(Paths.get(verzeichnis));
        result = walk.filter(f -> f.toFile().isFile()).map(x -> x.toString()).filter(f -> f.endsWith(nameEndet))
                .collect(Collectors.toList());
        walk.close();
        return result;
    }
}
