package com.yonga.auc.config;

import com.sun.mail.util.MailConnectException;
import com.yonga.auc.common.YongaUtil;
import com.yonga.auc.data.category.Category;
import com.yonga.auc.data.category.CategoryService;
import com.yonga.auc.data.log.LogService;
import com.yonga.auc.mail.MailContents;
import com.yonga.auc.mail.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.mail.AuthenticationFailedException;
import java.util.Arrays;
import java.util.Map;

@Slf4j
@Controller
class ConfigController {

	@Autowired
	private ConfigService configService;
	@Autowired
	private CategoryService categoryService;
	@Autowired
	private MailService mailService;
	@Autowired
	private LogService logService;

	@GetMapping("/config")
	public String log(Map<String, Object> model) {
		model.put("executorStatus", this.configService.getConfigValue("EXECUTOR", "STATUS"));
		model.put("executorMessage", this.configService.getConfigValue("EXECUTOR", "MESSAGE"));

		java.util.List<Category> categoryList = this.categoryService.findNotCompleteCategory();
		model.put("categoryList", categoryList);
		return "config/config";
	}

	@PostMapping("/config")
	public ResponseEntity setConfig(@RequestBody Map<String, Object> model) {
		log.debug("set config with model {}", model);
		String title = (String) model.get("title");
		String welcome = (String) model.get("welcome");
		String adminEmail = (String) model.get("adminEmail");
		Boolean extractView = (Boolean) model.get("extractView");
		String mailHost = (String) model.get("mailHost");
		String mailPort = (String) model.get("mailPort");
		String mailId = (String) model.get("mailId");
		String mailPassword = (String) model.get("mailPassword");
		if (YongaUtil.isEmpty(title) || YongaUtil.isEmpty(welcome)) {
			return ResponseEntity.badRequest().build();
		}
		String oldAdminEmail = this.configService.getConfigValue("CONFIG", "ADMIN_EMAIL");
		String oldMailHost = this.configService.getConfigValue("CONFIG", "MAIL_HOST");
		String oldMailPort = this.configService.getConfigValue("CONFIG", "MAIL_PORT");
		String oldMailId = this.configService.getConfigValue("CONFIG", "MAIL_ID");
		String oldMailPassword = this.configService.getConfigValue("CONFIG", "MAIL_PASSWORD");

		this.configService.setConfigValue("CONFIG", "ADMIN_EMAIL", adminEmail);
		this.configService.setConfigValue("CONFIG", "MAIL_HOST", mailHost);
		this.configService.setConfigValue("CONFIG", "MAIL_PORT", mailPort);
		this.configService.setConfigValue("CONFIG", "MAIL_ID", mailId);
		this.configService.setConfigValue("CONFIG", "MAIL_PASSWORD", mailPassword);
		Boolean sendMail = false;
		if (!YongaUtil.isEmpty(mailId)) {
			try {
				mailService.sendEmailSync(new MailContents("메일 설정 변경 알림", "메일 설정을 변경하였습니다.",
						Arrays.asList("테스트 메일 입니다."),
						Arrays.asList("설정 정보를 확인해 주세요."),
						Arrays.asList("host:" + mailHost, "port: " + mailPort, "id:" + mailId)), adminEmail);
				sendMail = true;
			} catch (MailConnectException e) {
				log.error(e.getMessage(), e);
				return ResponseEntity.badRequest().body("지정한 SMTP 서버에 접속할 수 없습니다. host 와 port 번호를 확인 하세요.");
			} catch (AuthenticationFailedException e) {
				log.error(e.getMessage(), e);
				return ResponseEntity.badRequest().body("메일 서버 인증에 실패하였습니다.");
			} catch (Exception e) {
				logService.addLog("메일 전송 중에 에러가 발생하였습니다. error [" + e.getMessage() + "]");
				log.error(e.getMessage(), e);
				return ResponseEntity.badRequest().body(e.getMessage());
			} finally {
				if (!sendMail) {
					this.configService.setConfigValue("CONFIG", "ADMIN_EMAIL", oldAdminEmail);
					this.configService.setConfigValue("CONFIG", "MAIL_HOST", oldMailHost);
					this.configService.setConfigValue("CONFIG", "MAIL_PORT", oldMailPort);
					this.configService.setConfigValue("CONFIG", "MAIL_ID", oldMailId);
					this.configService.setConfigValue("CONFIG", "MAIL_PASSWORD", oldMailPassword);
				}
			}
		}
		this.configService.setConfigValue("CONFIG", "TITLE", title);
		this.configService.setConfigValue("CONFIG", "WELCOME", welcome);
		this.configService.setConfigValue("CONFIG", "EXTRACT_VIEW", extractView.toString());
		ConfigConstants.APPLICATION_TITLE = title;
		ConfigConstants.APPLICATION_WELCOME = welcome;
		return ResponseEntity.ok().build();
	}
}