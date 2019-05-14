import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        if (args.length > 1) {
            System.err.println("Max one argument is allowed");
            System.exit(1);
        }

        try {
            if(args.length == 1){
                File file = new File(args[0]);
                Logger.instantiate(new FileLogWriter(new FileWriter(file, true)));
            }
            else
                Logger.instantiate(new STDOutLogWriter());
        } catch (InstantiationException | IOException e) {
            e.printStackTrace();
        }
        GuiHandler.getInstance();
    }
}
