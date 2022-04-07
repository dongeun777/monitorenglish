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
import igloosec.monitor.MailSending;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class ScheduleService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleService.class);

    public final ScheduleMapper mapper;
    public final MemberMapper memberMapper;
    public final HomeMapper homeMapper;
    public final ResourceMapper resourceMapper;
    public final ResourceService resourceService;
    public final HomeService homeService;

    public ScheduleService(ScheduleMapper mapper, MemberMapper memberMapper, HomeMapper homeMapper, ResourceMapper resourceMapper, ResourceService resourceService, HomeService homeService) {
        this.mapper = mapper;
        this.memberMapper = memberMapper;
        this.homeMapper = homeMapper;
        this.resourceMapper = resourceMapper;
        this.resourceService = resourceService;
        this.homeService = homeService;
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
        /*
        String ip = CommonUtil.getIp();
        //logger.info(ip + "/" + ConfigUtils.getConf("monitoringIp"));
        if(ip == null || ConfigUtils.getConf("monitoringIp") == null || ip.equals(ConfigUtils.getConf("monitoringIp")) == false) {
            return;
        }
        */
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
                        //mapper.insertProductInfo(vo);
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
    @Scheduled(cron = "0 * * * * *")  // 1분마다
    public void getResourceInfo() {
        //logger.info("[DISK PULLING] Start getting disk information");
        // 전체 유저 리소스그룹 조회
        //List<MemberVo> list = memberMapper.selectMemberList();
        List<MemberVo> list = memberMapper.selectProductList();

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
        if(insertList.size() != 0) { //
            mapper.insertResourceInfo(insertList);
        }

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


    // 국내 결제 (KCP)
    //@Scheduled(cron = "0 0/5 * * * *")
    //@Scheduled(cron = "0 5 1 * * *") // TCLOUD-54
    @Scheduled(cron = "0 0 5 1  * *") // TCLOUD-54
//    @Scheduled(cron = "0 20 13 *  * *") // Test
    public void payment() {

        //token 생성
        RestTemplate restTemplate = new RestTemplate();

        //서버로 요청할 Header
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> map = new HashMap<>();
        map.put("imp_key", "1117314894269411");
        map.put("imp_secret", "4MVoXXO470Ns6eh1JwDE0MPLmAVGQ10VOVkMT9Q19DtgRLiAhVfhI434FYLw0LsPHMBAWrB645mWQFx7");

        //map.put("imp_key", "8526978586250291");
        //map.put("imp_secret", "EONJZvKP4Xs3KjACZSa0847VJ86Oyjuc7hV6MGWRDoUoHv1HnbHwfOsYXyNwRiIYzE5ml2dYq8n8DoP0");

        Gson var = new Gson();
        String json=var.toJson(map);
        //서버로 요청할 Body

        HttpEntity<String> entity = new HttpEntity<>(json,headers);
        String token = restTemplate.postForObject("https://api.iamport.kr/users/getToken", entity, String.class);

        //String token = pay.getToken();
        Gson str = new Gson();
        token = token.substring(token.indexOf("response") + 10);
        token = token.substring(0, token.length() - 1);

        GetTokenVO vo = str.fromJson(token, GetTokenVO.class);

        String access_token = vo.getAccess_token();
        System.out.println(access_token);

        //결제 정보 가져오기
        List<MemberVo> list = homeService.getPaymentGroup();
        RestTemplate restTemplate2 =null;

//        /*
        if (list != null) {

            for (int i = 0 ; i < list.size(); i++) {

                Date date_now = new Date(System.currentTimeMillis());
                SimpleDateFormat fourteen_format = new SimpleDateFormat("yyyyMMddHHmmss");
                String paydate = fourteen_format.format(date_now).toString();

                // 카드결제 시에만 아임포트에 결제요청
                if(list.get(i).getPg() != "cash") {
                    restTemplate2 = new RestTemplate();

                    HttpHeaders headers2 = new HttpHeaders();
                    headers2.setContentType(MediaType.APPLICATION_JSON);
                    headers2.setBearerAuth(access_token);
                    //headers2.setBearerAuth("dd8c18caf6cb16e058f90683e62e8258f2684b23");

                    Map<String, Object> map2 = new HashMap<>();
                    map2.put("customer_uid", list.get(i).getEmail());
                    map2.put("merchant_uid", list.get(i).getEmail()+fourteen_format.format(date_now).toString());
                    //map2.put("merchant_uid", "why")
                    map2.put("amount", 100);
//                map2.put("amount", Integer.parseInt(list.get(i).getCostInBillingCurrency()));
                    map2.put("name", "Monthly Billing");
                    map2.put("buyer_email", list.get(i).getEmail());

                    Gson var2 = new Gson();
                    String json2 = var2.toJson(map2);
                    System.out.println(json2);
                    HttpEntity<String> entity2 = new HttpEntity<>(json2, headers2);
                    //return restTemplate2.postForObject("https://api.iamport.kr/subscribe/payments/onetime", entity2, String.class);
                    restTemplate2.postForObject("https://api.iamport.kr/subscribe/payments/again", entity2, String.class);
                }

                BillingVo temp = new BillingVo();
                temp.setEmail(list.get(i).getEmail());
                temp.setResource(list.get(i).getBillingResource());
                temp.setLicense(list.get(i).getBillingLicense());
                temp.setBillingsum(Integer.toString((Integer.parseInt(list.get(i).getCostInBillingCurrency()) + Integer.parseInt("0"))));
                temp.setPaydate(paydate.substring(0, 4) + "-" + paydate.substring(4, 6));
                temp.setBillingtype("정기결제");
                temp.setCurrency("KRW");

                homeService.insertBilling(temp);
            }
        }
//         */
        logger.info("[Monthly Billing] End");
    }


    // 해외 결제 (paymentwall)
    @Scheduled(cron = "0 0 5 1  * *")
//    @Scheduled(cron = "0 30 13 *  * *")   // Test
    public void paymentOverseas() {

        //token 생성
        RestTemplate restTemplate = new RestTemplate();

        //서버로 요청할 Header
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> map = new HashMap<>();
        map.put("imp_key", "1117314894269411");
        map.put("imp_secret", "4MVoXXO470Ns6eh1JwDE0MPLmAVGQ10VOVkMT9Q19DtgRLiAhVfhI434FYLw0LsPHMBAWrB645mWQFx7");

        Gson var = new Gson();
        String json=var.toJson(map);
        //서버로 요청할 Body

        HttpEntity<String> entity = new HttpEntity<>(json,headers);
        String token = restTemplate.postForObject("https://api.iamport.kr/users/getToken", entity, String.class);

        Gson str = new Gson();
        token = token.substring(token.indexOf("response") + 10);
        token = token.substring(0, token.length() - 1);

        GetTokenVO vo = str.fromJson(token, GetTokenVO.class);

        String access_token = vo.getAccess_token();
        System.out.println(access_token);

        //결제 정보 가져오기
        List<MemberVo> list = homeService.getPaymentGroupOverseas();
        RestTemplate restTemplate2 =null;

        if (list != null) {

            for (int i = 0 ; i < list.size(); i++) {

                Date date_now = new Date(System.currentTimeMillis());
                SimpleDateFormat fourteen_format = new SimpleDateFormat("yyyyMMddHHmmss");
                String paydate = fourteen_format.format(date_now).toString();

                // 카드결제 시에만 아임포트에 결제요청
                if(list.get(i).getPg() != "cash") {
                    restTemplate2 = new RestTemplate();

                    HttpHeaders headers2 = new HttpHeaders();
                    headers2.setContentType(MediaType.APPLICATION_JSON);
                    headers2.setBearerAuth(access_token);

                    Map<String, Object> map2 = new HashMap<>();
                    map2.put("customer_uid", list.get(i).getEmail());
                    map2.put("merchant_uid", list.get(i).getEmail()+fourteen_format.format(date_now).toString());
                    map2.put("currency", "USD");
                    map2.put("amount", 1);
//                map2.put("amount", Integer.parseInt(list.get(i).getCostInBillingCurrency()));
                    map2.put("name", "Monthly Billing");
                    map2.put("buyer_email", list.get(i).getEmail());

                    Gson var2 = new Gson();
                    String json2 = var2.toJson(map2);
                    System.out.println(json2);
                    HttpEntity<String> entity2 = new HttpEntity<>(json2, headers2);
                    restTemplate2.postForObject("https://api.iamport.kr/subscribe/payments/again", entity2, String.class);
                }

                BillingVo temp = new BillingVo();
                temp.setEmail(list.get(i).getEmail());
                temp.setResource(list.get(i).getCostInBillingCurrency());
                temp.setResource(list.get(i).getBillingResource());
                temp.setLicense(list.get(i).getBillingLicense());
                temp.setBillingsum(Integer.toString((Integer.parseInt(list.get(i).getCostInBillingCurrency()) + Integer.parseInt("0"))));
                temp.setPaydate(paydate.substring(0, 4) + "-" + paydate.substring(4, 6));
                temp.setBillingtype("정기결제");
                temp.setCurrency("USD");

                homeService.insertBilling(temp);
            }
        }

        logger.info("[Monthly Billing] End");
    }


    /**
     * disk_autoscaling
     */
    @Scheduled(cron = "0 0 * * * *")  // 1시간마다
    public void diskAutoscaling() {
        logger.info("[DISK AUTOSCALING] Start disk autoscaling operation");
        // user config list
        List<MemberVo> productList = mapper.selectAutoscalingProductInfo();

        for(MemberVo memberVo : productList) {
            // current disk usage
            ResourceVo param = new ResourceVo();
            param.setRscparam(memberVo.getRscGrp());
            ResourceVo currentVo = resourceMapper.selectResourceUsage(param);
            if(currentVo == null || currentVo.getRscparam() == null || currentVo.getUsageDiskPer() == null) {
                logger.info("[DISK AUTOSCALING] [{}] work end.. {}", memberVo.getRscGrp(), "No disks being saved");
                continue;
            }

            double criticalValue = Double.valueOf(memberVo.getCriticalValue());
            double currentValue = Double.parseDouble(currentVo.getUsageDiskPer().replace("%",""));

            if(criticalValue < currentValue) {
                // Sending messages that exceed the critical value
                MailSending mail = new MailSending();
                try {
                    mail.sendMailCriticalValueExceeded(memberVo.getEmail(), currentValue, criticalValue);
                } catch(Exception e) {
                    logger.error(CommonUtil.getPrintStackTrace(e));
                }
                // autoscaling is disabled
                if(memberVo.getAutoscalingYN().equals("Y")) {
                    logger.info("[DISK AUTOSCALING] [{}] Start disk autoscaling. current disk : {}, maximum disk : {}"
                            , memberVo.getRscGrp(), currentValue, criticalValue);
                    //resourceService.addMultiVolume(configVo.getRscparam(), configVo.getDiskSize());
                    ResourceVo vo = resourceService.requestExpansionShell(memberVo.getRscGrp(), Integer.parseInt(memberVo.getDisksize()));
                    if (vo.isDiskWorkResult() == false) {
                        logger.error("[DISK AUTOSCALING] [{}] requestExpansionShell error");
                        continue;
                    }
                    if(resourceService.waitDiskExpansionComplete(vo.getRscparam(), vo.getIdx()) == false) {
                        logger.error("[DISK AUTOSCALING] [{}] waitDiskExpansionComplete error");
                    }
                }
            } else {
                logger.info("[DISK AUTOSCALING] [{}] Do not start disk autoscaling. current disk : {}, maximum disk : {}"
                        , memberVo.getRscGrp(), currentValue, criticalValue);
            }
        }

        logger.info("[DISK AUTOSCALING] End disk autoscaling operation");
    }
}
