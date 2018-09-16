package com.yonga.auc.data.extract;

import com.yonga.auc.data.category.Category;

public class DataExtractException extends Exception {
	private static final long serialVersionUID = 7413256244167548650L;

	private Category category;
	private Integer extractedProductNum;
	public static enum ExtractExceptionMessage {
		LOGIN_FAIL("로그인에 실패하였습니다."),
		UNKNOWN("알 수 없는 에러가 발생하였습니다.")
		;
		private String message;
		ExtractExceptionMessage(String message) {
			this.message = message;
		}
		public String getMessage() {
			return this.message;
		}
	}
	public DataExtractException(ExtractExceptionMessage message) {
		super(message.getMessage());
	}
	public DataExtractException(Category category, Integer extractedProductNum) {
		this.category = category;
		this.extractedProductNum = extractedProductNum;
	}
}
