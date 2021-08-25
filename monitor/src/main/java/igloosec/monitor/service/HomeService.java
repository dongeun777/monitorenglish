package igloosec.monitor.service;

import igloosec.monitor.mapper.HomeMapper;
import igloosec.monitor.vo.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HomeService {
    public final HomeMapper mapper;

    public HomeService(HomeMapper mapper) {
        this.mapper = mapper;
    }


    public List<UsageVo> selectUsage(UsageVo param) {
        return mapper.selectUsage(param);
    }

    public List<UsageVo> selectUsagebefore(UsageVo param) {
        return mapper.selectUsagebefore(param);
    }

    public List<UsageVo> selectMeterDetail(UsageVo param) {
        return mapper.selectMeterDetail(param);
    }

    public List<UsageVo> selectMeterSum(UsageVo param) {
        return mapper.selectMeterSum(param);
    }


    public void registerLog(UsageVo param) {
        mapper.registerLog(param);
    }

    public void deleteLog(String logid) {
        mapper.deleteLog(logid);
    }

    public void joinUser(MemberVo memberVo) {
        // 비밀번호 암호화
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        memberVo.setPasswd(passwordEncoder.encode(memberVo.getPasswd()));
        mapper.joinMember(memberVo);


    }

    public void resetPass(MemberVo memberVo) {
        // 비밀번호 암호화
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        memberVo.setPasswd(passwordEncoder.encode(memberVo.getPasswd()));
        mapper.resetPass(memberVo);

    }

    public List<MemberVo> checkMember(MemberVo memberVo) {
        // 비밀번호 암호화
        return mapper.checkMember(memberVo);


    }

    public List<MemberVo> checkResetMember(MemberVo memberVo) {
        // 비밀번호 암호화
        return mapper.checkResetMember(memberVo);


    }

    public MemberVo selectMember(String email) {
        return mapper.selectMember(email);
    }
    public String selectSecretKey(MemberVo memberVo) {
        return mapper.selectSecretKey(memberVo);
    }

    public String selectQrCord(MemberVo memberVo) {
        return mapper.selectQrCord(memberVo);
    }


    public List<UsageVo> selectLogList(UsageVo param) {
        return mapper.selectLogList(param);
    }

    public void completeLog(MemberVo memberVo) {
        mapper.completeLog(memberVo);
    }
    public void goBack(String email) {
        mapper.goBack(email);
        mapper.deleteChoice2(email);
    }


    public void goChoice(String email) {
        mapper.goChoice(email);
    }

    public UsageVo selectCostTotal(UsageVo param) {
        return mapper.selectCostTotal(param);
    }

    public void completeLog2(MemberVo param) {
        mapper.completeLog2(param);
    }

    public String selectPath() {
        return mapper.selectPath();
    }

    public List<MemberVo> getGrpList() {
        return  mapper.getGrpList();
    }

    public List<MemberVo> getCustGrpList(MemberVo memberVo) {
        return  mapper.getCustGrpList(memberVo);
    }




    // shell parameter
    public UsageVo selectShellParam(String email) {return mapper.selectShellParam(email); };

    public String getPeriod(MemberVo memberVo) {return mapper.getPeriod(memberVo);
    }

    public List<MemberVo> rscCheck(MemberVo param) {return mapper.rscCheck(param);
    }

    public List<LeadsInfoVo> selectLeadsList(String emailStr) {return mapper.selectLeadsList(emailStr);
    }

    public void leadsToProduct(LeadsInfoVo vo) { mapper.leadsToProduct(vo);
    }

    public void goNext(LeadsInfoVo vo) { mapper.goNext(vo);
    }

    public void deleteChoice1(String email) { mapper.deleteChoice1(email);
    }

    public void deleteChoice2(String email) { mapper.deleteChoice2(email);
    }

    public void insertToProduct(LeadsInfoVo parameter) { mapper.leadsToProduct(parameter);
    }

    public void completeApplyLog(MemberVo param) {
        mapper.completeApplyLog(param);
    }

    public void completeApplyLog2(MemberVo param) {
        mapper.completeApplyLog2(param);
    }

    public List<MemberVo> getPaymentGroup() { return mapper.getPaymentGroup();
    }

    public List<BillingVo> getBillingList(BillingVo param) { return mapper.getBillingList(param);
    }

    public void insertBilling(BillingVo temp) { mapper.insertBilling(temp);
    }
}
