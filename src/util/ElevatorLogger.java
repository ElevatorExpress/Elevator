package util;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class that acts as a wrapper to java.util.logging.Logger. Makes the logger easier to use for this project.
 */
public class ElevatorLogger {
    // Name of the Logger
    private final String internalLoggerName;

    /**
     * Create a ElevatorLogger
     * @param ownerName The name of the class that owns this Logger
     */
    public ElevatorLogger(String ownerName) {
        internalLoggerName = ownerName;
    }

    /**
     * Logs the specified string
     * @param log The String to log
     * @param level The log level
     */
    private void log(String log, Level level){
        Logger l = Logger.getLogger(internalLoggerName);
        String sb = internalLoggerName + ": " + log;
        l.log(level, sb);
    }

    /**
     * Logs the specified String at the info level with the internal name as a prefix
     * @param log The String to log
     */
    public void info(String log){
        log('\n' + log + '\n', Level.INFO);
    }

    /**
     * Logs the specified String at the info level with the internal name and "DEBUG: " as a prefix
     * @param log The String to log
     */
    public void debug(String log){
        log("\nDEBUG: " + log + '\n', Level.ALL);
    }

}
