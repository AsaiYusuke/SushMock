package com.github.AsaiYusuke.SushMock;

import java.io.File;
import java.io.IOException;

import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;

import com.github.AsaiYusuke.SushMock.shell.CachedAuthenticator;
import com.github.AsaiYusuke.SushMock.shell.ProxyShellFactory;
import com.github.AsaiYusuke.SushMock.util.Constants;
import com.github.AsaiYusuke.SushMock.util.Constants.ExecutionType;

public class SushMockServer {
	public static void main(String[] args)
			throws IOException, InterruptedException {

		CommandOption option = new CommandOption();
		option.parse(args);

		if (option.getExecutionType() == ExecutionType.Help) {
			return;
		}

		SshServer sshd = SshServer.setUpDefaultServer();
		sshd.setPort(option.getListenPort());
		sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(
				new File(Constants.DefaultKeyFile)));

		CachedAuthenticator authenticator = new CachedAuthenticator();
		sshd.setPasswordAuthenticator(authenticator);

		sshd.setPublickeyAuthenticator(authenticator);

		sshd.setShellFactory(new ProxyShellFactory(authenticator));
		sshd.start();
		System.out.println("started");
		Thread.sleep(Integer.MAX_VALUE);
		System.out.println("end");
	}
}
