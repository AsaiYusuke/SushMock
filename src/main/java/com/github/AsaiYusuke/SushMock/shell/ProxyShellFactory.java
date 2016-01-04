package com.github.AsaiYusuke.SushMock.shell;

import org.apache.sshd.server.Command;
import org.apache.sshd.server.shell.InvertedShellWrapper;
import org.apache.sshd.server.shell.ProcessShellFactory;

public class ProxyShellFactory extends ProcessShellFactory {

	private CachedAuthenticator authenticator;

	public ProxyShellFactory(CachedAuthenticator authenticator) {
		this.authenticator = authenticator;
	}

	@Override
	public Command create() {
		return new InvertedShellWrapper(new ProxyShell(authenticator));
	}
}
