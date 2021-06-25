package igloosec.monitor;

import igloosec.monitor.service.ResourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CommonUtil {

    private static final Logger logger = LoggerFactory.getLogger(ResourceService.class);

    // os check
    public static String getOS() {
        return System.getProperty("os.name").toLowerCase();
    }

    public static String getPrintStackTrace(Exception e) {
        StringWriter errors = new StringWriter();
        e.printStackTrace(new PrintWriter(errors));
        return errors.toString();
    }

    public static boolean moveFile(String receivedPath, String completePath) {
        // file move
        try {
            Path received = Path.of(receivedPath);
            Path complete = Path.of(completePath);
            Files.createDirectories(complete.getParent());
            Files.move(received , complete);
            return true;
        } catch(Exception e) {
            //logger.error(CommonUtil.getPrintStackTrace(e));
            return false;
        }
    }

    public static String getCurrentDate() {
        Date nowDate = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        return sdf.format(nowDate);
    }

}
