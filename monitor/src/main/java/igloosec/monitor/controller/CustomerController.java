package igloosec.monitor.controller;


import igloosec.monitor.service.CustomerService;
import igloosec.monitor.service.MemberService;
import igloosec.monitor.vo.MemberVo;
import igloosec.monitor.vo.UsageVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@Controller
public class CustomerController {

    private final CustomerService customerService;
    @Autowired
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }



    @ResponseBody
    @RequestMapping(value = "/getCustomerList.do")
    public List<MemberVo> getMemberList(Model model,UsageVo param) {

        List<MemberVo>  list = customerService.selectMemberList(param);
        model.addAttribute("list",list);

        return list;
    }



}
