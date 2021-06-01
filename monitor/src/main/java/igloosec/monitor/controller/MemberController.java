package igloosec.monitor.controller;



import igloosec.monitor.service.MemberService;
import igloosec.monitor.vo.MemberVo;
import igloosec.monitor.vo.UsageVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.lang.reflect.Member;
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
                session.setAttribute("email",memberVo.getEmail());
                session.setAttribute("auth",user.getAuth());
                session.setAttribute("rscgrp",user.getRscGrp());
                session.setAttribute("step",user.getStep());
                //session.setAttribute();
                System.out.println(user.getAuth());
              //  session.setAttribute("auth",memberVo.getAuthorities());
                return "Success";
            }

        }




                

    }

    @RequestMapping(value="/logout")
    public String logoutProcess(HttpSession session) {

        session.setAttribute("loginCheck",null);
        session.setAttribute("email",null);
        session.setAttribute("step",null);
        session.setAttribute("rscgrp",null);
        session.setAttribute("auth",null);

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


    @PostMapping("/modifyUser")
    public String updatePw(MemberVo memberVo,HttpSession session) {
        memberVo.setEmail((String) session.getAttribute("email"));
        memberService.modifyUser(memberVo);
        return "redirect:/member";
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

}
