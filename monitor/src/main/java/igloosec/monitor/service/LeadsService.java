package igloosec.monitor.service;

import com.google.gson.Gson;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.table.*;
import igloosec.monitor.HttpRequest;
import igloosec.monitor.mapper.LeadsMapper;
import igloosec.monitor.vo.CustomerEntity;
import igloosec.monitor.vo.LeadsInfoVo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LeadsService {
    public final LeadsMapper mapper;

    public LeadsService(LeadsMapper mapper) {
        this.mapper = mapper;
    }

    @Value("${azure.marketplace.account.name}")
    private String accountName;

    @Value("${azure.marketplace.account.key}")
    private String accountKey;

    @Value("${azure.marketplace.table.name}")
    private String tableName;

    //@Scheduled(fixedDelay = 3600000)  // 1시간 간격
    //@Scheduled(cron = "0/10 * * * * *")  // 10초마다
    @Scheduled(cron = "0 0/1 * * * *")  // 1분마다
    public void getLeadsInfo() {
        System.out.println("Start getting leads information");

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
                System.out.println("Check leads inflow - " + entity.toString());
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

                // db에 leads 정보가 정상적으로 저장되면, 계정 생성 요청 및 해당 entity table에서 삭제
                if (mapper.insertLeadsInfo(vo)) {
                    System.out.println("Database insert success - " + vo.getEmail());
                    // 계정 생성 요청
                    HttpRequest request = new HttpRequest();
                    if (request.doGetHttps(vo.getEmail(), vo.getCompany())) {    // 계정 생성 성공 시
                        System.out.println("Account creation successful - " + vo.getEmail());
                        // entity tablel에서 해당 값 삭제
                        TableOperation deleteEntity = TableOperation.delete(entity);

                        TableResult result = cloudTable.execute(deleteEntity);

                        System.out.println("Successful deletion of leads information - " + vo.getEmail());
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("End of getting leads information");
        }

        // local test
        /*
        for(int i = 0; i < 1000; i++) {
            LeadsInfoVo vo = new LeadsInfoVo();
            vo.setPartitionKey("1");
            vo.setRowKey(Integer.toString(i));
            vo.setTimestamp("3");
            vo.setProductId("4");
            vo.setLeadSource("5");
            vo.setActionCode("6");
            vo.setPublisherDisplayName("7");
            vo.setOfferDisplayName("8");
            vo.setCreatedTime("9");
            vo.setDescription("10");
            vo.setFirstName("11");
            vo.setLastName("12");
            vo.setEmail("yougwoon.lee@igloosec.com");
            vo.setPhone("14");
            vo.setCountry("15");
            vo.setCompany("16");
            vo.setTitle("17");

            // db에 leads 정보가 정상적으로 저장되면, 계정 생성 요청 및 해당 entity table에서 삭제
            if (mapper.insertLeadsInfo(vo)) {
                System.out.println("Database insert success - " + vo.getEmail());
                // 계정 생성 요청
                HttpRequest request = new HttpRequest();
                if (request.doGetHttp(vo.getEmail(), vo.getCompany())) {    // 계정 생성 성공 시
                    System.out.println("Account creation successful - " + vo.getEmail());
                    // entity tablel에서 해당 값 삭제
                    System.out.println("Successful deletion of leads information - " + vo.getEmail());
                }
            }

            try {
                Thread.sleep(60000);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        */
    }
}
