package igloosec.monitor.controller;



import igloosec.monitor.service.MemberService;
import igloosec.monitor.vo.MemberVo;
import igloosec.monitor.vo.UsageVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@Controller
public class MemberController {

    private final MemberService memberService;
    @Autowired
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }


    @ResponseBody
    @RequestMapping(value = "/login.do")
    public String Login(MemberVo memberVo, HttpSession session) {

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        MemberVo user = (MemberVo) memberService.loadUserByUsername(memberVo.getEmail());
        if (user == null){
            return "idNull";

        }else{

            if(!passwordEncoder.matches(memberVo.getPasswd(), user.getPassword())) {
                return "pwCheck";

            }else{


               session.setAttribute("loginCheck",true);
 /*                session.setAttribute("email",memberVo.getEmail());
                session.setAttribute("auth",user.getAuth());
                session.setAttribute("rscgrp",user.getRscGrp());
                session.setAttribute("step",user.getStep());*/

                return "Success";
            }

        }




                

    }

    @RequestMapping(value="/logout")
    public String logoutProcess(HttpSession session) {

        session.removeAttribute("loginCheck");
        session.removeAttribute("email");
        session.removeAttribute("step");
        session.removeAttribute("rscgrp");
        session.removeAttribute("auth");

/*        session.setAttribute("loginCheck",null);
        session.setAttribute("email",null);
        session.setAttribute("step",null);
        session.setAttribute("rscgrp",null);
        session.setAttribute("auth",null);*/

        return "redirect:/";
    }




    @ResponseBody
    @RequestMapping(value = "/getMemberList.do")
    public List<MemberVo> getMemberList(Model model) {

        List<MemberVo>  list = memberService.selectMemberList();
        model.addAttribute("list",list);

        return list;
    }

    @PostMapping("/joinUser")
    public String joinMember(MemberVo memberVo) {
        memberService.joinUser(memberVo);
        return "redirect:/member";
    }

    @ResponseBody
    @RequestMapping(value = "/modifyUserPwd")
    public String modifyUserPwd(MemberVo memberVo) {
        return memberService.modifyUserPwd(memberVo);
    }

    @PostMapping("/modifyGrpIp")
    public String modifyGrpIpMember(MemberVo memberVo) {
        memberService.modifyGrpIp(memberVo);
        return "redirect:/member";
    }


    @PostMapping("/deleteUser")
    public String deleteMember(MemberVo memberVo) {


        UsageVo param2 = new UsageVo();
        UsageVo result = memberService.setDeletePath();

        try {
            shellVMdelete(result,memberVo);
        } catch (IOException e) {

        } catch (InterruptedException e) {

        }

        memberService.deleteUser(memberVo);

        return "redirect:/member";
    }



    private void shellVMdelete(UsageVo result,MemberVo memberVo) throws IOException, InterruptedException {

        boolean isWindows = System.getProperty("os.name")
                .toLowerCase().startsWith("windows");

        String homeDirectory = System.getProperty("user.home");


        Process process;
        if (isWindows) {
            process = Runtime.getRuntime()
                    .exec(String.format("cmd.exe /c dir %s", homeDirectory));

            System.out.println(result.getPathStr()+"/"+result.getShellDeletecom()+" "+ memberVo.getRscGrp());
            //System.out.println(result.getPathStr());
        } else {
            //Runtime.getRuntime().exec().
            process = Runtime.getRuntime()
                    .exec(result.getPathStr()+"/"+result.getShellDeletecom()+" "+ memberVo.getRscGrp());
        }



    }


    @ResponseBody
    @RequestMapping(value = "/getUserInfo")
    public MemberVo getUserInfo(MemberVo memberVo) {
        return memberService.getUserInfo(memberVo.getEmail());
    }

    @ResponseBody
    @RequestMapping(value = "/getUserProductInfoList.do")
    public List<MemberVo> getUserProductInfoList(MemberVo memberVo) {
        if(memberVo.getEmail() == null) {
            return null;
        }
        return memberService.getUserProductInfoList(memberVo.getEmail());
    }

    @ResponseBody
    @RequestMapping(value = "/setUserInfoSave")
    public boolean setUserInfoSave(String param) {
        return memberService.setUserInfoSave(param);
    }

//    @RequestMapping(value = "/readDeleteLog.do", method = RequestMethod.GET, produces="text/plain;charset=UTF-8")
//    public @ResponseBody String readLog(MemberVo memberVo, HttpSession session ) {
//
//        String emailStr = memberVo.getEmail();
//        String emailStr1 = emailStr.replace("@","");
//        String emailStr2 = emailStr1.replace(".","");
//
//        String emailStr3 = emailStr2 + "Rsg";
//
//        String result="notyet";
//
//        String path = memberService.selectPath();
//        //System.out.println(path+"/tm5deploy."+emailStr+".log");
//
//
//        boolean isWindows = System.getProperty("os.name")
//                .toLowerCase().startsWith("windows");
//
//
//        BufferedReader br = null;
//        StringBuffer sb = new StringBuffer(); // 테스트용 변수
//        try {
//
//            if(isWindows) br = new BufferedReader(new FileReader("C:\\monitor\\monitor\\src\\main\\resources\\static\\tm5remove.log"));
//            else  br = new BufferedReader(new FileReader(path+"/tm5remove."+emailStr3+".log"));
//            String line = null;
//
//            while ((line = br.readLine()) != null) {
//                System.out.println(line);
//                if (line.contains("end..")) {
//                    result ="end";
//                    break;
//                }
//                else {
//                    return line;
//                }
//
////                sb.append(line);
//            }
//
//        } catch (IOException ioe) {
//            System.out.println(ioe.getMessage());
//        } finally {
//            try {
//                if (br != null)
//                    br.close();
//            } catch (Exception e) {
//            }
//        }
//
//        return result;
//    }

}
