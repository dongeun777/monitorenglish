package igloosec.monitor;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.google.gson.Gson;

//@Service
public class ImportPay {
    public String getToken() {

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
        return restTemplate.postForObject("https://api.iamport.kr/users/getToken", entity, String.class);


    }
}
