package igloosec.monitor.controller;

import igloosec.monitor.service.ResourceService;
import igloosec.monitor.service.ScheduleService;
import igloosec.monitor.vo.ResourceVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
public class ScheduleController {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleController.class);
    private final ScheduleService scheduleService;
    @Autowired
    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping(value = "/diskAutoscaling.do")
    private String diskAutoscaling() {
        scheduleService.diskAutoscaling();
        return "join";
    }

}
