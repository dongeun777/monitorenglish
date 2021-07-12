package igloosec.monitor.mapper;

import igloosec.monitor.vo.CostVo;
import igloosec.monitor.vo.LeadsInfoVo;
import igloosec.monitor.vo.ResourceVo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface ScheduleMapper {
    boolean insertLeadsInfo(LeadsInfoVo leadsInfoVo);
    boolean insertResourceInfo(List<ResourceVo> list);


    List<ResourceVo> getUserConfigList();
}
