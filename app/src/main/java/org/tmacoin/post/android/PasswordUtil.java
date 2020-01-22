/*******************************************************************************
 * Copyright � 2018 Tma Coin admin@tmacoin.org, tmacoin@yahoo.com All rights reserved. No warranty, explicit or implicit, provided.
 * Permission granted to use Tma Coin client/blockchain free of charge. Any part of the software cannot be copied or modified to run or be used on any new or existing blockchain, distributed ledger, consensus system.
 * Any contribution to the project to improve and promote Tma Coin is welcome and automatically becomes part of Tma Coin project with this copyright.
 *
 * Authors addresses: 8LpN97eRQ2CQ95DaZoMiNLmuSM7NKKVKrUda, 6XUtJgWAzbqCH2XkU3eJhMm1eDcsQ8vDg8Uo
 *******************************************************************************/
package org.tmacoin.post.android;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.tma.blockchain.Wallet;
import org.tma.util.Base58;
import org.tma.util.Constants;
import org.tma.util.Encryptor;
import org.tma.util.StringUtil;
import org.tma.util.TmaLogger;

public class PasswordUtil {

	private static final TmaLogger logger = TmaLogger.getLogger();
	
	static {
		setupBouncyCastle();
	}
	
	private static final Wallets wallets = Wallets.getInstance();
	private static final Encryptor encryptor = new Encryptor();

	public static void setupBouncyCastle() {
		Security.setProperty("crypto.policy", "unlimited");
		Security.addProvider(new BouncyCastleProvider());
		final Provider provider = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);
		if (provider == null) {
			// Web3j will set up the provider lazily when it's first used.
			return;
		}
		if (provider.getClass().equals(BouncyCastleProvider.class)) {
			// BC with same package name, shouldn't happen in real life.
			return;
		}
		// Android registers its own BC provider. As it might be outdated and might not include
		// all needed ciphers, we substitute it with a known BC bundled in the app.
		// Android's BC has its package rewritten to "com.android.org.bouncycastle" and because
		// of that it's possible to have another BC implementation loaded in VM.
		Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
		Security.insertProviderAt(new BouncyCastleProvider(), 1);
	}

	public PasswordUtil() {

	}
	
	public boolean loadKeys(String passphrase) throws Exception {
		File keyFile = new File(Constants.FILES_DIRECTORY + Constants.KEYS);
		try (
				InputStream is = new FileInputStream(keyFile);
				BufferedReader in = new BufferedReader(new InputStreamReader(is));
		) {
			String line;
			int i = 0;
			while ((line = in.readLine()) != null) {
				List<String> list =  new ArrayList<String>(Arrays.asList(line.split(",")));
				int size = list.size();
				if(size != 4 && size != 2) {
					continue;
				}
				Wallet wallet = new Wallet();
				if(size == 2) {
					list.add(0, Integer.toString(i));
					list.add(0, Wallets.TMA);
				}
				
				String application = list.get(0);
				String name = list.get(1);
				PublicKey publicKey = StringUtil.loadPublicKey(list.get(2));
				PrivateKey privateKey = StringUtil.loadPrivateKey(encryptor.decrypt(passphrase, Base58.decode(list.get(3))));
				if(privateKey == null) {
					logger.debug("Passphrase entered was not correct. Try again.");
					return false;
				}
				wallet.setPrivateKey(privateKey);
				wallet.setPublicKey(publicKey);
				wallets.putWallet(application, name, wallet);
				i++;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		if(wallets.getNames(Wallets.TMA).isEmpty()) {
			keyFile.delete();
			return false;
		}
		return true;
	}

	public void saveKeys(String passphrase) throws Exception {
		File file = new File(Constants.FILES_DIRECTORY + Constants.KEYS);
		try {
			if(file.exists()) {
				copyFile(file.getAbsolutePath(), Constants.FILES_DIRECTORY + Constants.KEYS + ".backup");
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		file.delete();
		File parentDirectory = file.getParentFile();
		boolean parentDirectoryCreated = false;
		if(parentDirectory != null) {
			parentDirectoryCreated = parentDirectory.mkdirs();
		}
		if(parentDirectoryCreated) {
			logger.debug( "Parent directory created: {}");
		}
		try (
				OutputStream os = new FileOutputStream(new File(Constants.FILES_DIRECTORY + Constants.KEYS));
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(os));
		) {
			for (String application : wallets.getApplications()) {
				for(String name: wallets.getNames(application) ) {
					Wallet wallet = wallets.getWallet(application, name);
					String publicKey = Base58.encode(wallet.getPublicKey().getEncoded());
					String privateKey = Base58.encode(encryptor.encrypt(passphrase, wallet.getPrivateKey().getEncoded()));
					out.write(application + "," + name + "," + publicKey + "," + privateKey);
					out.newLine();
				}

			}
			out.flush();
		}
	}
	
	public void generateKey(String application, String name, String passphrase) {
		Wallet wallet = new Wallet();
		wallet.generateKeyPair();
		wallets.putWallet(application, name, wallet);
		logger.debug("New key was generated for application {} with name {} and address {}", application, name, wallet.getTmaAddress());
		try {
			saveKeys(passphrase);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void copyFile(String source, String destination) {
		InputStream is = null;
		OutputStream os = null;
		try {
			is = new FileInputStream(source);
			os = new FileOutputStream(destination);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = is.read(buffer)) > 0) {
				os.write(buffer, 0, length);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			try {
				if(is != null) {
					is.close();
				}
				if(os != null) {
					os.close();
				}

			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}

		}
	}

}
