package com.yonga.auc.config;


import java.util.Map;

import com.yonga.auc.data.customer.Customer;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
class WelcomeController {

    @GetMapping("/")
    public String welcome(Map<String, Object> model) {
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
