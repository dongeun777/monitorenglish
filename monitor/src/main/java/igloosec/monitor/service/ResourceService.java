package igloosec.monitor.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.jdi.VirtualMachine;
import igloosec.monitor.CommonUtil;
import igloosec.monitor.ConfigUtils;
import igloosec.monitor.HttpRequest;
import igloosec.monitor.mapper.HomeMapper;
import igloosec.monitor.mapper.ResourceMapper;
import igloosec.monitor.mapper.ScheduleMapper;
import igloosec.monitor.vo.ResourceVo;
import igloosec.monitor.vo.UsageVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class ResourceService {

    private static final Logger logger = LoggerFactory.getLogger(ResourceService.class);
    public final ResourceMapper mapper;
    public final ScheduleMapper scheduleMapper;
    public final ScheduleService scheduleService;
    public final static int DISK_EXPANSION_MAX_TM = 300000; // 5minute
    //public final static int DISK_EXPANSION_MAX_TM = 60000; // 1minute

    public ResourceService(ResourceMapper mapper, ScheduleMapper scheduleMapper, ScheduleService scheduleService) {
        this.mapper = mapper;
        this.scheduleMapper = scheduleMapper;
        this.scheduleService = scheduleService;
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

    private String logHead = null;

    public boolean addMultiVolume(String rscGrp, int diskSize) {
        logHead = "[MULTIVOLUME ADD]["+rscGrp+"]";
        logger.info("{} Start multivolume append operation", this.logHead);
        logger.info("{} resource group : {} disk size : {}", this.logHead, rscGrp, diskSize);

        String userParam = rscGrp.substring(0, rscGrp.length() - 3);

        String tmPath = ConfigUtils.getConf("tmLogPath");
        if(tmPath == null) {
            logger.error("{} key not exist : tmLogPath", this.logHead);
            return false;
        }

        // Move log files before operation
        this.logFileMove(tmPath + "/tm5disk." + userParam + ".log",
                tmPath + "/completed/tm5disk." + userParam + ".log." + CommonUtil.getCurrentDate());

        // disk expansion shell request
        if(diskExpansionRequest(tmPath, userParam, diskSize) == false) {
            logger.error("{} disk expansion request failed", this.logHead);
            return false;
        }

        // disk expansion standby
        ResourceVo retVo = diskExpansionStandby(tmPath, userParam);
        if(retVo.getPartitionName() == null || retVo.getPartitionName().equals("") || retVo.getPartitionName().equals("/")) {
            logger.error("{} disk expansion standby failed - partition name does not exist", this.logHead);
            return false;
        }

        if(retVo.getDiskName() == null || retVo.getDiskName().equals("") || retVo.getDiskName().equals("/")) {
            logger.error("{} disk expansion standby failed - disk name does not exist", this.logHead);
            return false;
        }

        logger.info("{} disk expansion success - {}, {}, {}", this.logHead, rscGrp, retVo.getPartitionName(), retVo.getDiskName());

        // add multivolume, sparrow api request
        String ip = mapper.selectUserVmIp(rscGrp);
        if(ip == null || ip.equals("")) {
            logger.error("{} User IP information does not exist - {}", this.logHead, rscGrp);
            return false;
        } else {
            ip = ip.trim();
        }

        retVo.setRscparam(rscGrp);

        HttpRequest request = new HttpRequest();
        Map<String, Object> retMap = request.multiVolume(this.logHead, ip, retVo.getRscparam(), retVo.getPartitionName(), "add");
        if(retMap.get("result") == null || ((Boolean)retMap.get("result")) == false) {
            return false;
        }

        // disk usage update
        if(!diskUsageUpdate(rscGrp, ip)) {
            logger.error("{} disk usage update failed - {}, {}, {}", this.logHead, rscGrp, retVo.getPartitionName(), retVo.getDiskName());
            return false;
        }

        // disk name update

        if(!mapper.insertDiskName(retVo)) {
            logger.error("{} disk name insert failed - {}, {}, {}", this.logHead, rscGrp, retVo.getPartitionName(), retVo.getDiskName());
            return false;
        }

        logger.info("{} disk usage update success - {}, {}, {}", this.logHead, rscGrp, retVo.getPartitionName(), retVo.getDiskName());

        return true;

    }

    public boolean delMultiVolume(ResourceVo param) {
        logHead = "[MULTIVOLUME REMOVE]["+param.getRscparam()+"]";
        logger.info("{} Start multivolume delete operation", this.logHead);
        String rscGrp = param.getRscparam();
        String partitionNm = param.getPartitionName();
        String diskNm = param.getDiskName();
        logger.info("{}resource group : {}, partition name : {}, disk name : {}", this.logHead, rscGrp, partitionNm, diskNm);

        String tmPath = ConfigUtils.getConf("tmLogPath");
        if(tmPath == null) {
            logger.error("{} key not exist : tmLogPath", this.logHead);
            return false;
        }

        // remove multivolume
        // remove multivolume, sparrow api request
        String ip = mapper.selectUserVmIp(rscGrp);
        if(ip == null || ip.equals("")) {
            logger.error("{} User IP information does not exist - {}", this.logHead, rscGrp);
            return false;
        } else {
            ip = ip.trim();
        }

        HttpRequest request = new HttpRequest();
        Map<String, Object> retMap = request.multiVolume(this.logHead, ip, rscGrp, partitionNm, "remove");
        if(retMap.get("result") == null || ((Boolean)retMap.get("result")) == false) {
            return false;
        }

        // disk detach shell request
        // Move log files before operation
        this.logFileMove(tmPath + "/tm5diskRemove." + rscGrp + ".log",
                tmPath + "/completed/tm5diskRemove." + rscGrp + ".log." + CommonUtil.getCurrentDate());

        // disk remove shell request
        if(diskRemoveRequest(tmPath, rscGrp, diskNm, partitionNm) == false) {
            logger.error("{} disk remove request failed", this.logHead);
            return false;
        }

        // disk remove standby
        if(diskRemoveStandby(tmPath, rscGrp) == false) {
            logger.error("{} disk remove standby failed", this.logHead);
            return false;
        }

        // db disk delete
        ResourceVo vo = new ResourceVo();
        vo.setRscparam(rscGrp);
        vo.setPartitionName(partitionNm);
        vo.setDiskName(diskNm);
        if(!mapper.deleteDiskName(vo)) {
            logger.error("{} disk name delete failed - {}, {}, {}", this.logHead, rscGrp, partitionNm, diskNm);
            return false;
        }

        // disk usage update
        if(!diskUsageUpdate(rscGrp, ip)) {
            logger.error("{} disk usage update failed - {}, {}, {}", this.logHead, rscGrp, rscGrp, diskNm);
            return false;
        }

        // Move log files before operation
        this.logFileMove(tmPath + "/tm5diskRemove." + rscGrp + ".log",
                tmPath + "/completed/tm5diskRemove." + rscGrp + ".log." + CommonUtil.getCurrentDate());
        return true;

    }

    private boolean diskRemoveRequest(String tmPath, String rscGrp, String diskNm, String partitionNm) {

        boolean retVal = false;

        boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");

        String homeDirectory = System.getProperty("user.home");

        String diskRemoveShell = ConfigUtils.getConf("tmDiskRemoveShell");
        if(diskRemoveShell == null) {
            logger.error("{} key not exist : tmDiskRemoveShell", this.logHead);
            return retVal;
        }

        try {
            Process process;
            String cmd = tmPath + "/" + diskRemoveShell + " " + rscGrp + " " + diskNm + " " + partitionNm;
            if (!isWindows) {
                process = Runtime.getRuntime().exec(cmd);
            }
            logger.info("{} run shell - {}", this.logHead, cmd);

            retVal = true;
        } catch(Exception e) {
            logger.error(CommonUtil.getPrintStackTrace(e));
            retVal = false;
        }

        return retVal;
    }



    private boolean diskUsageUpdate(String rscGrp, String ip) {
        List<ResourceVo> insertList = new ArrayList<ResourceVo>();

        try {
            HttpRequest request = new HttpRequest();
            Map<String, Object> data = request.multiVolume(this.logHead, ip, rscGrp, null, "get");
            if(data.get("data") != null) {
                // parsing
                List<ResourceVo> retList = scheduleService.resourceFileParse(rscGrp, (String)data.get("data"));

                if(retList.size() != 0) {
                    insertList.addAll(retList);
                }
            }
            //System.out.println(request.doPostHttp(vo.getIpAddr().trim()));
        } catch (Exception e) {
            logger.error(CommonUtil.getPrintStackTrace(e));
            return false;
        }

        // db update
        scheduleMapper.insertResourceInfo(insertList);
        return true;
    }

    private boolean diskExpansionRequest(String tmPath, String userParam, int diskSize) {

        boolean retVal = false;

        boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");

        String homeDirectory = System.getProperty("user.home");

        String diskExpansionShell = ConfigUtils.getConf("tmDiskExpansionShell");
        if(diskExpansionShell == null) {
            logger.error("{} key not exist : tmDiskExpansionShell", this.logHead);
            return retVal;
        }

        try {
            Process process;
            String cmd = tmPath + "/" + diskExpansionShell + " " + userParam + " " + diskSize;
            if (!isWindows) {
                process = Runtime.getRuntime().exec(cmd);
            }
            logger.info("{} run shell - {}", this.logHead, cmd);

            retVal = true;
        } catch(Exception e) {
            logger.error(CommonUtil.getPrintStackTrace(e));
            retVal = false;
        }

        return retVal;
    }

    private ResourceVo diskExpansionStandby(String tmPath, String userParam) {
        // partition name
        String partitionNm = null;
        // disk name
        String diskNm = null;

        // return value
        ResourceVo retVo = new ResourceVo();

        boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
        long startTm = System.currentTimeMillis();

        String tmLogPath = tmPath + "/tm5disk." + userParam + ".log";
        String tmLogCompletedPath = tmPath + "/completed/tm5disk." + userParam + ".log." + CommonUtil.getCurrentDate();
        String tmpNm = "/";
        while(true) {
            String result = null;
            BufferedReader br = null;

            try {
                if (isWindows) {
                    br = new BufferedReader(new FileReader("D:\\tm5disk.yongwoonleeiglooseccom.log"));
                } else {
                    br = new BufferedReader(new FileReader(tmLogPath));
                }

                String line = null;
                while ((line = br.readLine()) != null) {
                    if (line.contains("end..")) {
                        partitionNm = tmpNm;
                        break;
                    }

                    // get disk name
                    if(diskNm == null) {
                        if (line.contains("DISK_NAME")) {
                            diskNm = line.split(":")[1].trim();
                        }
                    }

                    // get partition name
                    if(line.contains("SIEM_DATA")) {
                        if(tmpNm.equals("/") == false) {
                            continue;
                        }
                        String[] strArr = line.split("/");
                        for(int i = 0; i < strArr.length; i++) {
                            if(strArr[i].contains("SIEM_DATA")) {
                                String[] siemDataStrArr = strArr[i].split("\\\\");
                                tmpNm += siemDataStrArr[0].split(" ")[0];
                                break;
                            }
                        }
                    }
                }

                if(partitionNm != null) {
                    break;
                }
            } catch (FileNotFoundException fe) {
                continue;
            } catch (IOException e) {
                logger.error(CommonUtil.getPrintStackTrace(e));
            } finally {
                try {
                    if (br != null)
                        br.close();
                } catch (Exception e) {
                    logger.error(CommonUtil.getPrintStackTrace(e));
                }
            }

            // work delay check
            long currentTm = System.currentTimeMillis();
            if (currentTm - startTm > DISK_EXPANSION_MAX_TM) {
                logger.info("{} The disk expansion has passed {} seconds.", this.logHead, (DISK_EXPANSION_MAX_TM / 1000));
                break;
            }

            try {
                Thread.sleep(100);
            } catch(Exception e) {
                logger.error(CommonUtil.getPrintStackTrace(e));
            }
        }

        this.logFileMove(tmLogPath, tmLogCompletedPath);

        /*
        // log file move
        if(partitionNm == null || partitionNm.equals("") || partitionNm.equals("/")) {
            return partitionNm;
        }
         */



        retVo.setPartitionName(partitionNm);
        retVo.setDiskName(diskNm);
        return retVo;
    }

    private boolean diskRemoveStandby(String tmPath, String rscGrp) {
        boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
        long startTm = System.currentTimeMillis();

        String tmLogPath = tmPath + "/tm5diskRemove." + rscGrp + ".log";
        String tmLogCompletedPath = tmPath + "/completed/tm5diskRemove." + rscGrp + ".log." + CommonUtil.getCurrentDate();
        while(true) {
            String result = null;
            BufferedReader br = null;

            try {
                if (isWindows) {
                    br = new BufferedReader(new FileReader("D:\\tm5diskRemove.yongwoonleeiglooseccom.log"));
                } else {
                    br = new BufferedReader(new FileReader(tmLogPath));
                }

                String line = null;
                while ((line = br.readLine()) != null) {
                    if (line.contains("end..")) {
                        return true;
                    }
                }
            } catch (FileNotFoundException fe) {
                continue;
            } catch (IOException e) {
                logger.error(CommonUtil.getPrintStackTrace(e));
            } finally {
                try {
                    if (br != null)
                        br.close();
                } catch (Exception e) {
                    logger.error(CommonUtil.getPrintStackTrace(e));
                }
            }

            // work delay check
            long currentTm = System.currentTimeMillis();
            if (currentTm - startTm > DISK_EXPANSION_MAX_TM) {
                logger.info("{} The disk expansion has passed {} seconds.", this.logHead, (DISK_EXPANSION_MAX_TM / 1000));
                break;
            }

            try {
                Thread.sleep(100);
            } catch(Exception e) {
                logger.error(CommonUtil.getPrintStackTrace(e));
            }
        }

        this.logFileMove(tmLogPath, tmLogCompletedPath);
        return false;
    }


    private boolean logFileMove(String tmLogPath, String tmLogCompletedPath) {
        boolean retVal = false;
        // file move failed -> file remove
        if(CommonUtil.moveFile(tmLogPath, tmLogCompletedPath) == false) {
            //logger.error("log file move failed : {} -> {}", tmLogPath, tmLogCompletedPath);
            File file = new File(tmLogPath);
            if( file.exists() ) {
                if(file.delete()){
                    //logger.info("log file remove success : {}", tmLogPath);
                    return retVal;
                } else {
                    //logger.error("log file remove failed : {}", tmLogPath);
                }
            }else{
                //logger.error("log file does not exist : {}", tmLogPath);
            }
        } else {
            logger.info("{} log file move success : {} -> {}", this.logHead, tmLogPath, tmLogCompletedPath);
            retVal = true;
        }

        return retVal;
    }
}
