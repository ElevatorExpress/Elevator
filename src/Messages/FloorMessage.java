package Messages;

import java.util.Map;
import java.util.Objects;

/**
 * Represents Messages that are sent by the FloorSystem. This record should only be instanced without a factory if
 * the id parameter is a random UUID. <br>
 * More details about the message protocol used can be found
 * <a href="https://github.com/ElevatorExpress/Elevator/wiki/Message-Protocol">here</a>.
 * @param messageType The type of the message
 * @param floorID The sender's unique ID
 * @param data The data contained inside the message
 * @param signal The new state information
 * @param id The unique id of the message
 * @param <T> Generic type representing data to be placed inside a data map
 */
public record FloorMessage<T>(MessageTypes messageType, String floorID, Map<String, T> data, Signal signal,
                              String id) implements MessageInterface<T> {


    /**
     * @return The type of the message
     */
    @Override
    public MessageTypes getType() {
        return messageType;
    }

    /**
     * @return The new state information inside the message
     */
    @Override
    public Signal getSignal() {
        return signal;
    }

    /**
     * @return The data contained inside the message
     */
    @Override
    public Map<String, T> getData() {
        return data;
    }

    /**
     * @return The unique ID of the sends of this message
     */
    @Override
    public String getSenderID() {
        return floorID;
    }

    /**
     * @return The unique ID of this message
     */
    @Override
    public String getMessageId() {
        return id;
    }

    /**
     * Indicates whether some other object is "equal to" this one.  In addition
     * to the general contract of {@link Object#equals(Object) Object.equals},
     * record classes must further obey the invariant that when
     * a record instance is "copied" by passing the result of the record component
     * accessor methods to the canonical constructor, as follows:
     * <pre>
     *     R copy = new R(r.c1(), r.c2(), ..., r.cn());
     * </pre>
     * then it must be the case that {@code r.equals(copy)}.
     *
     * @param obj the reference object with which to compare.
     * @return {@code true} if this record is equal to the
     * argument; {@code false} otherwise.
     * @implSpec The implicitly provided implementation returns {@code true} if
     * and only if the argument is an instance of the same record class
     * as this record, and each component of this record is equal to
     * the corresponding component of the argument; otherwise, {@code
     * false} is returned. Equality of a component {@code c} is
     * determined as follows:
     * <ul>
     *
     * <li> If the component is of a reference type, the component is
     * considered equal if and only if {@link
     * Objects#equals(Object, Object)
     * Objects.equals(this.c, r.c)} would return {@code true}.
     *
     * <li> If the component is of a primitive type, using the
     * corresponding primitive wrapper class {@code PW} (the
     * corresponding wrapper class for {@code int} is {@code
     * java.lang.Integer}, and so on), the component is considered
     * equal if and only if {@code
     * PW.compare(this.c, r.c)} would return {@code 0}.
     * </ul>
     * <p>
     * Apart from the semantics described above, the precise algorithm
     * used in the implicitly provided implementation is unspecified
     * and is subject to change. The implementation may or may not use
     * calls to the particular methods listed, and may or may not
     * perform comparisons in the order of component declaration.
     * @see Objects#equals(Object, Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FloorMessage<?> floorMessage) {
            return Objects.equals(floorMessage.id, id);
        }
        return false;
    }
}
