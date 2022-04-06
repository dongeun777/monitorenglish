package igloosec.monitor.controller;

import com.google.zxing.qrcode.decoder.Mode;
import igloosec.monitor.service.CostService;
import igloosec.monitor.service.ResourceService;
import igloosec.monitor.vo.CostVo;
import igloosec.monitor.vo.ResourceVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
public class CostController {
    private final CostService costService;
    @Autowired
    public CostController(CostService costService) {
        this.costService = costService;
    }


    @ResponseBody
    @RequestMapping(value = "/getCostList.do")
    public List<CostVo> getCostList(Model model,  CostVo param)  {


        List<CostVo> list = costService.selectCostList(param);

        model.addAttribute("list",list);

        return list;
    }

    // 해외결제 정기결제
    @ResponseBody
    @PostMapping(value = "/subscription/issue-billing")
    public String billingOverseas(HttpServletRequest request, HttpSession session, Model model) {
        String card_number = request.getParameter("card_number");
        String expiry = request.getParameter("expiry");
        String birth = request.getParameter("birth");
        String pwd_2digit = request.getParameter("pwd_2digit");

        model.addAttribute("card_number", card_number);
        model.addAttribute("expiry", expiry);
        model.addAttribute("birth", birth);
        model.addAttribute("pwd_2digit", pwd_2digit);
        model.addAttribute("email", session.getAttribute("email"));

        // 인증 토큰 발급 받기


        return "/payment";
    }
}
