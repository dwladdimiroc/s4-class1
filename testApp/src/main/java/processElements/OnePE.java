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

package processElements;

import org.apache.s4.base.Event;
import org.apache.s4.core.ProcessingElement;
import org.apache.s4.core.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class OnePE extends ProcessingElement {
	private static Logger logger = LoggerFactory.getLogger(OnePE.class);
	private boolean showEvent = false;

	Stream<Event> downStream;

	public void setDownStream(Stream<Event> stream) {
		downStream = stream;
	}

	public void onEvent(Event event) {
		// Processing
		try {
			Thread.sleep(20);
		} catch (InterruptedException e) {
			logger.error(e.toString());
		}

		Event eventOutput = new Event();

		eventOutput.put("levelTwoStream", Long.class, getEventCount()
				% 100);
		eventOutput.put("time", Long.class, event.get("time", Long.class));

		if (showEvent) {
			logger.debug(eventOutput.getAttributesAsMap().toString());
		}

		downStream.put(eventOutput);

	}

	@Override
	protected void onCreate() {
		logger.info("Create One PE");
	}

	@Override
	protected void onRemove() {
		logger.info("Remove One PE");
	}

}
