package com.yonga.auc.data.log;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@Slf4j
@Controller
class LogController {

	@Autowired
	private LogService logService;

	@GetMapping("/log")
	public String log(Map<String, Object> model) {
		model.put("logList", this.logService.getAllLogList());
		return "log/log";
	}
}