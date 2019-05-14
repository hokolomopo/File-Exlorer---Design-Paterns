import java.io.FileWriter;
import java.io.IOException;

public interface LoggerWriter {

    /**
     * Write a string in the log
     *
     * @param s the string to write
     * @return true if the write was successful, false otherwise
     */
    boolean write(String s);
}

class STDOutLogWriter implements LoggerWriter {

    @Override
    public boolean write(String s) {
        System.out.println(s);
        return true;
    }
}

class FileLogWriter implements LoggerWriter{

    private FileWriter writer;

    public FileLogWriter(FileWriter writer) {
        this.writer = writer;
    }

    @Override
    public boolean write(String s) {
        try {
            writer.write(s + "\n");
            writer.flush();
        } catch (IOException e) {
            System.err.println(e.toString());
            return false;
        }
        return true;
    }
}


