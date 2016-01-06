package com.github.AsaiYusuke.SushMock.shell;

import java.security.PublicKey;

import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator;
import org.apache.sshd.server.session.ServerSession;

import com.github.AsaiYusuke.SushMock.SushMockServer;
import com.github.AsaiYusuke.SushMock.util.Constants.AuthenticatorType;

public class CachedAuthenticator
		implements PasswordAuthenticator, PublickeyAuthenticator {

	private AuthenticatorType authenticatorType;
	private String username;
	private String password;
	private PublicKey key;

	@Override
	public boolean authenticate(String username, String password,
			ServerSession session) {

		this.authenticatorType = AuthenticatorType.Password;
		this.username = username;
		this.password = password;

		if (username.equalsIgnoreCase("shutdown")) {
			SushMockServer.shutdown();
		}

		return true;
	}

	@Override
	public boolean authenticate(String username, PublicKey key,
			ServerSession session) {

		this.authenticatorType = AuthenticatorType.PublicKey;
		this.username = username;
		this.key = key;

		if (username.equalsIgnoreCase("shutdown")) {
			SushMockServer.shutdown();
		}

		return true;
	}

	public AuthenticatorType getAuthenticatorType() {
		return authenticatorType;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public PublicKey getPublicKey() {
		return key;
	}

}
