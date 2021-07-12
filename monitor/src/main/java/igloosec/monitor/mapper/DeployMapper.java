package igloosec.monitor.mapper;

import igloosec.monitor.vo.ConfigVo;
import igloosec.monitor.vo.MemberVo;
import igloosec.monitor.vo.UsageVo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface DeployMapper {


    void registerLog(UsageVo param);

    List<UsageVo> selectLogList(UsageVo param);

    void completeLog(MemberVo memberVo);
    void completeLog2(MemberVo memberVo);
    void goBack(String email);

    UsageVo selectCostTotal(UsageVo param);
    MemberVo selectMember(String email);
    String selectPath();
    void deleteLog(String logid);

    List<MemberVo> getGrpList();

    // shell param
    UsageVo selectShellParam(String email);

    List<ConfigVo> selectConfig();

    String getPeriod(MemberVo memberVo);

    void productBack(String apply_id);

    void deleteEquip(String apply_id);
}
