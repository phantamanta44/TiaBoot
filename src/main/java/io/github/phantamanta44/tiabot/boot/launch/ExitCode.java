package io.github.phantamanta44.tiabot.boot.launch;

import java.util.Arrays;

public enum ExitCode {

	UNKNOWN(-1),
	EXITED(0),
	ERRORED(1),
	TERMINATED(130),
	REBOOT_REQUESTED(32),
	UPDATE_REQUESTED(33),
	LOGIN_FAILURE(34),
	CONNECT_FAILURE(35);
	
	public final int code;
	
	private ExitCode(int code) {
		this.code = code;
	}
	
	public static ExitCode forCode(int code) {
		return Arrays.stream(values())
				.filter(e -> e.code == code)
				.findAny().orElse(ExitCode.UNKNOWN);
	}
	
}
