package igloosec.monitor.mapper;

import igloosec.monitor.vo.UsageVo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface HomeMapper {
    List<UsageVo> selectUsage(UsageVo param);

    List<UsageVo> selectUsagebefore(UsageVo param);

    List<UsageVo> selectMeterDetail();

    List<UsageVo> selectMeterSum();
}
