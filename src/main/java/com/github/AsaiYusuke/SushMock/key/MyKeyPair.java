package com.github.AsaiYusuke.SushMock.key;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

public class MyKeyPair {

	private String dir;
	private KeyPair keyPair;

	public MyKeyPair(String dir) {
		this.dir = dir;

		File directory = new File(dir);
		Map<String, File> privateFileMap = new HashMap<String, File>();
		Map<String, File> publicFileMap = new HashMap<String, File>();

		for (File file : directory.listFiles()) {
			String filename = file.getName();
			if (filename.endsWith(".pub")) {
				publicFileMap.put(filename, file);
			} else {
				privateFileMap.put(filename, file);
			}
		}

		for (Entry<String, File> pubEntry : publicFileMap.entrySet()) {
			String pubFilename = pubEntry.getKey();

			Pattern p = Pattern.compile("\\.pub$");
			String filename = p.matcher(pubFilename).replaceFirst("");
			if (privateFileMap.containsKey(filename)) {
				File publicFile = pubEntry.getValue();
				File privateFile = privateFileMap.get(filename);

				PublicKey publicKey = null;
				PrivateKey privateKey = null;

				try {
					// DataInputStream dis = new DataInputStream(
					// new FileInputStream(publicFile));
					// byte[] pubKeyBytes = new byte[(int) publicFile.length()];
					// dis.readFully(pubKeyBytes);
					// dis.close();
					// X509EncodedKeySpec spec = new X509EncodedKeySpec(
					// pubKeyBytes);
					// KeyFactory keyFactory = KeyFactory.getInstance("RSA");
					// publicKey = keyFactory.generatePublic(spec);
					InputStream is = new FileInputStream(publicFile);
					CertificateFactory cf = CertificateFactory
							.getInstance("X509");
					X509Certificate certificate = (X509Certificate) cf
							.generateCertificate(is);
					publicKey = (RSAPublicKey) certificate.getPublicKey();

				} catch (FileNotFoundException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				} catch (CertificateException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				}

				try {
					DataInputStream dis = new DataInputStream(
							new FileInputStream(privateFile));
					byte[] priKeyBytes = new byte[(int) privateFile.length()];
					dis.readFully(priKeyBytes);
					dis.close();
					PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(
							priKeyBytes);
					KeyFactory keyFactory = KeyFactory.getInstance("RSA");
					privateKey = keyFactory.generatePrivate(spec);

				} catch (FileNotFoundException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				} catch (IOException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				} catch (NoSuchAlgorithmException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				} catch (InvalidKeySpecException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				}

				if (publicKey != null && privateKey != null) {
					keyPair = new KeyPair(publicKey, privateKey);
				}
			}
		}
	}

	public String getDir() {
		return dir;
	}

	public KeyPair getKeyPair() {
		return keyPair;
	}
}
