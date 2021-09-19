package at.diwh.generalnio.consumer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
/**
 * 
 * @author diwh
 *
 * @param <T> - Generic
 */
public class ReadNxLinesTextConsumer<T> implements Consumer<T> {

    private List<String> data = new ArrayList<>();
    private Integer numberOfLinesToBeRead;
    private Integer counter;
    private List<String> exceptions = new ArrayList<>();



    /**
     * Konstruktor, der verwendet werden MUSS
     * 
     * @param numberOfLinesToBeRead - wie viele Zeilen sollen gelesen werden
     */
    public ReadNxLinesTextConsumer(Integer numberOfLinesToBeRead) {
        super();
        this.numberOfLinesToBeRead = numberOfLinesToBeRead;
        this.counter = Integer.valueOf(0);
    }

    @SuppressWarnings("unused")
    private ReadNxLinesTextConsumer() {
        super();
    }

    @Override
    public void accept(T t) {
        if (this.counter.intValue() < this.numberOfLinesToBeRead.intValue()) {
            String input = (String) t;
            data.add(input);
            this.counter = Integer.valueOf(this.counter.intValue() + 1);
        }
    }

    /**
     * @return Liste von Exceptions als String
     */
    public List<String> getExceptions() {
        return new ArrayList<>(this.exceptions); // sodass der Aufrufer nicht ein getExceptions.add... machen kann
    }

    /**
     * @return the data
     */
    public List<String> getData() {
        return this.data;
    }
}
