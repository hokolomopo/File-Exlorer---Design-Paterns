public class Logger {
    private static Logger INSTANCE;
    private static LoggerWriter WRITER;

    private Logger(){}

    /**
     * Instantiate the Logger with the given writer
     *
     * @param loggerWriter a LogWriter to write the logs
     * @throws InstantiationException if the logger was already instantiated
     */
    public static void instantiate(LoggerWriter loggerWriter) throws InstantiationException {
        if(WRITER != null)
            throw new InstantiationException("Logger is already instantiated");
        WRITER = loggerWriter;
        INSTANCE = new Logger();
    }

    //Get instance of the Logger (Singleton design pattern)
    public static Logger getInstance() {
        if(INSTANCE == null){
            if(WRITER == null)
                WRITER = new STDOutLogWriter();
            INSTANCE = new Logger();
        }

        return INSTANCE;

    }

    //Write the string in the logs
    public void log(String s){
        String l = new DateInfo(new UserInfo(new OSInfo(new LogMessage(s)))).getString();
        WRITER.write(l);
    }

}
