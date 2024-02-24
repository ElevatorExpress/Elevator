package util;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ElevatorLogger {

    private final String internalLoggerName;

    public ElevatorLogger(String ownerName) {
        internalLoggerName = ownerName;
    }

    private void log(String log, Level level){
        Logger l = Logger.getLogger(internalLoggerName);
        String sb = internalLoggerName + ": " + log;
        l.log(level, sb);
    }

    public void info(String log){
        log('\n' + log + '\n', Level.INFO);
    }

    public void debug(String log){
        log("\nDEBUG: " + log + '\n', Level.ALL);
    }

}
