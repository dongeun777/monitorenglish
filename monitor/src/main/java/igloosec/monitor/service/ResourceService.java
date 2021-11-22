package igloosec.monitor.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
    public final static int DISK_EXPANSION_MAX_TM = 300000; // 5minute
    //public final static int DISK_EXPANSION_MAX_TM = 60000; // 1minute

    public ResourceService(ResourceMapper mapper, ScheduleMapper scheduleMapper) {
        this.mapper = mapper;
        this.scheduleMapper = scheduleMapper;
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

    private boolean diskRemoveRequest(String logHead, String tmPath, String rscGrp, String diskNm, String partitionNm) {

        boolean retVal = false;

        boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");

        String homeDirectory = System.getProperty("user.home");

        String diskRemoveShell = ConfigUtils.getConf("tmDiskRemoveShell");
        if(diskRemoveShell == null) {
            logger.error("{} key not exist : tmDiskRemoveShell", logHead);
            return retVal;
        }

        try {
            Process process;
            String cmd = tmPath + "/" + diskRemoveShell + " " + rscGrp + " " + diskNm + " " + partitionNm;
            if (!isWindows) {
                process = Runtime.getRuntime().exec(cmd);
            }
            logger.info("{} run shell - {}", logHead, cmd);

            retVal = true;
        } catch(Exception e) {
            logger.error(CommonUtil.getPrintStackTrace(e));
            retVal = false;
        }

        return retVal;
    }



    private boolean diskUsageUpdate(String logHead, String rscGrp, String ip) {
        List<ResourceVo> insertList = new ArrayList<ResourceVo>();

        try {
            HttpRequest request = new HttpRequest();
            Map<String, Object> data = request.multiVolume(logHead, ip, rscGrp, null, "get");
            if(data.get("data") != null) {
                // parsing
                List<ResourceVo> retList = resourceFileParse(rscGrp, (String)data.get("data"));

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

    private boolean diskExpansionRequest(String logHead, String tmPath, String userParam, int diskSize) {

        boolean retVal = false;

        boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");

        String homeDirectory = System.getProperty("user.home");

        String diskExpansionShell = ConfigUtils.getConf("tmDiskExpansionShell");
        if(diskExpansionShell == null) {
            logger.error("{} key not exist : tmDiskExpansionShell", logHead);
            return retVal;
        }

        try {
            Process process;
            String cmd = tmPath + "/" + diskExpansionShell + " " + userParam + " " + diskSize;
            if (!isWindows) {
                process = Runtime.getRuntime().exec(cmd);
            }
            logger.info("{} run shell - {}", logHead, cmd);

            retVal = true;
        } catch(Exception e) {
            logger.error(CommonUtil.getPrintStackTrace(e));
            retVal = false;
        }

        return retVal;
    }

    private ResourceVo diskStandby(String logHead, String tmPath, String userParam, String jobType) {
        // partition name
        String partitionNm = null;
        // disk name
        String diskNm = null;

        // return value
        ResourceVo retVo = new ResourceVo();
        retVo.setDiskWorkResult(false);

        boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
        long startTm = System.currentTimeMillis();

        String logFileNm = null;
        if(jobType.equals("add") == true) {
            logFileNm = "/tm5disk.";
        } else if(jobType.equals("remove") == true) {
            logFileNm = "/tm5diskRemove.";
        } else {
            logger.error("{} work type not valid", logHead, jobType);
            return null;
        }

        String tmLogPath = tmPath + logFileNm + userParam + ".log";
        String tmLogCompletedPath = tmPath + "/completed" + logFileNm + userParam + ".log." + CommonUtil.getCurrentDate();

        logger.info("{} Check log file (end..), path : {}", logHead, tmLogPath);
        String tmpNm = "/";
        while(true) {
            String result = null;
            BufferedReader br = null;

            try {
                if (isWindows) {
                    br = new BufferedReader(new FileReader("D:\\tm5diskRemove.yongwoonleeiglooseccomRsg.log"));
                } else {
                    br = new BufferedReader(new FileReader(tmLogPath));
                }

                String line = null;
                while ((line = br.readLine()) != null) {
                    if (line.contains("end..")) {
                        partitionNm = tmpNm;
                        retVo.setDiskWorkResult(true);
                        if(jobType.equals("add") == true) {
                            break;
                        } else if(jobType.equals("remove") == true) {
                            return retVo;
                        }

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
                logger.error("{} disk operation delay {} seconds.", logHead, (DISK_EXPANSION_MAX_TM / 1000));
                break;
            }

            try {
                Thread.sleep(100);
            } catch(Exception e) {
                logger.error(CommonUtil.getPrintStackTrace(e));
            }
        }

        this.logFileMove(logHead, tmLogPath, tmLogCompletedPath);

        retVo.setPartitionName(partitionNm);
        retVo.setDiskName(diskNm);
        return retVo;
    }


    private boolean logFileMove(String logHead, String tmLogPath, String tmLogCompletedPath) {
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
            logger.info("{} log file move success : {} -> {}", logHead, tmLogPath, tmLogCompletedPath);
            retVal = true;
        }

        return retVal;
    }





    public ResourceVo requestExpansionShell(String rscGrp, int diskSize) {
        String logHead = "[MULTIVOLUME ADD]["+rscGrp+"]";
        logger.info("{} Start multivolume append operation", logHead);
        logger.info("{} resource group : {} disk size : {}", logHead, rscGrp, diskSize);

        // disk_work_history init
        ResourceVo vo = new ResourceVo();
        vo.setRscparam(rscGrp);
        vo.setJobType("add");
        vo.setDiskSize(diskSize);
        vo.setDiskWorkResult(false);

        int idx = mapper.selectMaxIdxDiskWorkHistory(vo);
        vo.setIdx(idx);

        if(mapper.initDiskWorkHistory(vo) == false) {
            logger.error("{} initDiskWorkHistory error", logHead);
            return vo;
        }
        String userParam = rscGrp.substring(0, rscGrp.length() - 3);

        String tmPath = ConfigUtils.getConf("tmLogPath");
        if(tmPath == null) {
            logger.error("{} key not exist : tmLogPath", logHead);
            return vo;
        }

        // Move log files before operation
        this.logFileMove(logHead, tmPath + "/tm5disk." + userParam + ".log",
                tmPath + "/completed/tm5disk." + userParam + ".log." + CommonUtil.getCurrentDate());

        // disk expansion shell request
        if(diskExpansionRequest(logHead, tmPath, userParam, diskSize) == false) {
            logger.error("{} disk expansion request failed", logHead);
            return vo;
        }

        vo.setDiskWorkResult(true);
        return vo;
    }

    public boolean waitDiskExpansionComplete(String rscGrp, int idx) {
        String logHead = "[MULTIVOLUME ADD]["+rscGrp+"]";
        logger.info("{} Wait for disk expansion to complete", logHead);
        logger.info("{} resource group : {} idx : {}", logHead, rscGrp, idx);

        ResourceVo vo = new ResourceVo();
        vo.setRscparam(rscGrp);
        vo.setJobType("add");

        String userParam = rscGrp.substring(0, rscGrp.length() - 3);

        String tmPath = ConfigUtils.getConf("tmLogPath");
        if(tmPath == null) {
            logger.error("{} key not exist : tmLogPath", logHead);
            return false;
        }

        // disk expansion wait
        ResourceVo retVo = diskStandby(logHead, tmPath, userParam, "add");
        if(retVo.isDiskWorkResult() == false || retVo.getPartitionName() == null
                || retVo.getPartitionName().equals("") || retVo.getPartitionName().equals("/")) {
            logger.error("{} disk expansion standby failed - partition name does not exist", logHead);
            return false;
        }

        if(retVo.isDiskWorkResult() == false || retVo.getPartitionName() == null
                || retVo.getPartitionName().equals("") || retVo.getPartitionName().equals("/")) {
            logger.error("{} disk expansion standby failed - partition name does not exist", logHead);
            return false;
        }

        retVo.setRscparam(rscGrp);
        retVo.setIdx(idx);

        logger.info("{} disk expansion success - {}, {}, {}", logHead, rscGrp, retVo.getPartitionName(), retVo.getDiskName());

        // add multivolume, sparrow api request
        String ip = mapper.selectUserVmIp(rscGrp);
        if(ip == null || ip.equals("")) {
            logger.error("{} User IP information does not exist - {}", logHead, rscGrp);
            return false;
        } else {
            ip = ip.trim();
        }

        HttpRequest request = new HttpRequest();
        Map<String, Object> retMap = request.multiVolume(logHead, ip, retVo.getRscparam(), retVo.getPartitionName(), "add");
        if(retMap.get("result") == null || ((Boolean)retMap.get("result")) == false) {
            return false;
        }

        // disk usage update
        if(!diskUsageUpdate(logHead, rscGrp, ip)) {
            logger.error("{} disk usage update failed(USER_DISK_USAGE) - {}, {}, {}", logHead, rscGrp, retVo.getPartitionName(), retVo.getDiskName());
            return false;
        }

        logger.info("{} disk usage update success(USER_DISK_USAGE) - {}, {}, {}", logHead, rscGrp, retVo.getPartitionName(), retVo.getDiskName());

        // disk name insert
        if(!mapper.insertDiskName(retVo)) {
            logger.error("{} disk name insert failed(USER_DISK_USAGE) - {}, {}, {}", logHead, rscGrp, retVo.getPartitionName(), retVo.getDiskName());
            return false;
        }

        logger.info("{} disk name insert success(USER_DISK_USAGE) - {}, {}, {}", logHead, rscGrp, retVo.getPartitionName(), retVo.getDiskName());

        // disk work history update
        if(!mapper.updateDiskWorkHistory(retVo)) {
            logger.error("{} disk operation history update Failed(disk_work_history) - {}, {}, {}, {}", logHead, rscGrp, idx, retVo.getPartitionName(), retVo.getDiskName());
            return false;
        }

        logger.info("{} disk operation history update success(disk_work_history) - {}, {}, {}, {}", logHead, rscGrp, idx, retVo.getPartitionName(), retVo.getDiskName());

        return true;
    }

    public ResourceVo requestRemoveShell(String rscGrp, String partitionNm, String diskNm) {
        String logHead = "[MULTIVOLUME REMOVE]["+rscGrp+"]";
        logger.info("{} Start multivolume delete operation", logHead);
        logger.info("{} resource group : {}, partition name : {}, disk name : {}", logHead, rscGrp, partitionNm, diskNm);

        // disk_work_history init
        ResourceVo vo = new ResourceVo();
        vo.setRscparam(rscGrp);
        vo.setJobType("remove");
        vo.setDiskWorkResult(false);
        vo.setPartitionName(partitionNm);
        vo.setDiskName(diskNm);

        int idx = mapper.selectMaxIdxDiskWorkHistory(vo);
        vo.setIdx(idx);

        if(mapper.initDiskWorkHistory(vo) == false) {
            logger.error("{} initDiskWorkHistory error", logHead);
            return vo;
        }

        String tmPath = ConfigUtils.getConf("tmLogPath");
        if(tmPath == null) {
            logger.error("{} key not exist : tmLogPath", logHead);
            return vo;
        }

        // remove multivolume
        // remove multivolume, sparrow api request
        String ip = mapper.selectUserVmIp(rscGrp);
        if(ip == null || ip.equals("")) {
            logger.error("{} User IP information does not exist - {}", logHead, rscGrp);
            return vo;
        } else {
            ip = ip.trim();
        }

        HttpRequest request = new HttpRequest();
        Map<String, Object> retMap = request.multiVolume(logHead, ip, rscGrp, partitionNm, "remove");
        if(retMap.get("result") == null || ((Boolean)retMap.get("result")) == false) {
            return vo;
        }

        // disk detach shell request
        // Move log files before operation
        this.logFileMove(logHead, tmPath + "/tm5diskRemove." + rscGrp + ".log",
                tmPath + "/completed/tm5diskRemove." + rscGrp + ".log." + CommonUtil.getCurrentDate());

        // disk remove shell request
        if(diskRemoveRequest(logHead, tmPath, rscGrp, diskNm, partitionNm) == false) {
            logger.error("{} disk remove request failed", logHead);
            return vo;
        }

        vo.setDiskWorkResult(true);
        return vo;
    }

    public boolean waitDiskRemoveComplete(String rscGrp, int idx, String partitionNm, String diskNm) {
        String logHead = "[MULTIVOLUME REMOVE]["+rscGrp+"]";
        logger.info("{} Wait for disk remove to complete", logHead);
        logger.info("{} resource group : {} idx : {}", logHead, rscGrp, idx);

        ResourceVo vo = new ResourceVo();
        vo.setRscparam(rscGrp);
        vo.setJobType("remove");
        vo.setPartitionName(partitionNm);
        vo.setDiskName(diskNm);
        vo.setIdx(idx);

        String tmPath = ConfigUtils.getConf("tmLogPath");
        if(tmPath == null) {
            logger.error("{} key not exist : tmLogPath", logHead);
            return false;
        }

        // remove multivolume, sparrow api request
        String ip = mapper.selectUserVmIp(rscGrp);
        if(ip == null || ip.equals("")) {
            logger.error("{} User IP information does not exist - {}", logHead, rscGrp);
            return false;
        } else {
            ip = ip.trim();
        }


        // disk remove standby
        ResourceVo retVo = diskStandby(logHead, tmPath, rscGrp, "remove");
        if(retVo.isDiskWorkResult() == false) {
            logger.error("{} disk remove standby failed - partition name does not exist", logHead);
            return false;
        }

        // db disk delete
        if(!mapper.deleteDiskName(vo)) {
            logger.error("{} disk name delete failed(USER_DISK_USAGE) - {}, {}, {}", logHead, rscGrp, partitionNm, diskNm);
            return false;
        }

        logger.info("{} disk name delete success(USER_DISK_USAGE) - {}, {}, {}", logHead, rscGrp, partitionNm, diskNm);

        // disk usage update
        if(!diskUsageUpdate(logHead, rscGrp, ip)) {
            logger.error("{} disk usage update failed(USER_DISK_USAGE) - {}, {}, {}", logHead, rscGrp, rscGrp, diskNm);
            return false;
        }

        logger.info("{} disk usage update success(USER_DISK_USAGE) - {}, {}, {}", logHead, rscGrp, partitionNm, diskNm);

        // disk work history update
        if(!mapper.updateDiskWorkHistory(vo)) {
            logger.error("{} disk operation history update Failed(disk_work_history) - {}, {}, {}, {}", logHead, rscGrp, idx, partitionNm, diskNm);
            return false;
        }


        logger.info("{} disk operation history update success(disk_work_history) - {}, {}, {}, {}", logHead, rscGrp, idx, partitionNm, diskNm);

        // Move log files before operation
        this.logFileMove(logHead, tmPath + "/tm5diskRemove." + rscGrp + ".log",
                tmPath + "/completed/tm5diskRemove." + rscGrp + ".log." + CommonUtil.getCurrentDate());

        return true;
    }

    public boolean checkDiskWork(String rscparam) {
        boolean retVal = false;


        // work check 1 : working(true), 0 : not working(false)
        if(mapper.checkDiskWork(rscparam) == 0) {
            retVal = false;
        } else {
            retVal = true;
        }

        return retVal;
    }

    public List<ResourceVo> resourceFileParse(String rsgGrp, String data) {

        List<ResourceVo> list = new ArrayList<ResourceVo>();
        JsonObject obj = new JsonParser().parse(data).getAsJsonObject();

        JsonArray arr = (JsonArray)obj.get("list");

        for(int i = 0; i < arr.size(); i++) {
            if(arr.get(i).isJsonPrimitive() == false) {
                ResourceVo vo = new ResourceVo();
                vo.setRscparam(rsgGrp);
                vo.setPartitionName(arr.get(i).getAsJsonObject().getAsJsonPrimitive("name").getAsString());
                vo.setPriority(arr.get(i).getAsJsonObject().getAsJsonPrimitive("priority").getAsInt());
                vo.setLimits(arr.get(i).getAsJsonObject().getAsJsonPrimitive("limit").getAsInt());
                vo.setTotal(arr.get(i).getAsJsonObject().getAsJsonPrimitive("total").getAsString());
                vo.setFree(arr.get(i).getAsJsonObject().getAsJsonPrimitive("free").getAsString());
                vo.setCurrent(arr.get(i).getAsJsonObject().getAsJsonPrimitive("current").getAsString());
                vo.setData(arr.get(i).getAsJsonObject().getAsJsonPrimitive("data").getAsString());
                list.add(vo);
            }
        }

        //logger.info("[DISK PULLING] [{}] parse success", rsgGrp);

        return list;
    }
}
