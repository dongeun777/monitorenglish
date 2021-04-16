package igloosec.monitor.mapper;

import igloosec.monitor.vo.UsageVo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface HomeMapper {
    List<UsageVo> selectUsage();

    List<UsageVo> selectUsagebefore();
}
