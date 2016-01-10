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
package com.github.AsaiYusuke.SushMock.key;

import java.security.KeyPair;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

import com.github.AsaiYusuke.SushMock.util.Constants;

public class KeyTank {

	private static Map<PublicKey, KeyPair> keyMap;

	private static void initializeKeyMap() {
		if (keyMap == null || keyMap.size() == 0) {
			keyMap = new HashMap<PublicKey, KeyPair>();

			for (String dir : Constants.KeyDir) {
				MyKeyPair myKeyPair = new MyKeyPair(dir);
				KeyPair keyPair = myKeyPair.getKeyPair();
				keyMap.put(keyPair.getPublic(), keyPair);
			}
		}
	}

	public static KeyPair getKeyPair(PublicKey publicKey) {
		initializeKeyMap();
		return keyMap.get(publicKey);
	}
}
