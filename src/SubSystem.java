import Messages.MessageInterface;

import java.util.UUID;

public interface SubSystem<I> extends Runnable {
    I[] receiveMessage();
    UUID sendMessage(MessageInterface message);
}
