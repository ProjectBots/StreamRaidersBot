package run;

import program.Logger;

public class StreamRaidersException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private Exception cause;
	private String text;
	public StreamRaidersException(String text, Exception cause, boolean silent, String cid, Integer slot) {
		this.cause = cause;
		this.text = text;
		Logger.printException(text, cause, Logger.runerr, Logger.fatal, cid, slot, !silent);
	}
	public StreamRaidersException(String text, Exception cause, String cid, Integer slot) {
		this.cause = cause;
		this.text = text;
		Logger.printException(text, cause, Logger.runerr, Logger.fatal, cid, slot, true);
	}
	public StreamRaidersException(String text, String cid, Integer slot) {
		cause = null;
		this.text = text;
		Logger.print(text, Logger.runerr, Logger.fatal, cid, slot, true);
	}
	public String getText() {
		return text;
	}
	public Exception getCause() {
		return cause;
	}
}
