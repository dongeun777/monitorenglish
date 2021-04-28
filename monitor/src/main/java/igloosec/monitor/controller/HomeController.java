package igloosec.monitor.controller;

import com.google.gson.Gson;
import igloosec.monitor.service.HomeService;
import igloosec.monitor.vo.UsageVo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;



@Controller
public class HomeController {


    public HomeController(HomeService homeService) {
        this.homeService = homeService;
    }

    HomeService homeService;

    @GetMapping("/")
    public String home(Model model) {

        return "login";
    }


    @GetMapping("/main")
    public String main(Model model) {

        return "main";
    }

    @GetMapping("/policy")
    public String policy(Model model) {

        return "policy";
    }



    @ResponseBody
    @RequestMapping(value = "/getUsageList.do")
    public List<UsageVo> getUsageList(Model model, UsageVo param ) {

        List<UsageVo>  list = homeService.selectUsage(param);
        model.addAttribute("list",list);

        return list;
    }


    @ResponseBody
    @RequestMapping(value = "/getMeterDetailList.do")
    public List<UsageVo> getMeterDetailList(Model model) {

        List<UsageVo>  list = homeService.selectMeterDetail();
        model.addAttribute("list",list);

        return list;
    }

    @RequestMapping(value = "/getChartList.do", method = RequestMethod.GET, produces="text/plain;charset=UTF-8")
    public @ResponseBody String chartList(Locale locale, Model model, UsageVo param) {

        Gson gson = new Gson();

        List<UsageVo>  list = homeService.selectUsage(param);

        return gson.toJson(list);

    }


    @RequestMapping(value = "/getChartListbefore.do", method = RequestMethod.GET, produces="text/plain;charset=UTF-8")
    public @ResponseBody String chartListbefore(Locale locale, Model model , UsageVo param) {

        Gson gson = new Gson();

        List<UsageVo>  list = homeService.selectUsagebefore(param);

        return gson.toJson(list);

    }


    @RequestMapping(value = "/getMeterSum.do", method = RequestMethod.GET, produces="text/plain;charset=UTF-8")
    public @ResponseBody String getMeterSum(Locale locale, Model model) {

        Gson gson = new Gson();

        List<UsageVo>  list = homeService.selectMeterSum();

        return gson.toJson(list);

    }



}