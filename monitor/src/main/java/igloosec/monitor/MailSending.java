package igloosec.monitor;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class MailSending {

    private static final String MAIL_HOST = "outlook.office365.com";
    private static final int MAIL_PORT = 587;
    private static final String MAIL_FROM = "igloocld@igloosec.com";

    /*
    public static void main(String[] args) {
        MailSending sending = new MailSending();

        try {
            sending.sendMailCriticalValueExceeded("yongwoon.lee@igloosec.com", 70, 20);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
    */

    // critical value exceeded
    public void sendMailCriticalValueExceeded(String email, double currentDiskSize, double criticalValue) throws MessagingException {
        Session session = setMail();
        Message mimeMessage = new MimeMessage(session);
        mimeMessage.setFrom(new InternetAddress(MAIL_FROM));
        mimeMessage.setRecipient(Message.RecipientType.TO,
                new InternetAddress(email));
        mimeMessage.setSubject("Disk usage threshold exceeded");
        String title = "Disk usage threshold exceeded";
        String body = "<tr>\n" +
                "\t\t\t<td colspan='2' valign=\"top\"><strong class=\"\">" +
                "The current disk threshold has exceeded the value you set.</strong></td>\n" +
                "\t\t</tr>\n" +
                "\t\t<tr><td>&nbsp;</td></tr>\n" +
                "\t\t<tr><td width=\"30%\" valign=\"top\"><strong class=\"\">Current disk size:</strong></td>\n" +
                "\t\t\t<td> " + currentDiskSize + "%</td></tr>\n" +
                "\t\t\n" +
                "\t\t<tr><td width=\"30%\" valign=\"top\"><strong class=\"\">set threshold:</strong></td>\n" +
                "\t\t\t<td> " + criticalValue + "%</td></tr>\n";

        String content = setContent(title, body);
        System.out.println(content);
        mimeMessage.setContent(content,"text/html; charset=UTF-8");

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

    private String setContent(String title, String body) {
        return "<html><head><title>"+title+"</title><link rel=\"SHORTCUT ICON\" >\n" +
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
                "                        <td dir=\"ltr\" valign=\"top\" style=\"font-family: sans-serif; font-size: 16px; mso-height-rule: exactly;  color: #474444; padding: 20px;0px 0px 20px; text-align: left;\" class=\"bodycopy\"><h1 style=\"line-height: 40px;  margin-left:10px;\" class=\"\">"+title+"</h1>\n" +
                "</td></tr><tr>\n" +
                "<td dir=\"ltr\" valign=\"top\" style=\"font-family: sans-serif; font-size: 16px; mso-height-rule: exactly;  color: #474444; padding: 20px;0px 0px 10px; text-align: left;\" class=\"bodycopy\">\n" +
                "\t<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"550\">\n" +
                "\t\t<tbody>" + body +
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
                "</body></html>";
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
}
