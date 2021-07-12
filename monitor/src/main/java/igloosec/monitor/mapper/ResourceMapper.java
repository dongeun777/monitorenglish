package igloosec.monitor.mapper;

import igloosec.monitor.vo.ResourceVo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface ResourceMapper {

    List<ResourceVo> selectResourceList(ResourceVo param);

    List<ResourceVo> selectDiskUsageList(ResourceVo param);

    ResourceVo selectResourceUsage(ResourceVo param);

    List<ResourceVo> selectDiskPrice();

    String selectUserVmIp(String rscGrp);

    boolean insertDiskName(ResourceVo param);

    boolean deleteDiskName(ResourceVo param);

    int checkDiskWork(String rscGrp);

    ResourceVo selectCurrentDiskUsage(String rscparam);

    boolean initDiskWorkHistory(ResourceVo vo);

    boolean updateDiskWorkHistory(ResourceVo retVo);

    int selectMaxIdxDiskWorkHistory(ResourceVo vo);
}
