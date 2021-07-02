package igloosec.monitor;

import igloosec.monitor.service.ResourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

public class CommonUtil {

    private static final Logger logger = LoggerFactory.getLogger(ResourceService.class);

    // os check
    public static String getOS() {
        return System.getProperty("os.name").toLowerCase();
    }

    public static String getIp() {
        String ip = null;
        try {
            URL url_name = new URL("http://bot.whatismyipaddress.com");
            BufferedReader sc = new BufferedReader(new InputStreamReader(url_name.openStream()));
            ip = sc.readLine().trim();
        } catch (Exception e) {
            logger.error(CommonUtil.getPrintStackTrace(e));
        }

        return ip;
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
