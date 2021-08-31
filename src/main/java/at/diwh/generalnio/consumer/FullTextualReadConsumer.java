package at.diwh.generalnio.consumer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * <b>ACHTUNG:</b> Diese Klasse ist nur eine Demo! Im echten Betrieb muss man leseFile aus der general-nio-fileaccess
 * Klasse nur mit Blocksize = 0 aufrufen, dann wird die gesamte Datei gelesen und als Ganzes an den Consumer übergeben!
 *
 * @author 246J
 * @param <T> - Generic
 */
public class FullTextualReadConsumer<T> implements Consumer<T> {

    private StringBuilder data = new StringBuilder();
    private List<String> exceptions = new ArrayList<>();

    @Override
    public void accept(T t) {
        String input = (String) t;
        data.append(input);
    }

    /**
     * @return Liste von Exceptions als String
     */
    public List<String> getExceptions() {
        return new ArrayList<>(this.exceptions); // sodass der Aufrufer nicht ein getExceptions.add... machen kann
    }

    @SuppressWarnings("javadoc")
    public String getData() {
        return data.toString();
    }

    @SuppressWarnings("javadoc")
    public void setData(StringBuilder data) {
        this.data = data;
    }
}
