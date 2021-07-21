package igloosec.monitor.service;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import igloosec.monitor.CommonUtil;
import igloosec.monitor.mapper.MemberMapper;
import igloosec.monitor.vo.MemberVo;
import igloosec.monitor.vo.UsageVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;


@Service
public class MemberService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(MemberService.class);

    public final MemberMapper mapper;

    public MemberService(MemberMapper mapper) {
        this.mapper = mapper;
    }

    public void joinUser(MemberVo memberVo) {
        // 비밀번호 암호화
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        memberVo.setPasswd(passwordEncoder.encode(memberVo.getPasswd()));
        mapper.joinMember(memberVo);

        /*if (memberVo.getAuth().equals("ROLE_USER")){
            mapper.createTmTable(memberVo);
        }*/

       // return memberRepository.save(memberDto.toEntity()).getId();


        //1단계 사용자테이블 rscgrp - tm5SArsg
        //2단계 for is_stats_+       select rscgrp, event_date, event_volume
    }

    public void deleteUser(MemberVo memberVo) {
        // delete USER_DISK_USAGE
        mapper.deleteUsedAmount(memberVo);
        mapper.deleteTmStats(memberVo);
        mapper.deleteUserDiskUsage(memberVo);
        mapper.deleteMember(memberVo);
        mapper.deleteEquipList(memberVo);
        //mapper.deleteFedTable(memberVo);
    }

    public UsageVo setDeletePath(){
        return mapper.selectDeletePath();
    }



    public void modifyGrpIp(MemberVo memberVo) {

        mapper.modifyGrpIpMember(memberVo);

    }





    @Override
    public UserDetails  loadUserByUsername(String username) throws UsernameNotFoundException {

        MemberVo user =  mapper.selectMember(username);

        if(user==null) {

            // throw new UsernameNotFoundException(username);
        }
        return user;



    }

    public List<MemberVo> selectMemberList() {
        return mapper.selectMemberList();
    }

    public String selectPath() {
        return mapper.selectPath();
    }

    public String modifyUserPwd(MemberVo memberVo) {

        String msg = null;
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        try {
            // Check current password match
            MemberVo user =  mapper.selectMember(memberVo.getEmail());

            if (!passwordEncoder.matches(memberVo.getCurrentPwd(), user.getPassword())) {
                msg = "현재 비밀번호가 일치하지 않습니다.";
            } else {
                // new password update
                // password encryption
                memberVo.setPasswd(passwordEncoder.encode(memberVo.getNewPwd()));
                mapper.updatePw(memberVo);
            }
        } catch(Exception e) {
            logger.error(CommonUtil.getPrintStackTrace(e));
        }

        return msg;
    }

    public MemberVo getUserInfo(String email) {
        return mapper.selectMember(email);
    }

    public List<MemberVo> getUserProductInfoList(String email) {
        List<MemberVo> list = null;

        try {
            list = mapper.selectUserProductInfoList(email);
        } catch(Exception e) {
            logger.error(CommonUtil.getPrintStackTrace(e));
        }
        return list;
    }

    public boolean setUserInfoSave(String param) {
        logger.info("user info update start");
        boolean retVal = true;
        JsonObject obj = new JsonParser().parse(param).getAsJsonObject();
        MemberVo memberVo = new MemberVo();
        memberVo.setEmail(obj.get("email").getAsString());
        //memberVo.setCompany(obj.get("company").getAsString());

        // update USER_LIST
/*
        if(mapper.updateUserInfo(memberVo) == false) {
            logger.error("user info update failed : {}", memberVo.getEmail());
            return false;
        }
*/

        // update disk_autoscaling
        JsonArray jsonArr = (JsonArray)obj.get("list");
        for(int i = 0; i < jsonArr.size(); i++) {
            MemberVo vo = new MemberVo();
            JsonObject memberJson = jsonArr.get(i).getAsJsonObject();
            vo.setApplyId(memberJson.get("applyId").getAsString());
            vo.setCloud_vendor(memberJson.get("cloud_vendor").getAsString());
            vo.setProduct(memberJson.get("product").getAsString());
            vo.setCountry(memberJson.get("country").getAsString());
            vo.setAutoscalingYN(memberJson.get("autoscalingYN").getAsBoolean() == true ? "Y" : "N");
            vo.setCriticalValue(Integer.parseInt(memberJson.get("criticalValue").getAsString()));
            boolean autoscalingChk = memberJson.get("autoscalingYN").getAsBoolean();
            String diskSize = null;
            if(autoscalingChk == true) {
                diskSize = memberJson.get("disksize").getAsString();
            }

            vo.setDisksize(diskSize);

            if(mapper.updateUserDiskAutoscaling(vo) == false) {
                logger.error("user diskautoscaling update failed : {}, {}", memberVo.getEmail());
                logger.error("[applyId][vendor][product][country][autoscalingYN][criticalValue][disksize]");
                logger.error("[{}][{}][{}][{}][{}][{}][{}]"
                            ,vo.getApplyId()
                            ,vo.getCloud_vendor()
                            ,vo.getProduct()
                            ,vo.getCountry()
                            ,vo.getAutoscalingYN()
                            ,vo.getCriticalValue()
                            ,vo.getDisksize());
                retVal = false;
                continue;
            }


        }

        /*
        for(int i = 0; i < updateList.size(); i++) {
            MemberVo vo = updateList.get(i);
            System.out.print(vo.getApplyId() + " / ");
            System.out.print(vo.getCloud_vendor() + " / ");
            System.out.print(vo.getProduct() + " / ");
            System.out.print(vo.getCountry() + " / ");
            System.out.print(vo.getAutoscalingYN() + " / ");
            System.out.print(vo.getCriticalValue() + " / ");
            System.out.print(vo.getDisksize());
            System.out.println();
        }
        */



        return retVal;
    }
}