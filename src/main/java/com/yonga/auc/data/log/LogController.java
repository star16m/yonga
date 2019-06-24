package com.yonga.auc.data.log;

import com.yonga.auc.config.ConfigService;
import com.yonga.auc.data.category.Category;
import com.yonga.auc.data.category.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Controller
class LogController {

	@Autowired
	private LogService logService;
	@Autowired
	private ConfigService configService;
	@Autowired
	private CategoryService categoryService;

	@GetMapping("/log")
	public String log(Map<String, Object> model) {
		model.put("logList", this.logService.getAllLogList());
		model.put("executorStatus", this.configService.getConfigValue("EXECUTOR", "STATUS"));
		model.put("executorMessage", this.configService.getConfigValue("EXECUTOR", "MESSAGE"));

		Integer kaisaiKaisu = 0;
		List<Category> categoryList = this.categoryService.findAll(null).stream().filter(c -> {
			return !c.getStatus().equals("COMPLETE");
		}).collect(Collectors.toList());
		model.put("categoryList", categoryList);
		return "log/log";
	}
}