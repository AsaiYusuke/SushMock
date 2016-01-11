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

		this.authenticatorType = AuthenticatorType.PASSWORD;
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

		this.authenticatorType = AuthenticatorType.PUBLIC_KEY;
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
