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
package com.github.AsaiYusuke.SushMock.shell;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.PublicKey;
import java.util.Map;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.future.AuthFuture;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.server.shell.InvertedShell;

import com.github.AsaiYusuke.SushMock.key.KeyTank;
import com.github.AsaiYusuke.SushMock.task.ShellTask;
import com.github.AsaiYusuke.SushMock.util.Constants;
import com.github.AsaiYusuke.SushMock.util.Constants.AuthenticatorType;
import com.github.AsaiYusuke.SushMock.util.Constants.ExecutionType;

public class ProxyShell implements InvertedShell {

	private AuthenticatorType authenticatorType;
	private String remoteUser;
	private String remotePass;
	private PublicKey remoteKey;
	private String remoteHost;
	private int remotePort;

	private ClientChannel channel;
	private ClientSession session;
	private SshClient client;

	ShellTask shell;

	public ProxyShell(CachedAuthenticator authenticator) {
		authenticatorType = authenticator.getAuthenticatorType();
		remoteUser = authenticator.getUsername();

		switch (authenticator.getAuthenticatorType()) {
		case Password:
			remotePass = authenticator.getPassword();
			break;
		case PublicKey:
			remoteKey = authenticator.getPublicKey();
			break;
		}

		remoteHost = Constants.Option.getRemoteHost();
		remotePort = Constants.Option.getRemotePort();
	}

	@Override
	public void start(Map<String, String> env) throws IOException {

		if (remoteUser.equals("shutdown")) {
			return;
		}

		try {
			shell = new ShellTask();

			if (Constants.Option.getExecutionType() == ExecutionType.Record) {
				if (client == null || !client.isOpen()) {
					client = SshClient.setUpDefaultClient();
					System.out.println("start client");
					client.start();
				}

				if (session == null || !session.isOpen()) {
					System.out.println("connect");
					session = client.connect(remoteUser, remoteHost, remotePort)
							.await().getSession();

					System.out.println("auth");
					switch (authenticatorType) {
					case Password:
						session.addPasswordIdentity(remotePass);
						break;
					case PublicKey:
						session.addPublicKeyIdentity(
								KeyTank.getKeyPair(remoteKey));
						break;
					}
					AuthFuture auth = session.auth();
					auth.verify();
					if (!auth.isSuccess()) {
						client.stop();
						IOException e = new IOException("auth failed.");
						throw e;
					}
					System.out.println("auth successed");

				}

				if (channel == null || !channel.isOpen()) {
					System.out.println("create channel");

					channel = session
							.createChannel(ClientChannel.CHANNEL_SHELL);

					channel.setIn(shell.getClientIn());
					channel.setOut(shell.getClientOut());
					channel.setErr(shell.getClientErr());

					System.out.println("channel open");
					channel.open();
				}

			} else if (Constants.Option
					.getExecutionType() == ExecutionType.Simulate) {
				System.out.println("create channel");

				if (channel == null || !channel.isOpen()) {
					channel = new DummyClientChannel();

					System.out.println("channel open");
					channel.open();
				}
			}

			shell.setChannel(channel);

			shell.execute();

		} catch (IOException e) {
			System.err.println(e.getMessage());
			if (channel != null && channel.isOpen()) {
				channel.close();
			}
			if (session != null && !session.isClosed()) {
				session.close();
			}
			if (client != null && !client.isClosed()) {
				client.stop();
			}
			throw e;
		}

	}

	@Override
	public OutputStream getInputStream() {
		return shell.getServerIn();
	}

	@Override
	public InputStream getOutputStream() {
		return shell.getServerOut();
	}

	@Override
	public InputStream getErrorStream() {
		return shell.getServerErr();
	}

	@Override
	public boolean isAlive() {
		return shell.isAlive();
	}

	@Override
	public int exitValue() {
		return 0;
	}

	@Override
	public void destroy() {
		shell.destroy();
		if (channel != null && channel.isOpen()) {
			try {
				channel.close();
			} catch (IOException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
		}
	}
}
