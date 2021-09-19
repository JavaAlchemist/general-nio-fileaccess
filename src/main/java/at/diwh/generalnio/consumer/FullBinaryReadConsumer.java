package at.diwh.generalnio.consumer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * <b>ACHTUNG:</b> Diese Klasse ist nur eine Demo! Im echten Betrieb muss man leseFile aus der general-nio-fileaccess
 * Klasse nur mit Blocksize = 0 aufrufen, dann wird die gesamte Datei gelesen und als Ganzes an den Consumer übergeben!
 * 
 * @author diwh
 * @param <T> - Generic
 */
public class FullBinaryReadConsumer<T> implements Consumer<T> {

    private ByteArrayOutputStream data = new ByteArrayOutputStream();
    private List<String> exceptions = new ArrayList<>();

    @Override
    public void accept(T t) {
        byte[] input = (byte[]) t;
        try {
            data.write(input);
        }
        catch (IOException e) {
            this.exceptions.add(e.getMessage());
        }
    }

    /**
     * @return Liste von Exceptions als String
     */
    public List<String> getExceptions() {
        return new ArrayList<>(this.exceptions); // sodass der Aufrufer nicht ein getExceptions.add... machen kann
    }

    @SuppressWarnings("javadoc")
    public byte[] getData() {
        return data.toByteArray();
    }

    @SuppressWarnings("javadoc")
    public void setData(ByteArrayOutputStream data) {
        this.data = data;
    }
}
