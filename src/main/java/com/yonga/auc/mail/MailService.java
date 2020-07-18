package com.yonga.auc.mail;

import com.yonga.auc.config.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Date;
import java.util.Properties;
import java.util.stream.Collectors;

@Service
public class MailService {

    private static final String MAIL_CONTENT_TYPE = "text/html; charset=UTF-8";
    @Value("${mail.iconurl}")
    private String iconUrl;
    @Value("${mail.body.template}")
    private String contentTemplate;
    @Value("${mail.content.line}")
    private String contentLine;
    @Value("${mail.body.line}")
    private String contentBodyLine;
    @Autowired
    private ConfigService configService;

    public void sendEmailSync(MailContents mailContents, String toAddress) throws Exception {
        _sendEmail(mailContents, toAddress);
    }
    public void sendEmail(MailContents mailContents, String toAddress) throws Exception {
        _sendEmail(mailContents, toAddress);
    }
    private void _sendEmail(MailContents mailContents, String toAddress) throws Exception {
        String contents = new String(contentTemplate);
        contents = contents.replaceAll("#iconurl#", this.iconUrl);
        contents = contents.replaceAll("#title#", mailContents.getTitle());
        contents = contents.replaceAll("#subTitle#", mailContents.getSubTitle());
        String contentsPrefix = mailContents.getContentsPrefix().stream()
                .map(c -> contentBodyLine.replaceAll("#line#", c))
                .collect(Collectors.joining());
        String contentsPostfix = mailContents.getContentsPostfix().stream()
                .map(c -> contentBodyLine.replaceAll("#line#", c))
                .collect(Collectors.joining());
        String contentBody = mailContents.getContents().stream()
                .map(c -> contentBodyLine.replaceAll("#line#", c))
                .collect(Collectors.joining());
        contents = contents.replaceAll("#contentprefix#", contentsPrefix);
        contents = contents.replaceAll("#contentpostfix#", contentsPostfix);
        contents = contents.replaceAll("#contentbody#", contentBody);

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", configService.getConfigValue("CONFIG", "MAIL_HOST"));
        props.put("mail.smtp.port", configService.getConfigValue("CONFIG", "MAIL_PORT"));


        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(configService.getConfigValue("CONFIG", "MAIL_ID"), configService.getConfigValue("CONFIG", "MAIL_PASSWORD"));
            }
        });
        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(configService.getConfigValue("CONFIG", "MAIL_ID"), false));

        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toAddress));
        msg.setSubject(mailContents.getTitle());
        msg.setContent(mailContents.getTitle(), MAIL_CONTENT_TYPE);
        msg.setSentDate(new Date());

        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent(contents, MAIL_CONTENT_TYPE);

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);
//        MimeBodyPart attachPart = new MimeBodyPart()
//
//        attachPart.attachFile("/Users/star16m/project/yonga/src/main/resources/static/resources/images/403.png");
//        multipart.addBodyPart(attachPart);
        msg.setContent(multipart);
        Transport.send(msg);
    }
}
