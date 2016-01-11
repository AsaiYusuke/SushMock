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