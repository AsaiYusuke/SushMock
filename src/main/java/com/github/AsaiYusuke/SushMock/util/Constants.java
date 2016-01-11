/*
 * Copyright (C) 2016 Asai Yusuke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.AsaiYusuke.SushMock.util;

import com.github.AsaiYusuke.SushMock.CommandOption;
import com.github.AsaiYusuke.SushMock.ext.defaultExt.DateMaskCompareTransformer;

public class Constants {
	public static CommandOption Option;

	/*
	 * HostKeyProviderのための鍵ファイル
	 *
	 * 作り方はお好みでこのような感じで作成 : keytool -genkey -keyalg RSA -keystore asai.key
	 */
	public static String DefaultKeyFile = "asai.key";

	public static ExecutionType DefaultExecutionType = ExecutionType.SERVER;

	public static int DefaultListenPort = 22;
	/* モック対象の接続先リモートマシン情報(IPアドレス) */
	public static String DefaultRemoteHost = "192.168.1.1";
	/* モック対象の接続先リモートマシン情報(ポート番号) */
	public static int DefaultRemotePort = 22;

	public static String DefaultDataDir = "data";

	public static int DefaultHistoryBufferSize = 10 * 1024 * 1024;

	public static String[] DefaultExtensions = new String[] {
			DateMaskCompareTransformer.class.getName() };

	public static String[] DefaultKeyDirs = new String[] {
			"c:\\some\\where\\ssh-key-dir" };

	public static int PipedStreamSize = 1024 * 1024;

	public static enum ExecutionType {
		UNKNOWN, ERROR, HELP, SERVER, CLIENT, RECORD, // PROXY, DISPLAY,
														// AUTOPILOT
	}

	public static enum StreamType {
		IN, IN_ECHO, OUT, ERR
	}

	public static enum AuthenticatorType {
		PASSWORD, PUBLIC_KEY
	}
}
