package igloosec.monitor.controller;

import igloosec.monitor.service.ResourceService;
import igloosec.monitor.vo.ResourceVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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
}
