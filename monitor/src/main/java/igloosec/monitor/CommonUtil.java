package igloosec.monitor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;

public class CommonUtil {
    // os check
    public static String getOS() {
        return System.getProperty("os.name").toLowerCase();
    }

    public static String getPrintStackTrace(Exception e) {
        StringWriter errors = new StringWriter();
        e.printStackTrace(new PrintWriter(errors));
        return errors.toString();

    }
}
