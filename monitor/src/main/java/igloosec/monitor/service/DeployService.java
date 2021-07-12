package igloosec.monitor.service;

import igloosec.monitor.mapper.DeployMapper;
import igloosec.monitor.mapper.HomeMapper;
import igloosec.monitor.vo.MemberVo;
import igloosec.monitor.vo.UsageVo;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeployService {
    public final DeployMapper mapper;

    public DeployService(DeployMapper mapper) {
        this.mapper = mapper;
    }



    public void registerLog(UsageVo param) {
        mapper.registerLog(param);
    }

    public void deleteLog(String logid) {
        mapper.deleteLog(logid);
    }


    public MemberVo selectMember(String email) {
        return mapper.selectMember(email);
    }



    public void completeLog(MemberVo memberVo) {
        mapper.completeLog(memberVo);
    }
    public void goBack(String email) {
        mapper.goBack(email);
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

    public List<UsageVo> selectLogList(UsageVo param) {
        return mapper.selectLogList(param);
    }
    // shell parameter
    public UsageVo selectShellParam(String email) {return mapper.selectShellParam(email); };

    public String getPeriod(MemberVo memberVo) {return mapper.getPeriod(memberVo);
    }

    public void productBack(String apply_id) {
        mapper.productBack(apply_id);
        mapper.deleteEquip(apply_id);
    }
}
