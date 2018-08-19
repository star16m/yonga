package com.yonga.auc.config;


import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
class WelcomeController {

    @GetMapping("/")
    public String welcome(Map<String, Object> model) {
        return "welcome";
    }
}
