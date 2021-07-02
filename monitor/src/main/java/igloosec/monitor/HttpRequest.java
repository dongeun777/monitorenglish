package igloosec.monitor;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import igloosec.monitor.controller.HomeController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;


public class HttpRequest {

    private static final Logger logger = LoggerFactory.getLogger(HttpRequest.class);

    public boolean doGetHttps(String email, String company) {
        String urlString = "https://igloocld.com/userRegister?" +
                "email=" + email;
        if (company != null && company.equals("") == false) {
            try {
                company = URLEncoder.encode(company, "UTF-8");
            } catch (UnsupportedEncodingException ue) {
                logger.error(CommonUtil.getPrintStackTrace(ue));
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
                logger.error("responseCode : {}", responseCode);
                logger.error("responseMessage : {}", httpsConn.getResponseMessage());

                return false;
            }
        } catch (Exception e) {
            logger.error(CommonUtil.getPrintStackTrace(e));
            return false;
        } finally {
            try {
                if (httpsConn != null) {
                    httpsConn.disconnect();
                }
            } catch (Exception e) {
                logger.error(CommonUtil.getPrintStackTrace(e));
                return false;
            }
        }

    }

    public boolean doGetHttp(String email, String company) {
        String urlString = "http://localhost:8080/userRegister?" +
                "email=" + email;
        if (company != null && company.equals("") == false) {
            try {
                company = URLEncoder.encode(company, "UTF-8");
            } catch (UnsupportedEncodingException ue) {
                logger.error(CommonUtil.getPrintStackTrace(ue));
                return false;
            }
            urlString += "&company=" + company;
        }

        BufferedReader in = null;
        try {
            URL url = new URL(urlString);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET"); // optional default is GET

            int responseCode = con.getResponseCode();
            in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            return true;
        } catch(Exception e) {
            try {
                if (in != null) {
                    in.close();
                }
            } catch(IOException ie) {
                logger.error(CommonUtil.getPrintStackTrace(ie));

            }
        }

        return true;
    }


    // HTTP POST request
    public String doPostHttp(String uri, String param) throws Exception {

        URL url = new URL(uri);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        con.setRequestMethod("POST"); // HTTP POST 메소드 설정
        con.setDoOutput(true); // POST 파라미터 전달을 위한 설정
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        OutputStream out_stream = con.getOutputStream();

        out_stream.write( param.getBytes("UTF-8") );
        out_stream.flush();
        out_stream.close();

        InputStream is      = null;
        BufferedReader in   = null;
        String data         = "";

        is  = con.getInputStream();
        in  = new BufferedReader(new InputStreamReader(is), 8 * 1024);

        String line = null;
        StringBuffer buff   = new StringBuffer();

        while ( ( line = in.readLine() ) != null ) {
            buff.append(line + "\n");
        }
        data    = buff.toString().trim();

        return data;
    }

    public Map<String, Object> multiVolume(String logHead, String ip, String rscGrp, String partitionName, String jobType) {
        Map<String, Object> retMap = new HashMap<String, Object>();

        retMap.put("result", false);
        String uri = "http://" + ip + ":8983/solr/indexer.json";
        String param    = "wt=json&type=TARGET&action=DISK&partition=true";
        try {
            if(jobType.equals("get") == false) {
                if (jobType.equals("add") == true) {         // add multivolume
                    param += "&partition_add=true";
                } else if (jobType.equals("remove") == true) {  // remove multivolume
                    param += "&partition_add=false";
                } else {
                    logger.error("multivolume error - jobType is {}", jobType);
                    return retMap;
                }
                param += "&partition_pri=" + ConfigUtils.getConf("partitionPri");
                param += "&partition_limit=" + ConfigUtils.getConf("partitionLimit");
                param += "&partition_name=" + partitionName;
            }

            if(jobType.equals("get") == false) {
                logger.info("{} {} multivolume request uri : {}", logHead, jobType, uri);
                logger.info("{} {} multivolume request param : {}", logHead, jobType, param);
            }

            String data = doPostHttp(uri, param);
            retMap.put("data", data);
            JsonObject obj = new JsonParser().parse(data).getAsJsonObject();
            JsonObject resObj = (JsonObject) obj.get("responseHeader");
            if(resObj.get("status").getAsInt() != 0) {
                logger.error("{} {} multivolume error - {}", logHead, jobType, data);
                return retMap;
            }
            if(jobType.equals("get") == false) {
                logger.info("{} {} multivolume response : {}", logHead, jobType, data);
            }
            retMap.put("result", true);
        } catch(Exception e) {
            logger.error("{}, {}, {}", rscGrp, uri, param);
            logger.error(e.getMessage());
            //logger.error(CommonUtil.getPrintStackTrace(e));
            return retMap;
        }

        if(jobType.equals("get") == false) {
            logger.info("{} {} multivolume success - {}, {}", logHead, jobType, rscGrp, partitionName);
        }

        return retMap;
    }
}

