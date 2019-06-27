package com.yonga.auc.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Controller
class WelcomeController {

    @Autowired
    private ConfigService configService;
    @GetMapping("/")
    public String welcome(Map<String, Object> model) {
        model.put("status", configService.getConfigValue("EXECUTOR", "STATUS"));
        return "welcome";
    }
    @GetMapping("/login")
    public String login(Map<String, Object> model) {
        return "login";
    }

    @GetMapping("modifyprofile")
    public String modifyProfile(Map<String, Object> model, HttpServletRequest request) {
        String userId = request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : null;
        if (userId == null || userId.isEmpty()) {
            return "redirect:/customer/NEW";
        }
        return "redirect:/customer/" + userId;
    }
}
