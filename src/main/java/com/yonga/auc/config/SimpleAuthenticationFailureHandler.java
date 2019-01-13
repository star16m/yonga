package com.yonga.auc.config;

import com.sun.tools.javac.util.List;
import com.yonga.auc.common.YongaUtil;
import com.yonga.auc.data.log.LogService;
import com.yonga.auc.mail.MailContents;
import com.yonga.auc.mail.MailService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class SimpleAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    public SimpleAuthenticationFailureHandler() {
        super("/login");
    }
    @Autowired
    private LogService logService;
    @Autowired
    private ConfigService configService;
    @Autowired
    private MailService mailService;
    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        super.onAuthenticationFailure(request, response, exception);
        String userId = StringUtils.defaultString(request.getParameter("userId"));
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = request.getRemoteAddr();
        }
        logService.addLog("유저 [" + userId + "] 로그인에 실패 하였습니다. ip[" + ipAddress + "]");
        if (exception.getCause() != null && exception.getCause() instanceof DisabledException) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.setAttribute("CUSTOMER_DISABLED", exception);
            }
        } else {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.removeAttribute("CUSTOMER_DISABLED");
            }
        }
//        try {
//            String adminEmail = configService.getConfigValue("CONFIG", "ADMIN_EMAIL");
//            if (YongaUtil.isNotEmpty(adminEmail)) {
//                mailService.sendEmail(new MailContents("[로그인 알림]", "로그인 실패 알림",
//                                List.of("안녕하세요.", "유저 [" + userId + "] 이 로그인 실패하였습니다."),
//                                List.of("확인해 주세요."),
//                                List.of("[유저 정보]", "ID : " + userId, "ip-address: " + ipAddress)),
//                        adminEmail);
//            }
//        } catch (Exception e) {
//            logService.addLog("메일 발송 중 에러가 발생하였습니다. error [" + e.getMessage() + "]");
//        }
    }
}
