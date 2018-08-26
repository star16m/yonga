package com.yonga.auc.data.extract;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
public class ExtractSiteInfo {

	@Value("${app.brand-auc.uid}")
	private String uid;
	@Value("${app.brand-auc.pwd}")
	private String pwd;
	@Value("${app.brand-auc.executor}")
	private String executor;
	@Value("${app.brand-auc.targetProtocol}")
	private String targetProtocol;
	@Value("${app.brand-auc.targetHost}")
	private String targetHost;
	@Value("${app.brand-auc.work.path}")
	private String workRoot;
	@Value("${app.brand-auc.exportPassword}")
	private String exportPassword;
	public final String workPathList = "list";
	public final String workPathDetail = "detail";
	
	public String getTargetURL() {
		return getTargetURL("");
	}
	public String getTargetURL(String path) {
		return String.format("%s://%s/%s", this.targetProtocol, this.targetHost, path);
	}
}
