/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package adapter;

import org.apache.s4.base.Event;
import org.apache.s4.core.adapter.AdapterApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import processElements.ProcessOnePE;

public class Adapter extends AdapterApp implements Runnable {
	private static Logger logger = LoggerFactory.getLogger(Adapter.class);
	private boolean showEvent = false;

	@Override
	protected void onInit() {
		logger.info("Create Adapter");
		thread = new Thread(this);
		super.onInit();
	}

	@Override
	protected void onStart() {
		/* Este orden es importante */
		super.onStart();

		try {
			thread.start();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void run() {

		while (true) {

			Event event = new Event();

			event.put("levelOneStream", Long.class, getEventCount()
					% 100);
			event.put("time", Long.class, System.currentTimeMillis());

			if (showEvent) {
				logger.debug(event.getAttributesAsMap().toString());
			}

			getRemoteStream().put(event);

			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}
}
