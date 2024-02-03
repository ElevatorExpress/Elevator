package Messages;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class FloorMessage<T> implements MessageInterface<T, FloorSignal> {

    private final MessageTypes messageType;
    private final String floorID;
    private final Map<String, T> data;
    private final FloorSignal signal;
    private final String id;

    public FloorMessage(MessageTypes messageType, String floorID, Map<String, T> data, FloorSignal signal){
        this.messageType = messageType;
        this.floorID = floorID;
        this.data = data;
        this.signal = signal;
        this.id = UUID.randomUUID().toString();
    }

    @Override
    public MessageTypes getType() {
        return this.messageType;
    }

    @Override
    public FloorSignal getSignal() {
        return this.signal;
    }

    @Override
    public Map<String, T> getData() {
        return this.data;
    }

    @Override
    public String getSenderID() {
        return this.floorID;
    }

    @Override
    public String getMessageId() {
        return this.id;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * <p>
     * The {@code equals} method implements an equivalence relation
     * on non-null object references:
     * <ul>
     * <li>It is <i>reflexive</i>: for any non-null reference value
     *     {@code x}, {@code x.equals(x)} should return
     *     {@code true}.
     * <li>It is <i>symmetric</i>: for any non-null reference values
     *     {@code x} and {@code y}, {@code x.equals(y)}
     *     should return {@code true} if and only if
     *     {@code y.equals(x)} returns {@code true}.
     * <li>It is <i>transitive</i>: for any non-null reference values
     *     {@code x}, {@code y}, and {@code z}, if
     *     {@code x.equals(y)} returns {@code true} and
     *     {@code y.equals(z)} returns {@code true}, then
     *     {@code x.equals(z)} should return {@code true}.
     * <li>It is <i>consistent</i>: for any non-null reference values
     *     {@code x} and {@code y}, multiple invocations of
     *     {@code x.equals(y)} consistently return {@code true}
     *     or consistently return {@code false}, provided no
     *     information used in {@code equals} comparisons on the
     *     objects is modified.
     * <li>For any non-null reference value {@code x},
     *     {@code x.equals(null)} should return {@code false}.
     * </ul>
     *
     * <p>
     * An equivalence relation partitions the elements it operates on
     * into <i>equivalence classes</i>; all the members of an
     * equivalence class are equal to each other. Members of an
     * equivalence class are substitutable for each other, at least
     * for some purposes.
     *
     * @param obj the reference object with which to compare.
     * @return {@code true} if this object is the same as the obj
     * argument; {@code false} otherwise.
     * @implSpec The {@code equals} method for class {@code Object} implements
     * the most discriminating possible equivalence relation on objects;
     * that is, for any non-null reference values {@code x} and
     * {@code y}, this method returns {@code true} if and only
     * if {@code x} and {@code y} refer to the same object
     * ({@code x == y} has the value {@code true}).
     * <p>
     * In other words, under the reference equality equivalence
     * relation, each equivalence class only has a single element.
     * @apiNote It is generally necessary to override the {@link #hashCode() hashCode}
     * method whenever this method is overridden, so as to maintain the
     * general contract for the {@code hashCode} method, which states
     * that equal objects must have equal hash codes.
     * @see #hashCode()
     * @see HashMap
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FloorMessage<?> floorMessage){
            return Objects.equals(floorMessage.id, this.id);
        } return false;
    }
}
