package igloosec.monitor.controller;

import igloosec.monitor.service.CostService;
import igloosec.monitor.service.ResourceService;
import igloosec.monitor.vo.CostVo;
import igloosec.monitor.vo.ResourceVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

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
    public List<CostVo> getCostList(Model model) {


        List<CostVo> list = costService.selectCostList();

        model.addAttribute("list",list);

        return list;
    }
}
