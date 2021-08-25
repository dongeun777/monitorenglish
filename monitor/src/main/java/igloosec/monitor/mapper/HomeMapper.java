package igloosec.monitor.mapper;

import igloosec.monitor.vo.*;
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
    void completeApplyLog(MemberVo memberVo);
    void completeApplyLog2(MemberVo memberVo);

    void goBack(String email);
    String selectSecretKey(MemberVo memberVo);
    String selectQrCord(MemberVo memberVo);
    UsageVo selectCostTotal(UsageVo param);
    MemberVo selectMember(String email);
    String selectPath();
    void deleteLog(String logid);

    List<MemberVo> getGrpList();

    // shell param
    UsageVo selectShellParam(String email);

    List<ConfigVo> selectConfig();

    String getPeriod(MemberVo memberVo);

    List<MemberVo> getCustGrpList(MemberVo memberVo);

    List<MemberVo> rscCheck(MemberVo param);

    List<LeadsInfoVo> selectLeadsList(String emailStr);

    void leadsToProduct(LeadsInfoVo vo);

    void goNext(LeadsInfoVo vo);

    void goChoice(String email);

    void deleteChoice1(String email);

    void deleteChoice2(String email);

    List<MemberVo> getPaymentGroup();

    List<BillingVo> getBillingList(BillingVo param);

    void insertBilling(BillingVo temp);
}
