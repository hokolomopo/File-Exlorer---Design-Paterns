import java.util.Date;

abstract class LogComponent{
    abstract String getString();
}

/**
 * Principal message of the log
 */
class LogMessage extends LogComponent{
    private String message;

    public LogMessage(String message) {
        this.message = message;
    }

    @Override
    String getString() {
        return message;
    }
}

/**
 * Abstract Decorator class following Decorator design pattern
 */
abstract class LogDecorator extends LogComponent{
    protected final static String SEPARATOR = " - ";
    protected LogComponent component;
    protected String string;

    public LogDecorator(LogComponent component) {
        this.component = component;
    }

    @Override
    String getString() {
        return string + SEPARATOR + component.getString();
    }
}

/**
 * Decorator giving information on the date
 */
class DateInfo extends LogDecorator {

    public DateInfo(LogComponent component) {
        super(component);
        this.string = new Date().toString();
    }
}

/**
 * Decorator giving information on the operating system
 */
class OSInfo extends LogDecorator {

    public OSInfo(LogComponent component) {
        super(component);
        this.string = System.getProperty("os.name").toLowerCase();
    }
}

/**
 * Decorator giving information on user of the pc
 */
class UserInfo extends LogDecorator {

    public UserInfo(LogComponent component) {
        super(component);
        this.string = System.getProperty("user.name").toLowerCase();
    }
}