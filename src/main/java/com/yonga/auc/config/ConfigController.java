package com.yonga.auc.config;

import com.yonga.auc.common.YongaUtil;
import com.yonga.auc.data.category.Category;
import com.yonga.auc.data.category.CategoryService;
import com.yonga.auc.data.log.LogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@Slf4j
@Controller
class ConfigController {

	@Autowired
	private ConfigService configService;
	@Autowired
	private CategoryService categoryService;

	@GetMapping("/config")
	public String log(Map<String, Object> model) {
		model.put("executorStatus", this.configService.getConfigValue("EXECUTOR", "STATUS"));
		model.put("executorMessage", this.configService.getConfigValue("EXECUTOR", "MESSAGE"));

		List<Category> categoryList = this.categoryService.findNotCompleteCategory();
		model.put("categoryList", categoryList);
		return "config/config";
	}

	@PostMapping("/config")
	public ResponseEntity setConfig(@RequestBody Map<String, Object> model) {
		log.debug("set config with model {}", model);
		String title = (String) model.get("title");
		String welcome = (String) model.get("welcome");
		if (YongaUtil.isEmpty(title) || YongaUtil.isEmpty(welcome)) {
			return ResponseEntity.badRequest().build();
		}
		this.configService.setConfigValue("CONFIG", "TITLE", title);
		this.configService.setConfigValue("CONFIG", "WELCOME", welcome);
		ConfigConstants.APPLICATION_TITLE = title;
		ConfigConstants.APPLICATION_WELCOME = welcome;
		return ResponseEntity.ok().build();
	}
}