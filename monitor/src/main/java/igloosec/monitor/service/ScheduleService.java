package igloosec.monitor.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.table.*;
import igloosec.monitor.CommonUtil;
import igloosec.monitor.ConfigUtils;
import igloosec.monitor.HttpRequest;
import igloosec.monitor.mapper.HomeMapper;
import igloosec.monitor.mapper.MemberMapper;
import igloosec.monitor.mapper.ResourceMapper;
import igloosec.monitor.mapper.ScheduleMapper;
import igloosec.monitor.vo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ScheduleService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleService.class);

    public final ScheduleMapper mapper;
    public final MemberMapper memberMapper;
    public final HomeMapper homeMapper;
    public final ResourceMapper resourceMapper;
    public final ResourceService resourceService;

    public ScheduleService(ScheduleMapper mapper, MemberMapper memberMapper, HomeMapper homeMapper, ResourceMapper resourceMapper, ResourceService resourceService) {
        this.mapper = mapper;
        this.memberMapper = memberMapper;
        this.homeMapper = homeMapper;
        this.resourceMapper = resourceMapper;
        this.resourceService = resourceService;
    }

    @Value("${azure.marketplace.account.name}")
    private String accountName;

    @Value("${azure.marketplace.account.key}")
    private String accountKey;

    @Value("${azure.marketplace.table.name}")
    private String tableName;

    @Autowired
    Environment environment;

    /**
     * leads data
     */
    //@Scheduled(cron = "0/10 * * * * *")  // 10초마다
    @Scheduled(cron = "* * * * * *")  // 1초마다
    public void getLeadsInfo() {

        String os = CommonUtil.getOS();

        if (os.contains("win")) {
            return;
        }

        String ip = CommonUtil.getIp();
        //logger.info(ip + "/" + ConfigUtils.getConf("monitoringIp"));
        if(ip == null || ConfigUtils.getConf("monitoringIp") == null || ip.equals(ConfigUtils.getConf("monitoringIp")) == false) {
            return;
        }

        //logger.info("[LEADS PULLING] Start getting leads information");

        // Configure your storage connection string
        String storageConnectionString =
                "DefaultEndpointsProtocol=https;" +
                        "AccountName="+accountName+";" +
                        "AccountKey=" + accountKey;

        try {
            // Retrieve storage account from connection-string.
            CloudStorageAccount storageAccount =
                    CloudStorageAccount.parse(storageConnectionString);

            // Create the table client.
            CloudTableClient tableClient = storageAccount.createCloudTableClient();

            CloudTable cloudTable = tableClient.getTableReference(tableName);

            TableQuery<CustomerEntity> partitionQuery = TableQuery.from(CustomerEntity.class);

            // Loop through the results, displaying information about the entity.
            for (CustomerEntity entity : cloudTable.execute(partitionQuery)) {
                logger.info("[LEADS PULLING] Check leads inflow - {}", entity.toString());
                Gson gson = new Gson();

                // db insert vo
                LeadsInfoVo vo = gson.fromJson(entity.getCustomerInfo(), LeadsInfoVo.class);
                vo.setPartitionKey(entity.getPartitionKey());
                vo.setRowKey(entity.getRowKey());
                vo.setTimestamp(entity.getTimeStamp().toString());
                vo.setProductId(entity.getProductId());
                vo.setLeadSource(entity.getLeadSource());
                vo.setActionCode(entity.getActionCode());
                vo.setPublisherDisplayName(entity.getPublisherDisplayName());
                vo.setOfferDisplayName(entity.getOfferDisplayName());
                vo.setCreatedTime(entity.getCreatedTime());
                vo.setDescription(entity.getDescription());

                try {
                    // db에 leads 정보가 정상적으로 저장되면, 계정 생성 요청 및 해당 entity table에서 삭제
                    if (mapper.insertLeadsInfo(vo)) {
                        logger.info("[LEADS PULLING] Database insert success - {}", vo.getEmail());
                        // 계정 생성 요청
                        HttpRequest request = new HttpRequest();
                        if (request.doGetHttps(vo.getEmail(), vo.getCompany())) {    // 계정 생성 성공 시
                            logger.info("[LEADS PULLING] Account creation successful - {}", vo.getEmail());
                            // entity tablel에서 해당 값 삭제
                            TableOperation deleteEntity = TableOperation.delete(entity);

                            TableResult result = cloudTable.execute(deleteEntity);

                            logger.info("[LEADS PULLING] Successful deletion of leads information - {}", vo.getEmail());
                        }
                    }
                } catch(Exception e) {
                    logger.error(CommonUtil.getPrintStackTrace(e));
                }
            }

        } catch (Exception e) {
            logger.error(CommonUtil.getPrintStackTrace(e));
        } finally {
            //logger.info("[LEADS PULLING] End of getting leads information");
        }
    }

    /**
     * resource data
     */
    @Scheduled(cron = "0 0/1 * * * *")  // 1분마다
    public void getResourceInfo() {
        //logger.info("[DISK PULLING] Start getting disk information");
        // 전체 유저 리소스그룹 조회
        List<MemberVo> list = memberMapper.selectMemberList();

        HttpRequest request = new HttpRequest();

        // db insert list
        List<ResourceVo> insertList = new ArrayList<ResourceVo>();

        for(int i = 0; i < list.size(); i++) {
            MemberVo vo = list.get(i);

            if(vo.getIpAddr() != null && vo.getIpAddr().equals("") == false) {
                //logger.info("[DISK PULLING] resource group : {} ip : {}", vo.getRscGrp(), vo.getIpAddr());
                try {
                    // get data
                    Map<String, Object> data = request.multiVolume(
                            null ,vo.getIpAddr().trim(), vo.getRscGrp(), null, "get");
                    if(data.get("data") != null) {
                        //logger.info("[DISK PULLING] [{}] data pulling success", vo.getRscGrp());
                        // parsing
                        List<ResourceVo> retList = resourceService.resourceFileParse(vo.getRscGrp(), (String)data.get("data"));

                        if(retList.size() != 0) {
                            insertList.addAll(retList);
                        }
                    }
                    //System.out.println(request.doPostHttp(vo.getIpAddr().trim()));
                } catch (Exception e) {
                    logger.error("{} error", vo.getRscGrp());
                    logger.error(e.getMessage());
                    //logger.error(CommonUtil.getPrintStackTrace(e));
                }
            }
        }

        // db update
        mapper.insertResourceInfo(insertList);

        //logger.info("[DISK PULLING] db update success");
        logger.info("[DISK PULLING] End getting disk information");
    }

    /**
     * config set
     */
    @PostConstruct
    @Scheduled(cron = "0 0/5 * * * *")  // 5분마다
    public void getConfig() {
        // config 조회
        List<ConfigVo> list = homeMapper.selectConfig();

        // config setting
        ConfigUtils.setConfig(list);

        logger.info("[SET CONFIG] config settings [{}]", list.size());
    }




    /**
     * disk_autoscaling
     */
    //@Scheduled(cron = "0 0/5 * * * *")  // 1시간마다
    public void diskAutoscaling() {
        // user config list
        List<ResourceVo> userList = mapper.getUserConfigList();

        for(ResourceVo configVo : userList) {
            // user config check

            // autoscaling check
            if(configVo.isDiskAutoscaling() == false) {
                logger.info("[DISK AUTOSCALING] [{}] work end.. {}", configVo.getRscparam(), "Disable autoscaling");
                continue;
            }

            // current disk usage
            ResourceVo currentVo = resourceMapper.selectCurrentDiskUsage(configVo.getRscparam());
            if(currentVo == null || currentVo.getRscparam() == null || currentVo.getUsageDiskPer() == null) {
                logger.info("[DISK AUTOSCALING] [{}] work end.. {}", configVo.getRscparam(), "No disks being saved");
                continue;
            }

            if(configVo.getDiskMaximum() < Double.parseDouble(currentVo.getUsageDiskPer())) {
                logger.info("[DISK AUTOSCALING] [{}] Start disk autoscaling. current disk : {}, maximum disk : {}"
                        , configVo.getRscparam(), Double.parseDouble(currentVo.getUsageDiskPer()), configVo.getDiskMaximum());
                //resourceService.addMultiVolume(configVo.getRscparam(), configVo.getDiskSize());
            } else {
                logger.info("[DISK AUTOSCALING] [{}] Do not start disk autoscaling. current disk : {}, maximum disk : {}"
                        , configVo.getRscparam(), Double.parseDouble(currentVo.getUsageDiskPer()), configVo.getDiskMaximum());
            }
        }
    }

}
