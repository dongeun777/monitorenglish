package igloosec.monitor.mapper;

import igloosec.monitor.vo.ResourceVo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface ResourceMapper {

    List<ResourceVo> selectResourceList();


}
