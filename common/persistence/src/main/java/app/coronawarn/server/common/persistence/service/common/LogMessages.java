package app.coronawarn.server.common.persistence.service.common;

public enum LogMessages {

	KEYS_SELECTED_FOR_UPLOAD("Keys selected for upload: {}"), //
	KEYS_PICKED_FROM_UPLOAD_TABLE("{} keys picked after read from upload table"), //
	;

	private final String message;

	/**
	 * @return the log message (default English).
	 */
	public String toString() {
		return message;
	}

	private LogMessages(String message) {
		this.message = message;
	}
}
