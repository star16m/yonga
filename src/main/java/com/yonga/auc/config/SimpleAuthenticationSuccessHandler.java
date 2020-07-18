package com.yonga.auc.config;

import com.yonga.auc.data.log.LogService;
import com.yonga.auc.mail.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class SimpleAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private LogService logService;
    @Autowired
    private MailService mailService;
    @Autowired
    private ConfigService configService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication auth) throws IOException, ServletException {
        super.onAuthenticationSuccess(request, response, auth);
        if (auth != null && !auth.getName().equals("star16m")) {
            logService.addLog("유저 [" + auth.getName() + "] 이 로그인 하였습니다.");
        }
//
//        try {
//            String adminEmail = configService.getConfigValue("CONFIG", "ADMIN_EMAIL");
//            if (YongaUtil.isNotEmpty(adminEmail)) {
//                mailService.sendEmail(new MailContents("[로그인 알림]", "로그인 하였습니다.",
//                                List.of("안녕하세요.", "유저 [" + auth.getName() + "] 이 로그인 하였습니다."),
//                                List.of("확인해 주세요."),
//                                List.of("[유저 정보]", "ID : " + auth.getName(), "description: " + auth.getDetails())),
//                        adminEmail);
//            }
//        } catch (Exception e) {
//            logService.addLog("메일 발송 중 에러가 발생하였습니다. error [" + e.getMessage() + "]");
//        }
    }

}
