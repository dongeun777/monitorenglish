package igloosec.monitor.controller;

import igloosec.monitor.service.ResourceService;
import igloosec.monitor.vo.ResourceVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
public class ResourceController {

    private final ResourceService resourceService;
    @Autowired
    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }


    @ResponseBody
    @RequestMapping(value = "/getResourceList.do")
    public List<ResourceVo> getResourceList(Model model,ResourceVo param) {


        List<ResourceVo>  list = resourceService.selectResourceList(param);

        model.addAttribute("list",list);

        return list;
    }

    @ResponseBody
    @RequestMapping(value = "/getDiskUsageList.do")
    public List<ResourceVo> getDiskUsageList(Model model,ResourceVo param) {


        List<ResourceVo>  list = resourceService.selectDiskUsageList(param);

        model.addAttribute("list",list);

        return list;
    }

    @ResponseBody
    @RequestMapping(value = "/getResourceUsage.do")
    public ResourceVo getResourceUsage(Model model,ResourceVo param) {


        ResourceVo vo = resourceService.selectResourceUsage(param);

        model.addAttribute("list",vo);

        return vo;
    }

    @ResponseBody
    @RequestMapping(value = "/getDiskPrice.do")
    public List<ResourceVo> getDiskPrice(Model model) {


        List<ResourceVo>  list = resourceService.selectDiskPrice();

        model.addAttribute("list",list);

        return list;
    }

    @ResponseBody
    @RequestMapping(value = "/setDiskExpansion.do")
    public void setDiskExpansion(HttpSession session, ResourceVo param) {
        // disk expansion
        resourceService.addMultiVolume(param.getRscparam(), param.getDiskSize());
        /*

        model.addAttribute("list",list);
        */
        return;
    }

}
