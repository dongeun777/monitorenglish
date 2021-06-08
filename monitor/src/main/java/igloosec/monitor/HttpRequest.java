package igloosec.monitor;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;


public class HttpRequest {
    public boolean doGet(String email, String company) {
        String urlString = "https://igloocld.com/userRegister?" +
                "email=" + email;
        if(company != null || company.equals("") == false) {
            try {
                company = URLEncoder.encode(company, "UTF-8");
            } catch(UnsupportedEncodingException ue) {
                ue.printStackTrace();
                return false;
            }
            urlString += "&company=" + company;
        }

        HttpsURLConnection httpsConn = null;
        try {
            // Get HTTPS URL connection
            URL url = new URL(urlString);
            httpsConn = (HttpsURLConnection) url.openConnection();

            // Set Hostname verification
            httpsConn.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    // Ignore host name verification. It always returns true.
                    return true;
                }
            });
            // Input setting
            httpsConn.setDoInput(true);
            // Output setting
            // httpsConn.setDoOutput(true);
            // Caches setting
            httpsConn.setUseCaches(false);
            // Read Timeout Setting
            httpsConn.setReadTimeout(10000);
            // Connection Timeout setting
            httpsConn.setConnectTimeout(10000);
            // Method Setting(GET/POST)
            httpsConn.setRequestMethod("GET");
            // Header Setting
            //httpsConn.setRequestProperty("HeaderKey", "HeaderValue");
            int responseCode = httpsConn.getResponseCode();

            // Connect to host
            httpsConn.connect();
            httpsConn.setInstanceFollowRedirects(true);
            // Print response from host
            if (responseCode == HttpsURLConnection.HTTP_OK) { // 정상 호출 200
                return true;
            } else { // 에러 발생
                System.out.println("responseCode : " + responseCode);
                System.out.println("responseMessage : " + httpsConn.getResponseMessage());
                return false;
            }
        } catch (Exception e) {
            System.out.println("error : " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (httpsConn != null) {
                    httpsConn.disconnect();
                }
            } catch(Exception e) {
                e.printStackTrace();
                return false;
            }
        }

    }
}

