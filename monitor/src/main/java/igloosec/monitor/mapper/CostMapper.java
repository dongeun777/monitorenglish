package igloosec.monitor.mapper;

import igloosec.monitor.vo.CostVo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface CostMapper {
    List<CostVo> selectCostList();

}
