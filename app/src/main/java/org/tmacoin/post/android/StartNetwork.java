/*******************************************************************************
 * Copyright � 2018 Tma Coin admin@tmacoin.org, tmacoin@yahoo.com All rights reserved. No warranty, explicit or implicit, provided.
 * Permission granted to use Tma Coin client/blockchain free of charge. Any part of the software cannot be copied or modified to run or be used on any new or existing blockchain, distributed ledger, consensus system.
 * Any contribution to the project to improve and promote Tma Coin is welcome and automatically becomes part of Tma Coin project with this copyright.
 *
 * Authors addresses: 8LpN97eRQ2CQ95DaZoMiNLmuSM7NKKVKrUda, 6XUtJgWAzbqCH2XkU3eJhMm1eDcsQ8vDg8Uo
 *******************************************************************************/
package org.tmacoin.post.android;

import java.net.UnknownHostException;

import org.tma.blockchain.Wallet;
import org.tma.peer.Network;
import org.tma.util.ThreadExecutor;
import org.tma.util.TmaLogger;
import org.tma.util.TmaRunnable;

public class StartNetwork {

	private static final TmaLogger logger = TmaLogger.getLogger();
	private static final StartNetwork instance = new StartNetwork();
	public static StartNetwork getInstance() {
		return instance;
	}
	
	public void start(final MainActivity mainActivity) {
		ThreadExecutor.getInstance().execute(new TmaRunnable("StartNetwork") {
			public void doRun() {
				Wallet wallet = Wallets.getInstance().getWallet(Wallets.TMA, Wallets.WALLET_NAME);
				String tmaAddress = wallet.getTmaAddress();
				try {
					new Network(tmaAddress);
				} catch (UnknownHostException e) {
					logger.error(e.getMessage(), e);
				}
				mainActivity.connectedToTmaNetwork();
			}
		});
	}

}
