package igloosec.monitor.service;

import igloosec.monitor.mapper.ResourceMapper;
import igloosec.monitor.vo.ResourceVo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResourceService {
    public final ResourceMapper mapper;

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
}
