/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License") +  you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.reloadprotocol;

import org.apache.coyote.http11.Http11NioProtocol;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

import java.util.Timer;
import java.util.TimerTask;

public class Http11NioReloadProtocol extends Http11NioProtocol {
	private static final Log log = LogFactory.getLog(Http11NioReloadProtocol.class);
	private int reloadInterval = 3600000; // every hour default
	private boolean initDone = false;
	private TimerTask task = null;
	private Timer timer = null;

	public Http11NioReloadProtocol() {
		super();
	}

	private void reinitTimer() {
		if (timer != null) {
			this.timer.cancel();
		}
		if (task != null) {
			this.task.cancel();
		}

		timer = new Timer();
		task = new TimerTask() {
			@Override
			public void run() {
				reloadSSLConfigs();
			}
		};
	}

	public int getReloadInterval() {
		return this.reloadInterval;
	}

	public void setReloadInterval(int reloadInterval) {
		this.reloadInterval = reloadInterval * 1000;
		log.info("SSL reload interval is set to " + reloadInterval + " seconds");
		if (initDone) {
			reinitTimer();
			timer.schedule(task, reloadInterval, reloadInterval);
		}
	}

	public void reloadSSLConfigs() {
		try {
			this.getEndpoint().reloadSslHostConfigs();
			log.info("SSL configuration has been successfully reloaded");
		} catch (Exception e) {
			log.error("SSL configuration reload encountered the problem", e);
		}
	}

	@Override
	public void init() throws Exception {
		super.init();
		log.info("SSL configuration reloader started");
		initDone = true;
		reinitTimer();
		timer.schedule(task, reloadInterval, reloadInterval);
	}

	@Override
	public void destroy() throws Exception {
		log.info("SSL configuration reloader destroyed");
		if (timer != null) {
			this.timer.cancel();
		}
		if (task != null) {
			this.task.cancel();
		}
		super.destroy();
	}
}
