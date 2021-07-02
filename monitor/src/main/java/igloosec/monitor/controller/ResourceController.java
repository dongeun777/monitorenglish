package igloosec.monitor.controller;

import igloosec.monitor.service.ResourceService;
import igloosec.monitor.vo.ResourceVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
public class ResourceController {

    private static final Logger logger = LoggerFactory.getLogger(ResourceController.class);
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
    @RequestMapping(value = "/requestExpansionShell")
    public ResourceVo requestExpansionShell(HttpSession session, ResourceVo param) {
        // disk expansion
        return resourceService.requestExpansionShell(param.getRscparam(), param.getDiskSize());
    }

    @ResponseBody
    @RequestMapping(value = "/waitDiskExpansionComplete")
    public String waitDiskExpansionComplete(HttpSession session, ResourceVo param) {
        // disk expansion
        if(resourceService.waitDiskExpansionComplete(param.getRscparam(), param.getIdx()) == true) {
            return "succeed";
        } else {
            return "failed";
        }
        //resourceService.addMultiVolume(param.getRscparam(), param.getDiskSize());
    }

    @ResponseBody
    @RequestMapping(value = "/requestRemoveShell")
    public ResourceVo requestRemoveShell(HttpSession session, ResourceVo param) {
        String rscGrp = param.getRscparam();
        String partitionNm = param.getPartitionName();
        String diskNm = param.getDiskName();
        logger.info("requestRemoveShell {}, {}, {}, {}", rscGrp, partitionNm, diskNm);
        // disk remove
        return resourceService.requestRemoveShell(rscGrp, partitionNm, diskNm);
    }

    @ResponseBody
    @RequestMapping(value = "/waitDiskRemoveComplete")
    public String waitDiskRemoveComplete(HttpSession session, ResourceVo param) {
        String rscGrp = param.getRscparam();
        String partitionNm = param.getPartitionName();
        String diskNm = param.getDiskName();
        int idx = param.getIdx();
        logger.info("waitDiskRemoveComplete {}, {}, {}, {}", rscGrp, partitionNm, diskNm, idx);
        // disk remove
        if(resourceService.waitDiskRemoveComplete(rscGrp, idx, partitionNm, diskNm) == true) {
            return "succeed";
        } else {
            return "failed";
        }
    }

    @ResponseBody
    @RequestMapping(value = "/checkDiskWork")
    public boolean checkDiskWork(HttpSession session, ResourceVo param) {
        // disk work check
        return resourceService.checkDiskWork(param.getRscparam());
    }


}
