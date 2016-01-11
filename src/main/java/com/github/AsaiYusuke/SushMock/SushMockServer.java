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
package com.github.AsaiYusuke.SushMock;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;

import com.github.AsaiYusuke.SushMock.shell.CachedAuthenticator;
import com.github.AsaiYusuke.SushMock.shell.ProxyShellFactory;
import com.github.AsaiYusuke.SushMock.util.Constants;
import com.github.AsaiYusuke.SushMock.util.Constants.ExecutionType;

public class SushMockServer {
	public static boolean isRunning;

	public static void main(String[] args)
			throws IOException, InterruptedException {

		CommandOption option = new CommandOption();
		option.parse(args);

		if (option.getExecutionType() == ExecutionType.HELP) {
			return;
		}

		SshServer sshd = SshServer.setUpDefaultServer();
		sshd.setPort(option.getListenPort());
		sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(
				Paths.get(Constants.DefaultKeyFile)));

		CachedAuthenticator authenticator = new CachedAuthenticator();
		sshd.setPasswordAuthenticator(authenticator);

		sshd.setPublickeyAuthenticator(authenticator);

		sshd.setShellFactory(new ProxyShellFactory(authenticator));
		sshd.start();
		System.out.println("started");

		isRunning = true;
		while (isRunning) {
			Thread.sleep(1000);
		}
		System.out.println("end");
	}

	public static void shutdown() {
		isRunning = false;
	}
}
