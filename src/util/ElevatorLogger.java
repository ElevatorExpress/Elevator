package util;

/**
 * Class that acts as a wrapper to java.util.logging.Logger. Makes the logger easier to use for this project.
 */
public class ElevatorLogger {
    // Name of the Logger
    private final String internalLoggerName;
    private final long startTime;
    private String colorCode = "";

    /**
     * Create a ElevatorLogger
     * @param ownerName The name of the class that owns this Logger
     */
    public ElevatorLogger(String ownerName) {
        startTime = System.nanoTime();
        internalLoggerName = ownerName;
    }
    /**
     * Create a ElevatorLogger with a color code
     * @param ownerName The name of the class that owns this Logger
     * @param colorCode The desired colorCode of the logger name
     */
    public ElevatorLogger(String ownerName, String colorCode) {
        startTime = System.nanoTime();
        internalLoggerName = ownerName;
        this.colorCode = colorCode;
    }

    /**
     * Logs the specified string
     * @param log The String to log
     */
    private void log(String log){
        // TimeUnit is not working on intelliJ JDK 21. Looked online, seems to be a bug.
        long time = (System.nanoTime() - startTime);
        String time_to_write = ((time / 1000000000) > 0) ? time/1000000000 + " s" : time + " ns";
        System.out.println(colorCode + '[' + time_to_write + " - " + internalLoggerName + "] " + "\u001B[0m" + log );
    }

    /**
     * Logs the specified String at the info level with the internal name as a prefix
     * @param log The String to log
     */
    public void info(String log){
        log(log);
    }

    /**
     * Logs the specified String at the info level with the internal name and "DEBUG: " as a prefix
     * @param log The String to log
     */
    public void debug(String log){
        log("DEBUG: " + log);
    }

}
