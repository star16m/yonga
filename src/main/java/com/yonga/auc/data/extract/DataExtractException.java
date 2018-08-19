package com.yonga.auc.data.extract;

public class DataExtractException extends Exception {
	private static final long serialVersionUID = 7413256244167548650L;

	public static enum ExtractExceptionMessage {
		LOGIN_FAIL("로그인에 실패하였습니다."),
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
}
