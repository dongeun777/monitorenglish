package igloosec.monitor.service;

import igloosec.monitor.mapper.HomeMapper;
import igloosec.monitor.vo.UsageVo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HomeService {
    public final HomeMapper mapper;

    public HomeService(HomeMapper mapper) {
        this.mapper = mapper;
    }


    public List<UsageVo> selectUsage(UsageVo param) {
        return mapper.selectUsage(param);
    }

    public List<UsageVo> selectUsagebefore(UsageVo param) {
        return mapper.selectUsagebefore(param);
    }

    public List<UsageVo> selectMeterDetail() {
        return mapper.selectMeterDetail();
    }

    public List<UsageVo> selectMeterSum() {
        return mapper.selectMeterSum();
    }


}
