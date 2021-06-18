package igloosec.monitor.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import igloosec.monitor.ConfigUtils;
import igloosec.monitor.HttpRequest;
import igloosec.monitor.mapper.HomeMapper;
import igloosec.monitor.mapper.ResourceMapper;
import igloosec.monitor.vo.ResourceVo;
import igloosec.monitor.vo.UsageVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

@Service
public class ResourceService {

    private static final Logger logger = LoggerFactory.getLogger(ResourceService.class);
    public final ResourceMapper mapper;
    public final static int DISK_EXPANSION_MAX_TM = 300000; // 5분
    //public final static int DISK_EXPANSION_MAX_TM = 60000; // 1분

    public ResourceService(ResourceMapper mapper) {
        this.mapper = mapper;
    }

    public List<ResourceVo> selectResourceList(ResourceVo param) {
        return mapper.selectResourceList(param);
    }

    public List<ResourceVo> selectDiskUsageList(ResourceVo param) {
        return mapper.selectDiskUsageList(param);
    }

    public ResourceVo selectResourceUsage(ResourceVo param) {
        return mapper.selectResourceUsage(param);
    }

    public List<ResourceVo> selectDiskPrice() {
        return mapper.selectDiskPrice();
    }

    public boolean addMultiVolume(String rscGrp, int diskSize) {

        logger.info("Start multivolume append operation");
        logger.info("resource group : {} disk size : {}", rscGrp, diskSize);

        String userParam = rscGrp.substring(0, rscGrp.length() - 3);

        String tmPath = ConfigUtils.getConf("tmLogPath");
        if(tmPath == null) {
            logger.error("key not exist : tmLogPath");
            return false;
        }

        // disk expansion shell request
        if(diskExpansionRequest(tmPath, userParam, diskSize) == false) {
            logger.error("disk expansion failed");
            return false;
        }

        // disk expansion standby
        String partitionName = diskExpansionStandby(tmPath, userParam);
        if(partitionName == null || partitionName.equals("")) {
            return false;
        }

        logger.info("disk expansion success - {}, {}", rscGrp, partitionName);

        // add multivolume, sparrow api request
        String ip = mapper.selectUserVmIp(rscGrp);
        if(ip == null || ip.equals("")) {
            logger.error("User IP information does not exist - {}", rscGrp);
            return false;
        }
        HttpRequest request = new HttpRequest();

        try {
            String data = request.doPostHttp(ip, partitionName);
            JsonObject obj = new JsonParser().parse(data).getAsJsonObject();
            JsonObject resObj = (JsonObject) obj.get("responseHeader");
            if(resObj.get("status").getAsInt() != 0) {
                logger.error("multivolume add error - {}", data);
                return false;
            }
        } catch(Exception e) {
            logger.error(e.getMessage());
        }

        logger.info("multivolume add success - {}", rscGrp);

        return true;

    }

    private boolean diskExpansionRequest(String tmPath, String userParam, int diskSize) {

        boolean retVal = false;

        boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");

        String homeDirectory = System.getProperty("user.home");

        String diskExpansionShell = ConfigUtils.getConf("tmDiskExpansionShell");
        if(diskExpansionShell == null) {
            logger.error("key not exist : tmDiskExpansionShell");
            return retVal;
        }

        try {
            Process process;
            String cmd = tmPath + "/" + diskExpansionShell + " " + userParam + " " + diskSize;
            if (!isWindows) {
                process = Runtime.getRuntime().exec(cmd);
            }
            logger.info("run shell - {}", cmd);

            retVal = true;
        } catch(Exception e) {
            logger.error(e.getMessage());
            retVal = false;
        }

        return retVal;
    }

    private String diskExpansionStandby(String tmPath, String userParam) {
        boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
        long startTm = System.currentTimeMillis();

        String partitionName = "";

        while(true) {
            String result = null;
            BufferedReader br = null;
            try {
                if (isWindows) {
                    br = new BufferedReader(new FileReader("D:\\tm5disk." + userParam + ".log"));
                } else {
                    br = new BufferedReader(new FileReader(tmPath + "/tm5disk." + userParam + ".log"));
                }
                String line = null;
                while ((line = br.readLine()) != null) {
                    if (line.contains("end..")) {
                        return partitionName;
                    }

                    // get partition name
                    if(line.contains("SIEM_DATA")) {
                        String[] strArr = line.split("/");
                        for(int i = 0; i < strArr.length; i++) {
                            if(strArr[i].equals("data")) {
                                partitionName += "data/";
                            } else if(strArr[i].contains("SIEM_DATA")) {
                                String[] siemDataStrArr = strArr[i].split("\\\\");
                                partitionName += siemDataStrArr[0];
                            }
                        }
                    }
                }
            } catch (IOException ioe) {
                logger.error(ioe.getMessage());
            } finally {
                try {
                    if (br != null)
                        br.close();
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }

            // 5분 이상 디스크 확장 작업이 지연되면 실패 처리
            long currentTm = System.currentTimeMillis();
            if (currentTm - startTm > DISK_EXPANSION_MAX_TM) {
                logger.info("The disk expansion has passed {} seconds.", (DISK_EXPANSION_MAX_TM / 1000));
                break;
            }

            try {
                Thread.sleep(100);
            } catch(Exception e) {
                logger.error(e.getMessage());
            }
        }

        return partitionName;
    }
}
