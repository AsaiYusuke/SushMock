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
