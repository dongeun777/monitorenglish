package igloosec.monitor.mapper;

import igloosec.monitor.vo.MemberVo;
import igloosec.monitor.vo.UsageVo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface HomeMapper {
    List<UsageVo> selectUsage(UsageVo param);

    List<UsageVo> selectUsagebefore(UsageVo param);

    List<UsageVo> selectMeterDetail(UsageVo param);

    List<UsageVo> selectMeterSum(UsageVo param);

    void registerLog(UsageVo param);

    void joinMember(MemberVo memberVo);
    void resetPass(MemberVo memberVo);

    List<MemberVo> checkMember(MemberVo memberVo);
    List<MemberVo> checkResetMember(MemberVo memberVo);

    List<UsageVo> selectLogList(UsageVo param);

    void completeLog(MemberVo memberVo);
    void completeLog2(MemberVo memberVo);
    void goBack(String email);
    String selectSecretKey(MemberVo memberVo);
    String selectQrCord(MemberVo memberVo);
    UsageVo selectCostTotal(UsageVo param);
    MemberVo selectMember(String email);
    String selectPath();
    void deleteLog(String logid);

    List<MemberVo> getGrpList();
}
