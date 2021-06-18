package igloosec.monitor;

import igloosec.monitor.mapper.HomeMapper;
import igloosec.monitor.vo.ConfigVo;
import org.apache.ibatis.io.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ConfigUtils {
    private static final Logger logger = LoggerFactory.getLogger(ConfigUtils.class);
    private static ConfigUtils instance;
    private static Map<String, String> configMap;

    public static ConfigUtils setConfig(List<ConfigVo> list) {

        Map<String, String> map = new HashMap<String, String>();
        for(ConfigVo vo : list) {
            map.put(vo.getCfgKey(), vo.getCfgVal());
        }

        if(map.size() != 0) {
            configMap = map;
        }

        return instance;
    }

    public static String getConf(String key) {
        return configMap.get(key);
    }

}
