package igloosec.monitor.controller;

import com.google.gson.Gson;
import com.sun.mail.smtp.SMTPSaslAuthenticator;
import igloosec.monitor.service.HomeService;
import igloosec.monitor.service.MemberService;
import igloosec.monitor.vo.MemberVo;
import igloosec.monitor.vo.UsageVo;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.mail.*;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.function.Consumer;


@Controller
public class HomeController {




    private JavaMailSender javaMailSender;
    //private Object session;

    public HomeController(HomeService homeService) {
        this.homeService = homeService;
    }



    HomeService homeService;
  //  MemberService memberService;


    @GetMapping("/")
    public String home(Model model) {

        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {

        return "register";
    }



    @GetMapping("/main")
    public String main(Model model) {

        return "main";
    }

    @GetMapping("/policy")
    public String policy(Model model) {

        return "policy";
    }


    @GetMapping("/resource")
    public String resource(Model model) {

        return "resource";
    }


    @GetMapping("/cost")
    public String cost(Model model) {

        return "cost";
    }

    @PostMapping("/userRegister")
    public String joinMember(MemberVo memberVo) throws MessagingException {
        memberVo.setPasswd("1234");
        //memberVo.setAuth("ROLE_USER");


        List<MemberVo> list =homeService.checkMember(memberVo);

        if(list.size() == 0) {

            try {
                sendregistermail(memberVo.getEmail());
            } catch (MessagingException e) {
                e.printStackTrace();
            }
            homeService.joinUser(memberVo);
        }

        return "redirect:/register";
    }

    @ResponseBody
    @RequestMapping(value = "/getLogList.do")
    public List<UsageVo> getLogList(Model model, UsageVo param, HttpSession session ) {
        String emailStr = (String) session.getAttribute("email");
        param.setEmailparam(emailStr);

        List<UsageVo>  list = homeService.selectLogList(param);
        model.addAttribute("list",list);

        return list;
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
    public List<UsageVo> getMeterDetailList(Model model, UsageVo param) {

        List<UsageVo>  list = homeService.selectMeterDetail(param);
        model.addAttribute("list",list);

        return list;
    }

    @RequestMapping(value = "/getChartList.do", method = RequestMethod.GET, produces="text/plain;charset=UTF-8")
    public @ResponseBody String chartList(Locale locale, Model model, UsageVo param) {

        Gson gson = new Gson();
        List<UsageVo>  list =null;
        try
        {
            list = homeService.selectUsage(param);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return gson.toJson(list);

    }


    @RequestMapping(value = "/getChartListbefore.do", method = RequestMethod.GET, produces="text/plain;charset=UTF-8")
    public @ResponseBody String chartListbefore(Locale locale, Model model , UsageVo param) {

        Gson gson = new Gson();

        List<UsageVo>  list = homeService.selectUsagebefore(param);

        return gson.toJson(list);

    }

    @RequestMapping(value = "/getCostTotal.do", method = RequestMethod.GET, produces="text/plain;charset=UTF-8")
    public @ResponseBody String getCostTotal(Locale locale, Model model, UsageVo param) {

        Gson gson = new Gson();

        UsageVo result = homeService.selectCostTotal(param);

        return gson.toJson(result);

    }


    @RequestMapping(value = "/getMeterSum.do", method = RequestMethod.GET, produces="text/plain;charset=UTF-8")
    public @ResponseBody String getMeterSum(Locale locale, Model model, UsageVo param) {

        Gson gson = new Gson();

        List<UsageVo>  list = homeService.selectMeterSum(param);

        return gson.toJson(list);

    }

    @PostMapping("/registerLog.do")
    public String registerLog(UsageVo param, HttpSession session) throws MessagingException, IOException {
        String emailStr = (String) session.getAttribute("email");
        param.setEmailparam(emailStr);


        homeService.registerLog(param);
        return "redirect:/main";
    }

    @PostMapping("/completeLog.do")
    public String completeLog(HttpSession session,MemberVo param) throws MessagingException, IOException {
        String emailStr = (String) session.getAttribute("email");

        UsageVo param2 = new UsageVo();
        System.out.println(param.getTotalParam());
        param2.setUsageparam(param.getTotalParam());
        UsageVo result = homeService.selectCostTotal(param2);
        param.setEmail(emailStr);
        //sendmail(emailStr);
        //server 업로드시 주석해제
        emailStr = emailStr.replace("@","");
        emailStr = emailStr.replace(".","");

        result.setEmailparam(emailStr);





        try {
            shellVMcreate(result);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        homeService.completeLog(param);
        session.setAttribute("step","1");
        return "redirect:/main";
    }



    public void sendmail(String email)  throws MessagingException {



        String host = "outlook.office365.com";
        int port = 587;
        String from = "cloudhelp@igloosec.com";

        Properties props = System.getProperties();


        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.from", from);
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", true);
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.trust", host);



        Session session = Session.getInstance(props, new MyAuthentication());
        session.setDebug(true); //for debug

        Message mimeMessage = new MimeMessage(session);
        mimeMessage.setFrom(new InternetAddress(from));
        mimeMessage.setRecipient(Message.RecipientType.TO,
                new InternetAddress("dongeun.kim@igloosec.com"));
        mimeMessage.setSubject("#장비 등록 알림#");
        mimeMessage.setText(email +" 계정이 장비정보를 등록하였습니다.");

        Transport transport = session.getTransport();

        transport.connect();

        transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
        transport.close();



    }


    public void sendregistermail(String email)  throws MessagingException {



        String host = "outlook.office365.com";
        int port = 587;
        String from = "cloudhelp@igloosec.com";

        Properties props = System.getProperties();


        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.from", from);
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", true);
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.trust", host);



        Session session = Session.getInstance(props, new MyAuthentication());
        session.setDebug(true); //for debug

        Message mimeMessage = new MimeMessage(session);
        mimeMessage.setFrom(new InternetAddress(from));
        mimeMessage.setRecipient(Message.RecipientType.TO,
                new InternetAddress(email));
        mimeMessage.setSubject("#igloo security 모니터링시스템");
        mimeMessage.setText("https://igloocld.com/ 에 접속하셔서, 로그인하시기 바랍니다. \nid: "+ email+ "\n" + "pw: 1234" );

        Transport transport = session.getTransport();

        transport.connect();

        transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
        transport.close();



    }



    public class MyAuthentication extends Authenticator {

        PasswordAuthentication pa;



        public MyAuthentication() {

            pa = new PasswordAuthentication("cloudhelp@igloosec.com", "sp!dertm40");

        }



        public PasswordAuthentication getPasswordAuthentication() {

            return pa;

        }

    }

    private void shellVMcreate(UsageVo result) throws IOException, InterruptedException {




        boolean isWindows = System.getProperty("os.name")
                .toLowerCase().startsWith("windows");

        System.out.println("실행환경이 윈도우인가? " + isWindows);


        String homeDirectory = System.getProperty("user.home");

        System.out.println(homeDirectory);
        Process process;
        if (isWindows) {
            process = Runtime.getRuntime()
                    .exec(String.format("cmd.exe /c dir %s", homeDirectory));

            System.out.println(result.getPathStr()+"/"+result.getShellcom()+" \""+result.getEmailparam()+"\" \""+result.getVmseries()+"\"");
            //System.out.println(result.getPathStr());
        } else {
            process = Runtime.getRuntime()
                    .exec(result.getPathStr()+"/"+result.getShellcom()+" "+result.getEmailparam()+" "+result.getVmseries());
        }


/*        String psRetMsg = "";
        StreamGobbler streamGobbler =
                new StreamGobbler(process.getInputStream(), (item)->{
                    System.out.println(item);
                });
        Executors.newSingleThreadExecutor().submit(streamGobbler);
        int exitCode = process.waitFor();
        assert exitCode == 0;*/
    }


    private static class StreamGobbler implements Runnable {
        private InputStream inputStream;
        private Consumer<String> consumer;

        public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
            this.inputStream = inputStream;
            this.consumer = consumer;
        }


        public void run() {
            try {
                new BufferedReader(new InputStreamReader(inputStream, "euc-kr")).lines()
                        .forEach(consumer);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }




}