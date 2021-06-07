package igloosec.monitor.service;

import igloosec.monitor.mapper.HomeMapper;
import igloosec.monitor.vo.MemberVo;
import igloosec.monitor.vo.UsageVo;
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

    public void joinUser(MemberVo memberVo) {
        // 비밀번호 암호화
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        memberVo.setPasswd(passwordEncoder.encode(memberVo.getPasswd()));
        mapper.joinMember(memberVo);


    }

    public List<MemberVo> checkMember(MemberVo memberVo) {
        // 비밀번호 암호화
        return mapper.checkMember(memberVo);


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
}
