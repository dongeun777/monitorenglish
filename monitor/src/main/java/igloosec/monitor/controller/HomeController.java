package igloosec.monitor.controller;

import com.google.gson.Gson;
import com.sun.mail.smtp.SMTPSaslAuthenticator;
import igloosec.monitor.CommonUtil;
import igloosec.monitor.TOTPTokenValidation;
import igloosec.monitor.service.HomeService;
import igloosec.monitor.service.MemberService;
import igloosec.monitor.service.ResourceService;
import igloosec.monitor.vo.LeadsInfoVo;
import igloosec.monitor.vo.MemberVo;
import igloosec.monitor.vo.UsageVo;
import org.apache.commons.codec.binary.Base32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;


@Controller
public class HomeController {

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);


    private JavaMailSender javaMailSender;
    //private Object session;

    public HomeController(HomeService homeService) {
        this.homeService = homeService;
    }



    HomeService homeService;
    MemberService memberService;

    private static final String MAIL_HOST = "outlook.office365.com";
    private static final int MAIL_PORT = 587;
    private static final String MAIL_FROM = "igloocld@igloosec.com";
    @GetMapping("/")
    public String home(Model model) {

        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        return "register";
    }

    @GetMapping("/insert")
    public String insert(Model model, HttpSession session) {

        model.addAttribute("grpList", homeService.getGrpList());

        if(session.getAttribute("email")==null) return "redirect:/";
        else return "insert";
    }

    @GetMapping("/list")
    public String list(Model model, HttpSession session) {

        model.addAttribute("grpList", homeService.getGrpList());

        if(session.getAttribute("email")==null) return "redirect:/";
        else return "list";
    }

    @GetMapping("/main")
    public String main(Model model, HttpSession session) {

        String emailStr = (String) session.getAttribute("email");
        MemberVo param =new MemberVo();
        param.setEmail(emailStr);

        if(session.getAttribute("rscgrp")!=null || session.getAttribute("rscgrp")!="" ) model.addAttribute("grpList", homeService.getGrpList());
        if(session.getAttribute("rscgrp")!=null || session.getAttribute("rscgrp")!="") model.addAttribute("grpCustList", homeService.getCustGrpList(param));

        if(session.getAttribute("email")==null) return "redirect:/";
        else return "main";
    }

    @GetMapping("/policy")
    public String policy(Model model) {

        return "policy";
    }

    @GetMapping("/reset")
    public String reset(Model model) {

        return "reset";
    }


    @GetMapping("/deploy")
    public String deploy(Model model, HttpSession session) {

        if(session.getAttribute("email")==null) return "redirect:/";
        else return "deploy";
    }


    @GetMapping("/productlist")
    public String productlist(Model model, HttpSession session) {

        if(session.getAttribute("email")==null) return "redirect:/";
        else return "productlist";
    }

    @GetMapping("/product")
    public String product(Model model, HttpSession session) {

        if(session.getAttribute("email")==null) return "redirect:/";
        else return "product";
    }


    @GetMapping("/resource")
    public String resource(Model model, HttpSession session) {

        if(session.getAttribute("email")==null) return "redirect:/";
        else return "resource";
    }


    @GetMapping("/cost")
    public String cost(Model model, HttpSession session) {

        if(session.getAttribute("email")==null) return "redirect:/";
        else return "cost";
    }

    @GetMapping("/member")
    public String member(Model model, HttpSession session) {

        if(session.getAttribute("email")==null) return "redirect:/";
        else return "member";
    }

    @GetMapping("/customer")
    public String customer(Model model, HttpSession session) {

        if(session.getAttribute("email")==null) return "redirect:/";
        else return "customer";
    }



    @ResponseBody
    @RequestMapping(value = "/rscCheck.do")
    public int rscCheck(MemberVo param) throws MessagingException {

        List<MemberVo> list = homeService.rscCheck(param);

        int retVal = list.size();

        return retVal;

    }

    @ResponseBody
    @RequestMapping(value = "/userRegister")
    public String joinMember(MemberVo memberVo) throws MessagingException {

        List<MemberVo> list =homeService.checkMember(memberVo);
        logger.info("[{}] acount size : {}", memberVo.getEmail(),  list.size());
        String retVal = null;

        // 신규 고객일 경우
        if(list.size() == 0) {
            String firstPasswd = firstPassword();
            memberVo.setPasswd(firstPasswd);

            String secretKey = generateSecretKey();
            memberVo.setSecretkey(secretKey);

            String email = "IGLOOSECURITY";
            String barcodeUrl = getGoogleAuthenticatorBarCode(secretKey, email);
            memberVo.setQrcord(barcodeUrl);

            try {
               sendregistermail(memberVo.getEmail(),firstPasswd);
               //sendtmmail(memberVo.getEmail());

               // sendMailRepeatRequest(memberVo.getEmail());
            } catch (MessagingException e) {
                logger.error(CommonUtil.getPrintStackTrace(e));
            }
            homeService.joinUser(memberVo);
            retVal = "Success";
        }
        // 기존에 요청했던 고객일 경우
        else{
            // 계정이 이미 있으므로 신청 내역이 있다는 내용 메일 전송해 주기
            try {
                sendMailRepeatRequest(memberVo.getEmail());
            } catch (MessagingException e) {
                logger.error(CommonUtil.getPrintStackTrace(e));
            }
            retVal = "fail";
        }

        // 고객 유입 메일 전송
        try {
            sendMailCustomerInflux(memberVo.getEmail());
        } catch (MessagingException e) {
            logger.error(CommonUtil.getPrintStackTrace(e));
        }
        
        return retVal;
    }

    @ResponseBody
    @RequestMapping(value = "/googleVerify.do")
    public String equalCode(MemberVo memberVo, HttpSession session) {

        String userSecretKey = homeService.selectSecretKey(memberVo);
        String inputCode =  memberVo.getMfacode();
        //TOTPTokenValidation.

       // if (TOTPTokenValidation.validate(inputCode,userSecretKey)||memberVo.getEmail().equals("admin@igloosec.com")) {
            if (TOTPTokenValidation.validate(inputCode,userSecretKey)) {
            MemberVo user = homeService.selectMember(memberVo.getEmail());
            System.out.println(user.getAuth());
            session.setAttribute("loginCheck",true);
            session.setAttribute("email",memberVo.getEmail());
            session.setAttribute("auth",user.getAuth());
            session.setAttribute("rscgrp",user.getRscGrp());
            session.setAttribute("step",user.getStep());
            session.setAttribute("rowkey",user.getRowkey());
            return "success";
        }
        else {
            return "fail";
        }
    }

    @ResponseBody
    @RequestMapping(value = "/getImage.do")
    public String googleUrl(MemberVo memberVo) {
        String userQrCord = homeService.selectQrCord(memberVo);
        return userQrCord;
    }

    @ResponseBody
    @RequestMapping(value = "/grpChange.do")
    public String grpChange(MemberVo memberVo, HttpSession session) {
        session.setAttribute("rscgrp",memberVo.getRscGrp());
        return "change";
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
    @RequestMapping(value = "/getLeadsList.do")
    public List<LeadsInfoVo> getLeadsList(Model model, HttpSession session ) {
        String emailStr = (String) session.getAttribute("email");

        List<LeadsInfoVo>  list = homeService.selectLeadsList(emailStr);
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
        if (list != null){
            for(int i = 0; i < list.size(); i++ ){
                list.get(i).setQuantity(list.get(i).getQuantity().replace(".00",""));
            }
        }

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
        param.setLogid(UUID.randomUUID().toString().replace("-",""));

        homeService.registerLog(param);
        return "redirect:/main";
    }

    @PostMapping("/deleteLog.do")
    public String deleteLog(String logid)  {

        homeService.deleteLog(logid);
        return "redirect:/main";
    }

    @PostMapping("/leadsToProduct.do")
    public String leadsToProduct(LeadsInfoVo vo,HttpSession session)  {
        String emailStr = (String) session.getAttribute("email");
        String uuid = UUID.randomUUID().toString().replace("-","");
        uuid =emailStr+uuid;
        vo.setEmailAddr(emailStr);
        vo.setRowKey(uuid);

        homeService.goNext(vo);
        session.setAttribute("rowkey",uuid);
        session.setAttribute("step","0");


        homeService.leadsToProduct(vo);

        return "redirect:/main";
    }



    @PostMapping("/completeLog.do")
    public String completeLog(HttpSession session,MemberVo param) throws MessagingException, IOException {
        String emailStr = (String) session.getAttribute("email");


        param.setEmail(emailStr);

        homeService.completeLog(param);
        homeService.completeApplyLog(param);
        session.setAttribute("step","1");
        return "redirect:/main";
    }

    @PostMapping("/goBack.do")
    public String goBack(HttpSession session) throws MessagingException, IOException {
        String emailStr = (String) session.getAttribute("email");

        homeService.goBack(emailStr);
        session.setAttribute("step","0");
        return "redirect:/main";
    }

    @PostMapping("/goChoice.do")
    public String goChoice(HttpSession session) throws MessagingException, IOException {
        String rowkey = (String) session.getAttribute("rowkey");
        String emailStr = (String) session.getAttribute("email");

        homeService.goChoice(emailStr);
        homeService.deleteChoice1(rowkey);
        homeService.deleteChoice2(rowkey);
        session.setAttribute("step","100");
        return "redirect:/main";
    }




    @PostMapping("/createvm.do")
    public String createvm(HttpSession session,MemberVo param) throws MessagingException, IOException {
        String emailStr = (String) session.getAttribute("email");
        String emailStr2 = (String) session.getAttribute("email");

        UsageVo param2 = new UsageVo();

        param2.setUsageparam(param.getTotalParam());
        UsageVo result = homeService.selectCostTotal(param2);
        param.setEmail(emailStr);
        sendtmmail(emailStr);
        //server 업로드시 주석해제
        emailStr = emailStr.replace("@","");
        emailStr = emailStr.replace(".","");

        result.setEmailparam(emailStr);

        // country, product, vendor 조회
        UsageVo shellParam = homeService.selectShellParam(emailStr2);

        result.setCountry(shellParam.getCountry());
        result.setProduct(shellParam.getProduct());
        result.setVendor(shellParam.getVendor());

        LeadsInfoVo parameter = new LeadsInfoVo();
        parameter.setRowKey(emailStr);
        parameter.setVendor(shellParam.getVendor());
        parameter.setCustomerCountry(shellParam.getCountry());
        parameter.setOfferDisplayName(shellParam.getProduct());
        parameter.setEmailAddr(emailStr2);
        parameter.setRscGrp(emailStr+"Rsg");
        homeService.insertToProduct(parameter);
        try {
            shellVMcreate(result,emailStr2);
        } catch (IOException e) {

        } catch (InterruptedException e) {

        }

        param.setId(result.getId());
        homeService.completeLog2(param);
        homeService.completeApplyLog2(param);
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



    @ResponseBody
    @RequestMapping(value = "/pwReset.do")
    public String pwReset(MemberVo memberVo) throws MessagingException {

        List<MemberVo> list =homeService.checkResetMember(memberVo);

        if(list.size() == 0) {
            return "fail";
        }
        else{

            String firstPasswd = firstPassword();
            memberVo.setPasswd(firstPasswd);

            String secretKey = generateSecretKey();
            memberVo.setSecretkey(secretKey);

            try {
                sendResetMail(memberVo.getEmail(),firstPasswd);
            } catch (MessagingException e) {
                e.printStackTrace();
            }
            homeService.resetPass(memberVo);
            return "success";

        }

    }


    @ResponseBody
    @RequestMapping(value = "/getPeriod.do")
    public String getPeriod(MemberVo memberVo, HttpSession session )  {

        String emailStr = (String) session.getAttribute("email");
        memberVo.setEmail(emailStr);
        String result = homeService.getPeriod(memberVo);
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

    public void sendResetMail(String email,String firstPasswd)  throws MessagingException {



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
        mimeMessage.setSubject("Your password has been changed");
        mimeMessage.setContent("<html><head><title>Welcome to IGLOO Cloud!</title><link rel=\"SHORTCUT ICON\" >\n" +
                "    \n" +
                "<meta name=\"robots\" content=\"noindex, nofollow\">\n" +
                "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n" +
                "    <!-- utf-8 works for most cases -->\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <!-- Forcing initial-scale shouldn't be necessary -->\n" +
                "    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n" +
                "    <!-- Use the latest (edge) version of IE rendering engine -->\n" +
                "      <meta name=\"x-apple-disable-message-reformatting\">\n" +
                "      \n" +
                "    <title>Welcome to IGLOO Security Cloud!</title>\n" +
                "    <!-- The title tag shows in email notifications, like Android 4.4. -->\n" +
                "    <!-- Please use an inliner tool to convert all CSS to inline as inpage or external CSS is removed by email clients -->\n" +
                "    <!-- important in CSS is used to prevent the styles of currently inline CSS from overriding the ones mentioned in media queries when corresponding screen sizes are encountered -->\n" +
                "\n" +
                "    <!-- CSS Reset -->\n" +
                "    <style type=\"text/css\">\n" +
                "/* What it does: Remove spaces around the email design added by some email clients. */\n" +
                "      /* Beware: It can remove the padding / margin and add a background color to the compose a reply window. */\n" +
                "html, body {\n" +
                "    margin: 0 !important;\n" +
                "    padding: 0 !important;\n" +
                "    height: 100% !important;\n" +
                "    width: 100% !important;\n" +
                "    font-weight: 100;\n" +
                "}\n" +
                "/* What it does: Stops email clients resizing small text. */\n" +
                "* {\n" +
                "    -ms-text-size-adjust: 100%;\n" +
                "    -webkit-text-size-adjust: 100%;\n" +
                "}\n" +
                "/* What it does: Forces Outlook.com to display emails full width. */\n" +
                ".ExternalClass {\n" +
                "    width: 100%;\n" +
                "}\n" +
                "/* What is does: Centers email on Android 4.4 */\n" +
                "div[style*=\"margin: 16px 0\"] {\n" +
                "    margin: 0 !important;\n" +
                "}\n" +
                "/* What it does: Stops Outlook from adding extra spacing to tables. */\n" +
                "table, td {\n" +
                "    mso-table-lspace: 0pt !important;\n" +
                "    mso-table-rspace: 0pt !important;\n" +
                "}\n" +
                "/* What it does: Fixes webkit padding issue. Fix for Yahoo mail table alignment bug. Applies table-layout to the first 2 tables then removes for anything nested deeper. */\n" +
                "table {\n" +
                "    border-spacing: 0 !important;\n" +
                "    border-collapse: collapse !important;\n" +
                "    table-layout: fixed !important;\n" +
                "    margin: 0 auto !important;\n" +
                "}\n" +
                "table table table {\n" +
                "    table-layout: auto;\n" +
                "}\n" +
                "/* What it does: Uses a better rendering method when resizing images in IE. */\n" +
                "img {\n" +
                "    -ms-interpolation-mode: bicubic;\n" +
                "}\n" +
                "/* What it does: Overrides styles added when Yahoo's auto-senses a link. */\n" +
                ".yshortcuts a {\n" +
                "    border-bottom: none !important;\n" +
                "}\n" +
                "/* What it does: Another work-around for iOS meddling in triggered links. */\n" +
                "a[x-apple-data-detectors] {\n" +
                "    color: inherit !important;\n" +
                "}\n" +
                ".email-header {\n" +
                "    background-color: black\n" +
                "}\n" +
                ".icon {\n" +
                "    margin-top: -60px\n" +
                "}\n" +
                ".white-component {\n" +
                "    padding-top: 10px\n" +
                "}\n" +
                ".label {\n" +
                "    font-family: sans-serif;\n" +
                "    font-size: 8px;\n" +
                "    border-radius: 2px;\n" +
                "    mso-height-rule: exactly;\n" +
                "    line-height: 20px;\n" +
                "    color: #ffffff;\n" +
                "    background-color: black;\n" +
                "    display: inline-block;\n" +
                "    padding: 1px 5px 0px 5px;\n" +
                "    margin-bottom: 5px;\n" +
                "    margin-top: 10px;\n" +
                "}\n" +
                "webversion {\n" +
                "    color: #97999b;\n" +
                "    text-decoration: underline;\n" +
                "    text-align: center;\n" +
                "    font-size: 12px;\n" +
                "    font-family: Gotham, \"Helvetica Neue\", Helvetica, Arial, \"sans-serif\";\n" +
                "}\n" +
                ".email-footer {\n" +
                "    background-color: #2c3338\n" +
                "}\n" +
                "a {\n" +
                "    text-decoration: none\n" +
                "}\n" +
                "</style>\n" +
                "\n" +
                "    <!-- Progressive Enhancements -->\n" +
                "    <style type=\"text/css\">\n" +
                "/* What it does: Hover styles for buttons */\n" +
                ".button-td,  .button-a {\n" +
                "    transition: all 100ms ease-in;\n" +
                "}\n" +
                ".button-td:hover,  .button-a:hover {\n" +
                "    background: #43d220 !important;\n" +
                "}\n" +
                ".partner-logo img {\n" +
                "   /* display: inline-block;*/\n" +
                "\n" +
                "}\n" +
                "\n" +
                "/* Media Queries */\n" +
                "@media screen and (max-width: 600px) {\n" +
                ".icon {\n" +
                "    margin-top: 0px\n" +
                "}\n" +
                ".label {\n" +
                "    margin-left: 0\n" +
                "}\n" +
                ".email-container {\n" +
                "    width: 100% !important;\n" +
                "}\n" +
                "span[class=bodycopy], .bodycopy {\n" +
                "    font-size: 16px !important;\n" +
                "    line-height: 26px !important;\n" +
                "    font-weight: normal !important;\n" +
                "\t\n" +
                "}\n" +
                "  \n" +
                "@media only screen and (max-width: 480px) {\n" +
                "\n" +
                ".full {\n" +
                "\n" +
                "display:block;\n" +
                "direction: rtl;\n" +
                "width:100%;\n" +
                "\n" +
                "\n" +
                "\n" +
                "}\n" +
                "\n" +
                "}\n" +
                "/* What it does: Forces elements to resize to the full width of their container. Useful for resizing images beyond their max-width. */\n" +
                ".fluid,  .fluid-centered {\n" +
                "    max-width: 50px !important;\n" +
                "    height: auto !important;\n" +
                "    margin-left: auto !important;\n" +
                "    margin-right: auto !important;\n" +
                "}\n" +
                "/* And center justify these ones. */\n" +
                ".fluid-centered {\n" +
                "    margin-left: auto !important;\n" +
                "    margin-right: auto !important;\n" +
                "}\n" +
                "/* What it does: Forces table cells into full-width rows. */\n" +
                ".stack-column,  .stack-column-center {\n" +
                "    display: block !important;\n" +
                "    width: 100% !important;\n" +
                "    max-width: 100% !important;\n" +
                "    direction: ltr !important;\n" +
                "}\n" +
                "/* And center justify these ones. */\n" +
                ".stack-column-center {\n" +
                "    text-align: center !important;\n" +
                "}\n" +
                "/* What it does: Generic utility class for centering. Useful for images, buttons, and nested tables. */\n" +
                ".center-on-narrow {\n" +
                "    text-align: center !important;\n" +
                "    display: block !important;\n" +
                "    margin-left: auto !important;\n" +
                "    margin-right: auto !important;\n" +
                "    float: none !important;\n" +
                "}\n" +
                "table.center-on-narrow {\n" +
                "    display: inline-block !important;\n" +
                "}\n" +
                "\t.icontable {\n" +
                "\t\twidth: 100% !important;\n" +
                "\t\t/* margin-bottom: 20px !important; */\n" +
                "\t\t\n" +
                "\t}\t\n" +
                "\tblockquote { \n" +
                "  display: block;\n" +
                "  margin-top: 1em;\n" +
                "  margin-bottom: 1em;\n" +
                "  margin-left: 40px;\n" +
                "  margin-right: 40px;\n" +
                "}\n" +
                "}\n" +
                "      hr {\n" +
                "       border-top: 0px solid #D5DCE5;\n" +
                "        \n" +
                "      }\n" +
                "</style>\n" +
                "    </head>\n" +
                "<body bgcolor=\"#F8F9FC\" width=\"100%\" style=\"margin: 0;\" yahoo=\"yahoo\"><br>\n" +
                "\n" +
                "    \n" +
                "    <table bgcolor=\"#F8F9FC\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" height=\"100%\" width=\"100%\" style=\"border-collapse:collapse;\">\n" +
                "      <tbody><tr>\n" +
                "        <td><center style=\"width: 100%;\" class=\"\">\n" +
                "            \n" +
                "            <!-- Visually Hidden Preheader Text : BEGIN -->\n" +
                "            <div style=\"display:none;font-size:1px;line-height:1px;max-height:0px;max-width:0px;opacity:0;overflow:hidden;mso-hide:all;font-family: sans-serif;\">Welcome to IGLOO Security Cloud!</div>\n" +
                "            <!-- Visually Hidden Preheader Text : END --> \n" +
                "            \n" +
                "         \n" +
                "            \n" +
                "            <!-- Email Body : BEGIN -->\n" +
                "            <table cellspacing=\"0\" cellpadding=\"0\" border=\"0\" align=\"center\" bgcolor=\"#ffffff\" width=\"600\" style='box-shadow: 0 .15rem 1.75rem 0 rgba(58,59,69,.15)!important;     border: 1px solid #e3e6f0;\n" +
                "    border-radius: .35rem;' class=\"email-container\">\n" +
                "\t\t\t  <!-- Header // START -->    \n" +
                "          \t<tbody><tr>\n" +
                "              <td bgcolor=\"#335ACB\" style=\"padding: 10px 0 10px 10px; text-align: left\" class=\"\"><a href=\"http://www.igloosec.co.kr/index.do\" target=\"_blank\" title=\"IGLOO SECURITY\" data-targettype=\"webpage\">\n" +
                "               </a></td>\n" +
                "              \n" +
                "            </tr>\n" +
                "\t\t\t\t\n" +
                "               <!-- Header // END --> \n" +
                "\t\t\t\t<tr>\n" +
                "                        <td dir=\"ltr\" valign=\"top\" style=\"font-family: sans-serif; font-size: 16px; mso-height-rule: exactly;  color: #474444; padding: 20px;0px 0px 20px; text-align: left;\" class=\"bodycopy\"><h1 style=\"line-height: 40px; margin-left:10px;\" class=\"\">Your password has been changed</h1>\n" +
                "</td></tr><tr>\n" +
                "<td dir=\"ltr\" valign=\"top\" style=\"font-family: sans-serif; font-size: 16px; mso-height-rule: exactly;  color: #474444; padding: 20px;0px 0px 10px; text-align: left;\" class=\"bodycopy\">\n" +
                "\t<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"550\">\n" +
                "\t\t<tbody><tr>\n" +
                "\t\t\t<td width=\"30%\" valign=\"top\"><strong class=\"\">URL:</strong></td>\n" +
                "\t\t\t<td> <a href=\"https://igloocld.com/ \" style=\"color:#E20082;text-decoration:none;\" data-targettype=\"webpage\">https://igloocld.com/ </a></td>\n" +
                "\t\t</tr>\n" +
                "\t\t<tr><td width=\"30%\" valign=\"top\"><strong class=\"\">User Name:</strong></td>\n" +
                "\t\t\t<td>"+email+"</td></tr>\n" +
                "\t\t\n" +
                "\t\t<tr><td width=\"30%\" valign=\"top\"><strong class=\"\">Password:</strong></td>\n" +
                "\t\t\t<td style=\"overflow-wrap: break-word;\">"+firstPasswd+"</td></tr>\n" +
                "\t\t<tr><td colspan=\"2\" style=\"font-size: 13px\"><br><i>* You’ll be asked to\n" +
                "create a permanent password on first login.</i></td></tr>\n" +
                "\t</tbody></table></td></tr>\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\n" +
                "\n" +
                "            \n" +
                "           \n" +
                "\n" +
                "\n" +
                "\t\t\t\n" +
                "           \t\t\t\n" +
                "            \n" +
                "          </center></td>\n" +
                "      </tr>\n" +
                "    </tbody></table>\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "<!-- Footer -->\n" +
                "</body></html>","text/html; charset=UTF-8");
        //mimeMessage.setText("https://igloocld.com/ 에 접속하셔서, 로그인하시기 바랍니다. \nid: "+ email+ "\n" + "pw: 1234" );
        Transport transport = session.getTransport();

        transport.connect();

        transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
        transport.close();



    }
    public void sendregistermail(String email,String firstPasswd)  throws MessagingException {



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
        mimeMessage.setSubject("Welcome to IGLOO SECURITY Cloud!");
        mimeMessage.setContent("<html><head><title>Welcome to IGLOO Cloud!</title><link rel=\"SHORTCUT ICON\" >\n" +
                "    \n" +
                "<meta name=\"robots\" content=\"noindex, nofollow\">\n" +
                "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n" +
                "    <!-- utf-8 works for most cases -->\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <!-- Forcing initial-scale shouldn't be necessary -->\n" +
                "    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n" +
                "    <!-- Use the latest (edge) version of IE rendering engine -->\n" +
                "      <meta name=\"x-apple-disable-message-reformatting\">\n" +
                "      \n" +
                "    <title>Welcome to IGLOO Security Cloud!</title>\n" +
                "    <!-- The title tag shows in email notifications, like Android 4.4. -->\n" +
                "    <!-- Please use an inliner tool to convert all CSS to inline as inpage or external CSS is removed by email clients -->\n" +
                "    <!-- important in CSS is used to prevent the styles of currently inline CSS from overriding the ones mentioned in media queries when corresponding screen sizes are encountered -->\n" +
                "\n" +
                "    <!-- CSS Reset -->\n" +
                "    <style type=\"text/css\">\n" +
                "/* What it does: Remove spaces around the email design added by some email clients. */\n" +
                "      /* Beware: It can remove the padding / margin and add a background color to the compose a reply window. */\n" +
                "html, body {\n" +
                "    margin: 0 !important;\n" +
                "    padding: 0 !important;\n" +
                "    height: 100% !important;\n" +
                "    width: 100% !important;\n" +
                "    font-weight: 100;\n" +
                "}\n" +
                "/* What it does: Stops email clients resizing small text. */\n" +
                "* {\n" +
                "    -ms-text-size-adjust: 100%;\n" +
                "    -webkit-text-size-adjust: 100%;\n" +
                "}\n" +
                "/* What it does: Forces Outlook.com to display emails full width. */\n" +
                ".ExternalClass {\n" +
                "    width: 100%;\n" +
                "}\n" +
                "/* What is does: Centers email on Android 4.4 */\n" +
                "div[style*=\"margin: 16px 0\"] {\n" +
                "    margin: 0 !important;\n" +
                "}\n" +
                "/* What it does: Stops Outlook from adding extra spacing to tables. */\n" +
                "table, td {\n" +
                "    mso-table-lspace: 0pt !important;\n" +
                "    mso-table-rspace: 0pt !important;\n" +
                "}\n" +
                "/* What it does: Fixes webkit padding issue. Fix for Yahoo mail table alignment bug. Applies table-layout to the first 2 tables then removes for anything nested deeper. */\n" +
                "table {\n" +
                "    border-spacing: 0 !important;\n" +
                "    border-collapse: collapse !important;\n" +
                "    table-layout: fixed !important;\n" +
                "    margin: 0 auto !important;\n" +
                "}\n" +
                "table table table {\n" +
                "    table-layout: auto;\n" +
                "}\n" +
                "/* What it does: Uses a better rendering method when resizing images in IE. */\n" +
                "img {\n" +
                "    -ms-interpolation-mode: bicubic;\n" +
                "}\n" +
                "/* What it does: Overrides styles added when Yahoo's auto-senses a link. */\n" +
                ".yshortcuts a {\n" +
                "    border-bottom: none !important;\n" +
                "}\n" +
                "/* What it does: Another work-around for iOS meddling in triggered links. */\n" +
                "a[x-apple-data-detectors] {\n" +
                "    color: inherit !important;\n" +
                "}\n" +
                ".email-header {\n" +
                "    background-color: black\n" +
                "}\n" +
                ".icon {\n" +
                "    margin-top: -60px\n" +
                "}\n" +
                ".white-component {\n" +
                "    padding-top: 10px\n" +
                "}\n" +
                ".label {\n" +
                "    font-family: sans-serif;\n" +
                "    font-size: 8px;\n" +
                "    border-radius: 2px;\n" +
                "    mso-height-rule: exactly;\n" +
                "    line-height: 20px;\n" +
                "    color: #ffffff;\n" +
                "    background-color: black;\n" +
                "    display: inline-block;\n" +
                "    padding: 1px 5px 0px 5px;\n" +
                "    margin-bottom: 5px;\n" +
                "    margin-top: 10px;\n" +
                "}\n" +
                "webversion {\n" +
                "    color: #97999b;\n" +
                "    text-decoration: underline;\n" +
                "    text-align: center;\n" +
                "    font-size: 12px;\n" +
                "    font-family: Gotham, \"Helvetica Neue\", Helvetica, Arial, \"sans-serif\";\n" +
                "}\n" +
                ".email-footer {\n" +
                "    background-color: #2c3338\n" +
                "}\n" +
                "a {\n" +
                "    text-decoration: none\n" +
                "}\n" +
                "</style>\n" +
                "\n" +
                "    <!-- Progressive Enhancements -->\n" +
                "    <style type=\"text/css\">\n" +
                "/* What it does: Hover styles for buttons */\n" +
                ".button-td,  .button-a {\n" +
                "    transition: all 100ms ease-in;\n" +
                "}\n" +
                ".button-td:hover,  .button-a:hover {\n" +
                "    background: #43d220 !important;\n" +
                "}\n" +
                ".partner-logo img {\n" +
                "   /* display: inline-block;*/\n" +
                "\n" +
                "}\n" +
                "\n" +
                "/* Media Queries */\n" +
                "@media screen and (max-width: 600px) {\n" +
                ".icon {\n" +
                "    margin-top: 0px\n" +
                "}\n" +
                ".label {\n" +
                "    margin-left: 0\n" +
                "}\n" +
                ".email-container {\n" +
                "    width: 100% !important;\n" +
                "}\n" +
                "span[class=bodycopy], .bodycopy {\n" +
                "    font-size: 16px !important;\n" +
                "    line-height: 26px !important;\n" +
                "    font-weight: normal !important;\n" +
                "\t\n" +
                "}\n" +
                "  \n" +
                "@media only screen and (max-width: 480px) {\n" +
                "\n" +
                ".full {\n" +
                "\n" +
                "display:block;\n" +
                "direction: rtl;\n" +
                "width:100%;\n" +
                "\n" +
                "\n" +
                "\n" +
                "}\n" +
                "\n" +
                "}\n" +
                "/* What it does: Forces elements to resize to the full width of their container. Useful for resizing images beyond their max-width. */\n" +
                ".fluid,  .fluid-centered {\n" +
                "    max-width: 50px !important;\n" +
                "    height: auto !important;\n" +
                "    margin-left: auto !important;\n" +
                "    margin-right: auto !important;\n" +
                "}\n" +
                "/* And center justify these ones. */\n" +
                ".fluid-centered {\n" +
                "    margin-left: auto !important;\n" +
                "    margin-right: auto !important;\n" +
                "}\n" +
                "/* What it does: Forces table cells into full-width rows. */\n" +
                ".stack-column,  .stack-column-center {\n" +
                "    display: block !important;\n" +
                "    width: 100% !important;\n" +
                "    max-width: 100% !important;\n" +
                "    direction: ltr !important;\n" +
                "}\n" +
                "/* And center justify these ones. */\n" +
                ".stack-column-center {\n" +
                "    text-align: center !important;\n" +
                "}\n" +
                "/* What it does: Generic utility class for centering. Useful for images, buttons, and nested tables. */\n" +
                ".center-on-narrow {\n" +
                "    text-align: center !important;\n" +
                "    display: block !important;\n" +
                "    margin-left: auto !important;\n" +
                "    margin-right: auto !important;\n" +
                "    float: none !important;\n" +
                "}\n" +
                "table.center-on-narrow {\n" +
                "    display: inline-block !important;\n" +
                "}\n" +
                "\t.icontable {\n" +
                "\t\twidth: 100% !important;\n" +
                "\t\t/* margin-bottom: 20px !important; */\n" +
                "\t\t\n" +
                "\t}\t\n" +
                "\tblockquote { \n" +
                "  display: block;\n" +
                "  margin-top: 1em;\n" +
                "  margin-bottom: 1em;\n" +
                "  margin-left: 40px;\n" +
                "  margin-right: 40px;\n" +
                "}\n" +
                "}\n" +
                "      hr {\n" +
                "       border-top: 0px solid #D5DCE5;\n" +
                "        \n" +
                "      }\n" +
                "</style>\n" +
                "    </head>\n" +
                "<body bgcolor=\"#F8F9FC\" width=\"100%\" style=\"margin: 0;\" yahoo=\"yahoo\"><br>\n" +
                "\n" +
                "    \n" +
                "    <table bgcolor=\"#F8F9FC\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" height=\"100%\" width=\"100%\" style=\"border-collapse:collapse;\">\n" +
                "      <tbody><tr>\n" +
                "        <td><center style=\"width: 100%;\" class=\"\">\n" +
                "            \n" +
                "            <!-- Visually Hidden Preheader Text : BEGIN -->\n" +
                "            <div style=\"display:none;font-size:1px;line-height:1px;max-height:0px;max-width:0px;opacity:0;overflow:hidden;mso-hide:all;font-family: sans-serif;\">Welcome to IGLOO Security Cloud!</div>\n" +
                "            <!-- Visually Hidden Preheader Text : END --> \n" +
                "            \n" +
                "         \n" +
                "            \n" +
                "            <!-- Email Body : BEGIN -->\n" +
                "            <table cellspacing=\"0\" cellpadding=\"0\" border=\"0\" align=\"center\" bgcolor=\"#ffffff\" width=\"600\" style='box-shadow: 0 .15rem 1.75rem 0 rgba(58,59,69,.15)!important;     border: 1px solid #e3e6f0;\n" +
                "    border-radius: .35rem;' class=\"email-container\">\n" +
                "\t\t\t  <!-- Header // START -->    \n" +
                "          \t<tbody><tr>\n" +
                "              <td bgcolor=\"#335ACB\" style=\"padding: 10px 0 10px 10px; text-align: left\" class=\"\"><a href=\"http://www.igloosec.co.kr/index.do\" target=\"_blank\" title=\"IGLOO SECURITY\" data-targettype=\"webpage\">\n" +
                "               </a></td>\n" +
                "              \n" +
                "            </tr>\n" +
                "\t\t\t\t\n" +
                "               <!-- Header // END --> \n" +
                "\t\t\t\t<tr>\n" +
                "                        <td dir=\"ltr\" valign=\"top\" style=\"font-family: sans-serif; font-size: 16px; mso-height-rule: exactly;  color: #474444; padding: 20px;0px 0px 20px; text-align: left;\" class=\"bodycopy\"><h1 style=\"line-height: 40px; margin-left:10px;\" class=\"\">Welcome</h1>\n" +
                "                          <span style=\"font-size:11.0pt;  margin-left:10px; font-family:&quot;Arial&quot;,sans-serif;\n" +
                "mso-fareast-font-family:Calibri;mso-fareast-theme-font:minor-latin;mso-ansi-language:\n" +
                "EN-US;mso-fareast-language:EN-US;mso-bidi-language:AR-SA\">Thanks for signing up\n" +
                "for IGLOO SECURITY Cloud! Here is your login information:</span><br>\n" +
                "</td></tr><tr>\n" +
                "<td dir=\"ltr\" valign=\"top\" style=\"font-family: sans-serif; font-size: 16px; mso-height-rule: exactly;  color: #474444; padding: 20px;0px 0px 10px; text-align: left;\" class=\"bodycopy\">\n" +
                "\t<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"550\">\n" +
                "\t\t<tbody><tr>\n" +
                "\t\t\t<td width=\"30%\" valign=\"top\"><strong class=\"\">URL:</strong></td>\n" +
                "\t\t\t<td> <a href=\"https://igloocld.com/ \" style=\"color:#E20082;text-decoration:none;\" data-targettype=\"webpage\">https://igloocld.com/ </a></td>\n" +
                "\t\t</tr>\n" +
                "\t\t<tr><td width=\"30%\" valign=\"top\"><strong class=\"\">User Name:</strong></td>\n" +
                "\t\t\t<td>"+email+"</td></tr>\n" +
                "\t\t\n" +
                "\t\t<tr><td width=\"30%\" valign=\"top\"><strong class=\"\">Password:</strong></td>\n" +
                "\t\t\t<td style=\"overflow-wrap: break-word;\">"+firstPasswd+"</td></tr>\n" +
                "\t\t<tr><td colspan=\"2\" style=\"font-size: 13px\"><br><i>* You’ll be asked to\n" +
                "create a permanent password on first login.</i></td></tr>\n" +
                "\t</tbody></table></td></tr>\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\n" +
                "\n" +
                "            \n" +
                "           \n" +
                "\n" +
                "\n" +
                "\t\t\t\n" +
                "           \t\t\t\n" +
                "            \n" +
                "          </center></td>\n" +
                "      </tr>\n" +
                "    </tbody></table>\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "<!-- Footer -->\n" +
                "</body></html>","text/html; charset=UTF-8");
        //mimeMessage.setText("https://igloocld.com/ 에 접속하셔서, 로그인하시기 바랍니다. \nid: "+ email+ "\n" + "pw: 1234" );
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
        mimeMessage.setSubject("SPiDER TM Login Information");

//        mimeMessage.setContent("<h1 style='font-size: 20px;'><a href='https://"+emailstr+".igloocld.com'>https://"+emailstr +".igloocld.com </a>에 접속하셔서, 로그인하시기 바랍니다.</h1>"+
//                "\n<span style='font-weight: bold;'>ID: </span> <span>  tmadmin<br></span>\n" + "<span style='font-weight: bold;'>pw: </span> <span> 0!password  </span>" , "text/html; charset=UTF-8");


        mimeMessage.setContent("<html><head><title>Welcome to IGLOO Cloud!</title><link rel=\"SHORTCUT ICON\" >\n" +
                "    \n" +
                "<meta name=\"robots\" content=\"noindex, nofollow\">\n" +
                "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n" +
                "    <!-- utf-8 works for most cases -->\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <!-- Forcing initial-scale shouldn't be necessary -->\n" +
                "    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n" +
                "    <!-- Use the latest (edge) version of IE rendering engine -->\n" +
                "      <meta name=\"x-apple-disable-message-reformatting\">\n" +
                "      \n" +
                "    <title>Welcome to IGLOO Security Cloud!</title>\n" +
                "    <!-- The title tag shows in email notifications, like Android 4.4. -->\n" +
                "    <!-- Please use an inliner tool to convert all CSS to inline as inpage or external CSS is removed by email clients -->\n" +
                "    <!-- important in CSS is used to prevent the styles of currently inline CSS from overriding the ones mentioned in media queries when corresponding screen sizes are encountered -->\n" +
                "\n" +
                "    <!-- CSS Reset -->\n" +
                "    <style type=\"text/css\">\n" +
                "/* What it does: Remove spaces around the email design added by some email clients. */\n" +
                "      /* Beware: It can remove the padding / margin and add a background color to the compose a reply window. */\n" +
                "html, body {\n" +
                "    margin: 0 !important;\n" +
                "    padding: 0 !important;\n" +
                "    height: 100% !important;\n" +
                "    width: 100% !important;\n" +
                "    font-weight: 100;\n" +
                "}\n" +
                "/* What it does: Stops email clients resizing small text. */\n" +
                "* {\n" +
                "    -ms-text-size-adjust: 100%;\n" +
                "    -webkit-text-size-adjust: 100%;\n" +
                "}\n" +
                "/* What it does: Forces Outlook.com to display emails full width. */\n" +
                ".ExternalClass {\n" +
                "    width: 100%;\n" +
                "}\n" +
                "/* What is does: Centers email on Android 4.4 */\n" +
                "div[style*=\"margin: 16px 0\"] {\n" +
                "    margin: 0 !important;\n" +
                "}\n" +
                "/* What it does: Stops Outlook from adding extra spacing to tables. */\n" +
                "table, td {\n" +
                "    mso-table-lspace: 0pt !important;\n" +
                "    mso-table-rspace: 0pt !important;\n" +
                "}\n" +
                "/* What it does: Fixes webkit padding issue. Fix for Yahoo mail table alignment bug. Applies table-layout to the first 2 tables then removes for anything nested deeper. */\n" +
                "table {\n" +
                "    border-spacing: 0 !important;\n" +
                "    border-collapse: collapse !important;\n" +
                "    table-layout: fixed !important;\n" +
                "    margin: 0 auto !important;\n" +
                "}\n" +
                "table table table {\n" +
                "    table-layout: auto;\n" +
                "}\n" +
                "/* What it does: Uses a better rendering method when resizing images in IE. */\n" +
                "img {\n" +
                "    -ms-interpolation-mode: bicubic;\n" +
                "}\n" +
                "/* What it does: Overrides styles added when Yahoo's auto-senses a link. */\n" +
                ".yshortcuts a {\n" +
                "    border-bottom: none !important;\n" +
                "}\n" +
                "/* What it does: Another work-around for iOS meddling in triggered links. */\n" +
                "a[x-apple-data-detectors] {\n" +
                "    color: inherit !important;\n" +
                "}\n" +
                ".email-header {\n" +
                "    background-color: black\n" +
                "}\n" +
                ".icon {\n" +
                "    margin-top: -60px\n" +
                "}\n" +
                ".white-component {\n" +
                "    padding-top: 10px\n" +
                "}\n" +
                ".label {\n" +
                "    font-family: sans-serif;\n" +
                "    font-size: 8px;\n" +
                "    border-radius: 2px;\n" +
                "    mso-height-rule: exactly;\n" +
                "    line-height: 20px;\n" +
                "    color: #ffffff;\n" +
                "    background-color: black;\n" +
                "    display: inline-block;\n" +
                "    padding: 1px 5px 0px 5px;\n" +
                "    margin-bottom: 5px;\n" +
                "    margin-top: 10px;\n" +
                "}\n" +
                "webversion {\n" +
                "    color: #97999b;\n" +
                "    text-decoration: underline;\n" +
                "    text-align: center;\n" +
                "    font-size: 12px;\n" +
                "    font-family: Gotham, \"Helvetica Neue\", Helvetica, Arial, \"sans-serif\";\n" +
                "}\n" +
                ".email-footer {\n" +
                "    background-color: #2c3338\n" +
                "}\n" +
                "a {\n" +
                "    text-decoration: none\n" +
                "}\n" +
                "</style>\n" +
                "\n" +
                "    <!-- Progressive Enhancements -->\n" +
                "    <style type=\"text/css\">\n" +
                "/* What it does: Hover styles for buttons */\n" +
                ".button-td,  .button-a {\n" +
                "    transition: all 100ms ease-in;\n" +
                "}\n" +
                ".button-td:hover,  .button-a:hover {\n" +
                "    background: #43d220 !important;\n" +
                "}\n" +
                ".partner-logo img {\n" +
                "   /* display: inline-block;*/\n" +
                "\n" +
                "}\n" +
                "\n" +
                "/* Media Queries */\n" +
                "@media screen and (max-width: 600px) {\n" +
                ".icon {\n" +
                "    margin-top: 0px\n" +
                "}\n" +
                ".label {\n" +
                "    margin-left: 0\n" +
                "}\n" +
                ".email-container {\n" +
                "    width: 100% !important;\n" +
                "}\n" +
                "span[class=bodycopy], .bodycopy {\n" +
                "    font-size: 16px !important;\n" +
                "    line-height: 26px !important;\n" +
                "    font-weight: normal !important;\n" +
                "\t\n" +
                "}\n" +
                "  \n" +
                "@media only screen and (max-width: 480px) {\n" +
                "\n" +
                ".full {\n" +
                "\n" +
                "display:block;\n" +
                "direction: rtl;\n" +
                "width:100%;\n" +
                "\n" +
                "\n" +
                "\n" +
                "}\n" +
                "\n" +
                "}\n" +
                "/* What it does: Forces elements to resize to the full width of their container. Useful for resizing images beyond their max-width. */\n" +
                ".fluid,  .fluid-centered {\n" +
                "    max-width: 50px !important;\n" +
                "    height: auto !important;\n" +
                "    margin-left: auto !important;\n" +
                "    margin-right: auto !important;\n" +
                "}\n" +
                "/* And center justify these ones. */\n" +
                ".fluid-centered {\n" +
                "    margin-left: auto !important;\n" +
                "    margin-right: auto !important;\n" +
                "}\n" +
                "/* What it does: Forces table cells into full-width rows. */\n" +
                ".stack-column,  .stack-column-center {\n" +
                "    display: block !important;\n" +
                "    width: 100% !important;\n" +
                "    max-width: 100% !important;\n" +
                "    direction: ltr !important;\n" +
                "}\n" +
                "/* And center justify these ones. */\n" +
                ".stack-column-center {\n" +
                "    text-align: center !important;\n" +
                "}\n" +
                "/* What it does: Generic utility class for centering. Useful for images, buttons, and nested tables. */\n" +
                ".center-on-narrow {\n" +
                "    text-align: center !important;\n" +
                "    display: block !important;\n" +
                "    margin-left: auto !important;\n" +
                "    margin-right: auto !important;\n" +
                "    float: none !important;\n" +
                "}\n" +
                "table.center-on-narrow {\n" +
                "    display: inline-block !important;\n" +
                "}\n" +
                "\t.icontable {\n" +
                "\t\twidth: 100% !important;\n" +
                "\t\t/* margin-bottom: 20px !important; */\n" +
                "\t\t\n" +
                "\t}\t\n" +
                "\tblockquote { \n" +
                "  display: block;\n" +
                "  margin-top: 1em;\n" +
                "  margin-bottom: 1em;\n" +
                "  margin-left: 40px;\n" +
                "  margin-right: 40px;\n" +
                "}\n" +
                "}\n" +
                "      hr {\n" +
                "       border-top: 0px solid #D5DCE5;\n" +
                "        \n" +
                "      }\n" +
                "</style>\n" +
                "    </head>\n" +
                "<body bgcolor=\"#F8F9FC\" width=\"100%\" style=\"margin: 0;\" yahoo=\"yahoo\"><br>\n" +
                "\n" +
                "    \n" +
                "    <table bgcolor=\"#F8F9FC\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" height=\"100%\" width=\"100%\" style=\"border-collapse:collapse;\">\n" +
                "      <tbody><tr>\n" +
                "        <td><center style=\"width: 100%;\" class=\"\">\n" +
                "            \n" +
                "            <!-- Visually Hidden Preheader Text : BEGIN -->\n" +
                "            <div style=\"display:none;font-size:1px;line-height:1px;max-height:0px;max-width:0px;opacity:0;overflow:hidden;mso-hide:all;font-family: sans-serif;\">Welcome to IGLOO Security Cloud!</div>\n" +
                "            <!-- Visually Hidden Preheader Text : END --> \n" +
                "            \n" +
                "         \n" +
                "            \n" +
                "            <!-- Email Body : BEGIN -->\n" +
                "            <table cellspacing=\"0\" cellpadding=\"0\" border=\"0\" align=\"center\" bgcolor=\"#ffffff\" width=\"600\" style='box-shadow: 0 .15rem 1.75rem 0 rgba(58,59,69,.15)!important;     border: 1px solid #e3e6f0;\n" +
                "    border-radius: .35rem;' class=\"email-container\">\n" +
                "\t\t\t  <!-- Header // START -->    \n" +
                "          \t<tbody><tr>\n" +
                "              <td bgcolor=\"#335ACB\" style=\"padding: 10px 0 10px 10px; text-align: left\" class=\"\"><a href=\"http://www.igloosec.co.kr/index.do\" target=\"_blank\" title=\"IGLOO SECURITY\" data-targettype=\"webpage\">\n" +
                "               </a></td>\n" +
                "              \n" +
                "            </tr>\n" +
                "\t\t\t\t\n" +
                "               <!-- Header // END --> \n" +
                "\t\t\t\t<tr>\n" +
                "                        <td dir=\"ltr\" valign=\"top\" style=\"font-family: sans-serif; font-size: 16px; mso-height-rule: exactly;  color: #474444; padding: 20px;0px 0px 20px; text-align: left;\" class=\"bodycopy\"><h1 style=\"line-height: 40px;  margin-left:10px;\" class=\"\">SPiDER TM Login Info.</h1>\n" +
                "</td></tr><tr>\n" +
                "<td dir=\"ltr\" valign=\"top\" style=\"font-family: sans-serif; font-size: 16px; mso-height-rule: exactly;  color: #474444; padding: 20px;0px 0px 10px; text-align: left;\" class=\"bodycopy\">\n" +
                "\t<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"550\">\n" +
                "\t\t<tbody><tr>\n" +
                "\t\t\t<td width=\"30%\" valign=\"top\"><strong class=\"\">TM URL:</strong></td>\n" +
                "\t\t\t<td> <a href='https://"+emailstr+".igloocld.com' style=\"color:#E20082;text-decoration:none;\" data-targettype=\"webpage\">https://"+emailstr +".igloocld.com  </a></td>\n" +
                "\t\t</tr>\n" +
                "\t\t<tr><td width=\"30%\" valign=\"top\"><strong class=\"\">User Name:</strong></td>\n" +
                "\t\t\t<td> tmadmin</td></tr>\n" +
                "\t\t\n" +
                "\t\t<tr><td width=\"30%\" valign=\"top\"><strong class=\"\">Password:</strong></td>\n" +
                "\t\t\t<td style=\"overflow-wrap: break-word;\"> 0!password</td></tr>\n" +
                "\t\t<tr><td colspan=\"2\" style=\"font-size: 13px\"><br><i>* You’ll be asked to\n" +
                "create a permanent password on first login.</i></td></tr>\n" +
                "\t</tbody></table></td></tr>\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\n" +
                "\n" +
                "            \n" +
                "           \n" +
                "\n" +
                "\n" +
                "\t\t\t\n" +
                "           \t\t\t\n" +
                "            \n" +
                "          </center></td>\n" +
                "      </tr>\n" +
                "    </tbody></table>\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "<!-- Footer -->\n" +
                "</body></html>","text/html; charset=UTF-8");
        //mimeMessage.setText("https://"+emailstr+".igloocld.com/ 에 접속하셔서, 로그인하시기 바랍니다. \nid : igloosec \n" + "pw : sp!dertm40" );

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

            System.out.println(result.getPathStr()+"/"+result.getShellcom()+" "+result.getEmailparam()+" "+result.getVmseries()+" "+emailStr + " "
                    + result.getCountry() + " " + result.getProduct() + " " + result.getVendor());
            //System.out.println(result.getPathStr());
        } else {
            //Runtime.getRuntime().exec().
            process = Runtime.getRuntime()
                    .exec(result.getPathStr()+"/"+result.getShellcom()+" "+result.getEmailparam()+" "+result.getVmseries()+" "+emailStr + " "
                            + result.getCountry() + " " + result.getProduct() + " " + result.getVendor());
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
    private static String GOOGLE_URL = "https://chart.googleapis.com/chart?chs=100x100&chld=M|0&cht=qr&chl=";

    // Security Key 생성
    public static String generateSecretKey() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        Base32 base32 = new Base32();
        return base32.encodeToString(bytes);
    }

    public static String firstPassword() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[10];
        random.nextBytes(bytes);
        Base32 base32 = new Base32();
        return base32.encodeToString(bytes);
    }

    // barcode 생성
    public static String getGoogleAuthenticatorBarCode(String secretKey, String issuer) {
        try {//igloosec          //igloosec
            return GOOGLE_URL+"otpauth://totp/"
                    + URLEncoder.encode(issuer , "UTF-8").replace("+", "%20")
                    + "?secret=" + URLEncoder.encode(secretKey, "UTF-8").replace("+", "%20")
                    + "&issuer=" + URLEncoder.encode(issuer, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    // 기존에 요청했던 고객이 재요청 한 경우
    public void sendMailRepeatRequest(String email) throws MessagingException {
        Session session = setMail();
        Message mimeMessage = new MimeMessage(session);
        mimeMessage.setFrom(new InternetAddress(MAIL_FROM));
        mimeMessage.setRecipient(Message.RecipientType.TO,
                new InternetAddress(email));
        mimeMessage.setSubject("Your email has already been registered.");
        mimeMessage.setContent("<html><head><title>Welcome to IGLOO Cloud!</title><link rel=\"SHORTCUT ICON\" >\n" +
                "    \n" +
                "<meta name=\"robots\" content=\"noindex, nofollow\">\n" +
                "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n" +
                "    <!-- utf-8 works for most cases -->\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <!-- Forcing initial-scale shouldn't be necessary -->\n" +
                "    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n" +
                "    <!-- Use the latest (edge) version of IE rendering engine -->\n" +
                "      <meta name=\"x-apple-disable-message-reformatting\">\n" +
                "      \n" +
                "    <title>Welcome to IGLOO Security Cloud!</title>\n" +
                "    <!-- The title tag shows in email notifications, like Android 4.4. -->\n" +
                "    <!-- Please use an inliner tool to convert all CSS to inline as inpage or external CSS is removed by email clients -->\n" +
                "    <!-- important in CSS is used to prevent the styles of currently inline CSS from overriding the ones mentioned in media queries when corresponding screen sizes are encountered -->\n" +
                "\n" +
                "    <!-- CSS Reset -->\n" +
                "    <style type=\"text/css\">\n" +
                "/* What it does: Remove spaces around the email design added by some email clients. */\n" +
                "      /* Beware: It can remove the padding / margin and add a background color to the compose a reply window. */\n" +
                "html, body {\n" +
                "    margin: 0 !important;\n" +
                "    padding: 0 !important;\n" +
                "    height: 100% !important;\n" +
                "    width: 100% !important;\n" +
                "    font-weight: 100;\n" +
                "}\n" +
                "/* What it does: Stops email clients resizing small text. */\n" +
                "* {\n" +
                "    -ms-text-size-adjust: 100%;\n" +
                "    -webkit-text-size-adjust: 100%;\n" +
                "}\n" +
                "/* What it does: Forces Outlook.com to display emails full width. */\n" +
                ".ExternalClass {\n" +
                "    width: 100%;\n" +
                "}\n" +
                "/* What is does: Centers email on Android 4.4 */\n" +
                "div[style*=\"margin: 16px 0\"] {\n" +
                "    margin: 0 !important;\n" +
                "}\n" +
                "/* What it does: Stops Outlook from adding extra spacing to tables. */\n" +
                "table, td {\n" +
                "    mso-table-lspace: 0pt !important;\n" +
                "    mso-table-rspace: 0pt !important;\n" +
                "}\n" +
                "/* What it does: Fixes webkit padding issue. Fix for Yahoo mail table alignment bug. Applies table-layout to the first 2 tables then removes for anything nested deeper. */\n" +
                "table {\n" +
                "    border-spacing: 0 !important;\n" +
                "    border-collapse: collapse !important;\n" +
                "    table-layout: fixed !important;\n" +
                "    margin: 0 auto !important;\n" +
                "}\n" +
                "table table table {\n" +
                "    table-layout: auto;\n" +
                "}\n" +
                "/* What it does: Uses a better rendering method when resizing images in IE. */\n" +
                "img {\n" +
                "    -ms-interpolation-mode: bicubic;\n" +
                "}\n" +
                "/* What it does: Overrides styles added when Yahoo's auto-senses a link. */\n" +
                ".yshortcuts a {\n" +
                "    border-bottom: none !important;\n" +
                "}\n" +
                "/* What it does: Another work-around for iOS meddling in triggered links. */\n" +
                "a[x-apple-data-detectors] {\n" +
                "    color: inherit !important;\n" +
                "}\n" +
                ".email-header {\n" +
                "    background-color: black\n" +
                "}\n" +
                ".icon {\n" +
                "    margin-top: -60px\n" +
                "}\n" +
                ".white-component {\n" +
                "    padding-top: 10px\n" +
                "}\n" +
                ".label {\n" +
                "    font-family: sans-serif;\n" +
                "    font-size: 8px;\n" +
                "    border-radius: 2px;\n" +
                "    mso-height-rule: exactly;\n" +
                "    line-height: 20px;\n" +
                "    color: #ffffff;\n" +
                "    background-color: black;\n" +
                "    display: inline-block;\n" +
                "    padding: 1px 5px 0px 5px;\n" +
                "    margin-bottom: 5px;\n" +
                "    margin-top: 10px;\n" +
                "}\n" +
                "webversion {\n" +
                "    color: #97999b;\n" +
                "    text-decoration: underline;\n" +
                "    text-align: center;\n" +
                "    font-size: 12px;\n" +
                "    font-family: Gotham, \"Helvetica Neue\", Helvetica, Arial, \"sans-serif\";\n" +
                "}\n" +
                ".email-footer {\n" +
                "    background-color: #2c3338\n" +
                "}\n" +
                "a {\n" +
                "    text-decoration: none\n" +
                "}\n" +
                "</style>\n" +
                "\n" +
                "    <!-- Progressive Enhancements -->\n" +
                "    <style type=\"text/css\">\n" +
                "/* What it does: Hover styles for buttons */\n" +
                ".button-td,  .button-a {\n" +
                "    transition: all 100ms ease-in;\n" +
                "}\n" +
                ".button-td:hover,  .button-a:hover {\n" +
                "    background: #43d220 !important;\n" +
                "}\n" +
                ".partner-logo img {\n" +
                "   /* display: inline-block;*/\n" +
                "\n" +
                "}\n" +
                "\n" +
                "/* Media Queries */\n" +
                "@media screen and (max-width: 600px) {\n" +
                ".icon {\n" +
                "    margin-top: 0px\n" +
                "}\n" +
                ".label {\n" +
                "    margin-left: 0\n" +
                "}\n" +
                ".email-container {\n" +
                "    width: 100% !important;\n" +
                "}\n" +
                "span[class=bodycopy], .bodycopy {\n" +
                "    font-size: 16px !important;\n" +
                "    line-height: 26px !important;\n" +
                "    font-weight: normal !important;\n" +
                "\t\n" +
                "}\n" +
                "  \n" +
                "@media only screen and (max-width: 480px) {\n" +
                "\n" +
                ".full {\n" +
                "\n" +
                "display:block;\n" +
                "direction: rtl;\n" +
                "width:100%;\n" +
                "\n" +
                "\n" +
                "\n" +
                "}\n" +
                "\n" +
                "}\n" +
                "/* What it does: Forces elements to resize to the full width of their container. Useful for resizing images beyond their max-width. */\n" +
                ".fluid,  .fluid-centered {\n" +
                "    max-width: 50px !important;\n" +
                "    height: auto !important;\n" +
                "    margin-left: auto !important;\n" +
                "    margin-right: auto !important;\n" +
                "}\n" +
                "/* And center justify these ones. */\n" +
                ".fluid-centered {\n" +
                "    margin-left: auto !important;\n" +
                "    margin-right: auto !important;\n" +
                "}\n" +
                "/* What it does: Forces table cells into full-width rows. */\n" +
                ".stack-column,  .stack-column-center {\n" +
                "    display: block !important;\n" +
                "    width: 100% !important;\n" +
                "    max-width: 100% !important;\n" +
                "    direction: ltr !important;\n" +
                "}\n" +
                "/* And center justify these ones. */\n" +
                ".stack-column-center {\n" +
                "    text-align: center !important;\n" +
                "}\n" +
                "/* What it does: Generic utility class for centering. Useful for images, buttons, and nested tables. */\n" +
                ".center-on-narrow {\n" +
                "    text-align: center !important;\n" +
                "    display: block !important;\n" +
                "    margin-left: auto !important;\n" +
                "    margin-right: auto !important;\n" +
                "    float: none !important;\n" +
                "}\n" +
                "table.center-on-narrow {\n" +
                "    display: inline-block !important;\n" +
                "}\n" +
                "\t.icontable {\n" +
                "\t\twidth: 100% !important;\n" +
                "\t\t/* margin-bottom: 20px !important; */\n" +
                "\t\t\n" +
                "\t}\t\n" +
                "\tblockquote { \n" +
                "  display: block;\n" +
                "  margin-top: 1em;\n" +
                "  margin-bottom: 1em;\n" +
                "  margin-left: 40px;\n" +
                "  margin-right: 40px;\n" +
                "}\n" +
                "}\n" +
                "      hr {\n" +
                "       border-top: 0px solid #D5DCE5;\n" +
                "        \n" +
                "      }\n" +
                "</style>\n" +
                "    </head>\n" +
                "<body bgcolor=\"#F8F9FC\" width=\"100%\" style=\"margin: 0;\" yahoo=\"yahoo\"><br>\n" +
                "\n" +
                "    \n" +
                "    <table bgcolor=\"#F8F9FC\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" height=\"100%\" width=\"100%\" style=\"border-collapse:collapse;\">\n" +
                "      <tbody><tr>\n" +
                "        <td><center style=\"width: 100%;\" class=\"\">\n" +
                "            \n" +
                "            <!-- Visually Hidden Preheader Text : BEGIN -->\n" +
                "            <div style=\"display:none;font-size:1px;line-height:1px;max-height:0px;max-width:0px;opacity:0;overflow:hidden;mso-hide:all;font-family: sans-serif;\">Welcome to IGLOO Security Cloud!</div>\n" +
                "            <!-- Visually Hidden Preheader Text : END --> \n" +
                "            \n" +
                "         \n" +
                "            \n" +
                "            <!-- Email Body : BEGIN -->\n" +
                "            <table cellspacing=\"0\" cellpadding=\"0\" border=\"0\" align=\"center\" bgcolor=\"#ffffff\" width=\"600\" style='box-shadow: 0 .15rem 1.75rem 0 rgba(58,59,69,.15)!important;     border: 1px solid #e3e6f0;\n" +
                "    border-radius: .35rem;' class=\"email-container\">\n" +
                "\t\t\t  <!-- Header // START -->    \n" +
                "          \t<tbody><tr>\n" +
                "              <td bgcolor=\"#335ACB\" style=\"padding: 10px 0 10px 10px; text-align: left\" class=\"\"><a href=\"http://www.igloosec.co.kr/index.do\" target=\"_blank\" title=\"IGLOO SECURITY\" data-targettype=\"webpage\">\n" +
                "               </a></td>\n" +
                "              \n" +
                "            </tr>\n" +
                "\t\t\t\t\n" +
                "               <!-- Header // END --> \n" +
                "\t\t\t\t<tr>\n" +
                "                        <td dir=\"ltr\" valign=\"top\" style=\"font-family: sans-serif; font-size: 16px; mso-height-rule: exactly;  color: #474444; padding: 20px;0px 0px 20px; text-align: left;\" class=\"bodycopy\"><h1 style=\"line-height: 40px; margin-left:10px;\" class=\"\">Welcome</h1>\n" +
                "                          <span style=\"font-size:11.0pt;  margin-left:10px; font-family:&quot;Arial&quot;,sans-serif;\n" +
                "mso-fareast-font-family:Calibri;mso-fareast-theme-font:minor-latin;mso-ansi-language:\n" +
                "EN-US;mso-fareast-language:EN-US;mso-bidi-language:AR-SA\">\n" +
                "Your email has already been registered.</span><br>\n" +
                "</td></tr><tr>\n" +
                "<td dir=\"ltr\" valign=\"top\" style=\"font-family: sans-serif; font-size: 16px; mso-height-rule: exactly;  color: #474444; padding: 20px;0px 0px 10px; text-align: left;\" class=\"bodycopy\">\n" +
                "\t<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"550\">\n" +
                "\t\t<tbody><tr>\n" +
                "\t\t\t<td width=\"30%\" valign=\"top\"><strong class=\"\">URL:</strong></td>\n" +
                "\t\t\t<td> <a href=\"https://igloocld.com/ \" style=\"color:#E20082;text-decoration:none;\" data-targettype=\"webpage\">https://igloocld.com/ </a></td>\n" +
                "\t\t<br></tr>\n" +
                "\t</tbody></table></td></tr>\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\n" +
                "\n" +
                "            \n" +
                "           \n" +
                "\n" +
                "\n" +
                "\t\t\t\n" +
                "           \t\t\t\n" +
                "            \n" +
                "          </center></td>\n" +
                "      </tr>\n" +
                "    </tbody></table>\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "<!-- Footer -->\n" +
                "</body></html>","text/html; charset=UTF-8");

        Transport transport = session.getTransport();

        transport.connect();

        transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
        transport.close();
    }


    public void sendMailCustomerInflux(String email) throws MessagingException{
        Session session = setMail();
        Message mimeMessage = new MimeMessage(session);
        mimeMessage.setFrom(new InternetAddress(MAIL_FROM));
        mimeMessage.setRecipient(Message.RecipientType.TO,
                new InternetAddress("cloud@igloosec.com"));
        mimeMessage.setSubject("#고객 유입 알림#");
        mimeMessage.setText(email + " 계정이 고객정보를 등록하였습니다.");

        Transport transport = session.getTransport();

        transport.connect();

        transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
        transport.close();
    }

    private Session setMail() {
        Properties props = System.getProperties();

        props.put("mail.smtp.host", MAIL_HOST);
        props.put("mail.smtp.port", MAIL_PORT);
        props.put("mail.smtp.from", MAIL_FROM);
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", true);
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.trust", MAIL_HOST);

        Session session = Session.getInstance(props, new MyAuthentication());
        session.setDebug(false); //for debug

        return session;
    }

}