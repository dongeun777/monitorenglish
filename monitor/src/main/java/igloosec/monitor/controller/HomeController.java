package igloosec.monitor.controller;

import com.google.gson.Gson;
import com.sun.mail.smtp.SMTPSaslAuthenticator;
import igloosec.monitor.service.HomeService;
import igloosec.monitor.service.MemberService;
import igloosec.monitor.vo.MemberVo;
import igloosec.monitor.vo.UsageVo;
import org.springframework.core.io.ClassPathResource;
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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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
    public String main(Model model, HttpServletRequest request) {
        HttpSession session =request.getSession(false);
        model.addAttribute("grpList", homeService.getGrpList());

        if(session==null) return "redirect:/";
        else return "main";
    }

    @GetMapping("/policy")
    public String policy(Model model) {

        return "policy";
    }


    @GetMapping("/resource")
    public String resource(Model model, HttpServletRequest request) {
        HttpSession session =request.getSession(false);
        if(session==null) return "redirect:/";
        else return "resource";
    }


    @GetMapping("/cost")
    public String cost(Model model, HttpServletRequest request) {
        HttpSession session =request.getSession(false);
        if(session==null) return "redirect:/";
        else return "cost";
    }

    @GetMapping("/member")
    public String member(Model model, HttpServletRequest request) {
        HttpSession session =request.getSession(false);
        if(session==null) return "redirect:/";
        else return "member";
    }


    @ResponseBody
    @RequestMapping(value = "/userRegister")
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
            return "Success";
        }

        else{
            return "fail";
        }

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

        param.setEmail(emailStr);

        homeService.completeLog(param);
        session.setAttribute("step","1");
        return "redirect:/main";
    }


    @PostMapping("/createvm.do")
    public String createvm(HttpSession session,MemberVo param) throws MessagingException, IOException {
        String emailStr = (String) session.getAttribute("email");
        String emailStr2 = (String) session.getAttribute("email");

        UsageVo param2 = new UsageVo();
        System.out.println(param.getTotalParam());
        param2.setUsageparam(param.getTotalParam());
        UsageVo result = homeService.selectCostTotal(param2);
        param.setEmail(emailStr);
        sendtmmail(emailStr);
        //server 업로드시 주석해제
        emailStr = emailStr.replace("@","");
        emailStr = emailStr.replace(".","");

        result.setEmailparam(emailStr);


        try {
            shellVMcreate(result,emailStr2);
        } catch (IOException e) {

        } catch (InterruptedException e) {

        }
        homeService.completeLog2(param);
        session.setAttribute("step","2");
        return "redirect:/main";
    }


    @RequestMapping(value = "/readLog.do", method = RequestMethod.GET, produces="text/plain;charset=UTF-8")
    public @ResponseBody String readLog(Model model, HttpSession session ) {

        String emailStr = (String) session.getAttribute("email");
        emailStr = emailStr.replace("@","");
        emailStr = emailStr.replace(".","");

        String emailStr2 =emailStr+"Rsg";

        String result="notyet";

        String path = homeService.selectPath();
        //System.out.println(path+"/tm5deploy."+emailStr+".log");


        boolean isWindows = System.getProperty("os.name")
                .toLowerCase().startsWith("windows");


        BufferedReader br = null;
        StringBuffer sb = new StringBuffer(); // 테스트용 변수
        try {

            if(isWindows) br = new BufferedReader(new FileReader("C:\\monitor\\monitor\\src\\main\\resources\\static\\tm5deploy.log"));
            else  br = new BufferedReader(new FileReader(path+"/tm5deploy."+emailStr+".log"));
            String line = null;

            while ((line = br.readLine()) != null) {
                //System.out.println(line);
                if (line.contains("end..")) {
                    result ="end";
                    session.setAttribute("rscgrp", emailStr2);
                    break;

                }

                //sb.append(line);
            }

        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
        } finally {
            try {
                if (br != null)
                    br.close();
            } catch (Exception e) {
            }
        }

        return result;
    }



    public void sendmail(String email)  throws MessagingException {



        String host = "outlook.office365.com";
        int port = 587;
        String from = "igloocld@igloosec.com";

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
        String from = "igloocld@igloosec.com";

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


    public void sendtmmail(String email)  throws MessagingException {



        String host = "outlook.office365.com";
        int port = 587;
        String from = "igloocld@igloosec.com";
        String emailstr=email;
        emailstr = emailstr.replace("@","");
        emailstr = emailstr.replace(".","");

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
        mimeMessage.setSubject("#SPiDERTM 접속정보");
        mimeMessage.setText("https://"+emailstr+".igloocld.com/ 에 접속하셔서, 로그인하시기 바랍니다. \nid:igloosec \n" + "pw:sp!dertm40" );

        Transport transport = session.getTransport();

        transport.connect();

        transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
        transport.close();


    }



    public class MyAuthentication extends Authenticator {

        PasswordAuthentication pa;



        public MyAuthentication() {

            pa = new PasswordAuthentication("igloocld@igloosec.com", "d+jndkm#dd9msdf%ds9f8gsFDGKdfg(");

        }



        public PasswordAuthentication getPasswordAuthentication() {

            return pa;

        }

    }

    private void shellVMcreate(UsageVo result,String emailStr) throws IOException, InterruptedException {




        boolean isWindows = System.getProperty("os.name")
                .toLowerCase().startsWith("windows");

        String homeDirectory = System.getProperty("user.home");


        Process process;
        if (isWindows) {
            process = Runtime.getRuntime()
                    .exec(String.format("cmd.exe /c dir %s", homeDirectory));

            System.out.println(result.getPathStr()+"/"+result.getShellcom()+" "+result.getEmailparam()+" "+result.getVmseries()+" "+emailStr);
            //System.out.println(result.getPathStr());
        } else {
            //Runtime.getRuntime().exec().
            process = Runtime.getRuntime()
                    .exec(result.getPathStr()+"/"+result.getShellcom()+" "+result.getEmailparam()+" "+result.getVmseries()+" "+emailStr);
        }


/*        while(i==1) {
            String test = readFile("C:\\monitor\\monitor\\src\\main\\resources\\static\\tm5deploy.log");
            if (test.equals("end")) break;
            else continue;
        }*/

        //readFile test = new readFile();
        //test.start();


    }

    /*public String readFile(String filePath) {
        BufferedReader br = null;
        StringBuffer sb = new StringBuffer(); // 테스트용 변수
        try {
            br = new BufferedReader(new FileReader(filePath));
            String line = null;

            while ((line = br.readLine()) != null) {
                System.out.println(line);
                if (line.contains("end..")) return "end";

                //sb.append(line);
            }

        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
        } finally {
            try {
                if (br!=null)
                    br.close();
            } catch (Exception e) {}
        }

        return "notend";
    }*/



    private static class readFile extends Thread{


        public int readFile(){

        int i =1;
        //String
            while(i==1) {
                BufferedReader br = null;
                StringBuffer sb = new StringBuffer(); // 테스트용 변수
                try {
                    br = new BufferedReader(new FileReader("C:\\monitor\\monitor\\src\\main\\resources\\static\\tm5deploy.log"));
                    String line = null;

                    while ((line = br.readLine()) != null) {
                        System.out.println(line);
                        if (line.contains("end..")) {
                            i--;
                            System.out.println("끝!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                            break;

                        }

                        //sb.append(line);
                    }

                } catch (IOException ioe) {
                    System.out.println(ioe.getMessage());
                } finally {
                    try {
                        if (br != null)
                            br.close();
                    } catch (Exception e) {
                    }
                }



            }

            return i;
        }

        public void run(){
            int i = readFile();

        }
    }




}