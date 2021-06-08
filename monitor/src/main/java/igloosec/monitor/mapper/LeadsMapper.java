package igloosec.monitor.mapper;

import igloosec.monitor.vo.CostVo;
import igloosec.monitor.vo.LeadsInfoVo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface LeadsMapper {
    boolean insertLeadsInfo(LeadsInfoVo leadsInfoVo);

}
