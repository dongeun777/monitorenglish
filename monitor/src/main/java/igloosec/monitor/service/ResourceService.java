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




}
