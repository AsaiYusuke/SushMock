package com.github.AsaiYusuke.SushMock.util;

import java.util.Arrays;
import java.util.List;

import com.github.AsaiYusuke.SushMock.CommandOption;

public class Constants {
	public static CommandOption Option;

	/*
	 * HostKeyProviderのための鍵ファイル
	 *
	 * 作り方はお好みでこのような感じで作成 : keytool -genkey -keyalg RSA -keystore asai.key
	 */
	public static String DefaultKeyFile = "asai.key";

	public static ExecutionType DefaultExecutionType = ExecutionType.Simulate;

	public static int DefaultListenPort = 22;
	/* モック対象の接続先リモートマシン情報(IPアドレス) */
	public static String DefaultRemoteHost = "192.168.1.1";
	/* モック対象の接続先リモートマシン情報(ポート番号) */
	public static int DefaultRemotePort = 22;

	public static String DefaultDataDir = "data";

	public static String DefaultFileFormat = "%1$02d_%2$04d_%3$s";

	public static int DefaultHistoryBufferSize = 10 * 1024 * 1024;

	public static int PipedStreamSize = 1024 * 1024;

	public static List<String> KeyDir = Arrays
			.asList("D:\\_home\\BEYOND\\ssh-key");

	public static enum ExecutionType {
		Unknown, Err, Help, Simulate, Record, // Proxy, Display, Autopilot
	}

	public static enum StreamType {
		Input, InputEcho, Output, Error
	}

	public static enum AuthenticatorType {
		Password, PublicKey
	}
}
