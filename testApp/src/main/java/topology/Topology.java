/**
 * or more contributor license agreements.  See the NOTICE file
 * Licensed to the Apache Software Foundation (ASF) under one
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

package topology;

import java.util.Arrays;
import java.util.List;

import org.apache.s4.base.Event;
import org.apache.s4.base.KeyFinder;
import org.apache.s4.core.App;
import org.apache.s4.core.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import processElements.OnePE;
import processElements.ThreePE;
import processElements.TwoPE;

public class Topology extends App {
	private static Logger logger = LoggerFactory.getLogger(Topology.class);

	/* PEs de la App */
	OnePE onePE;
	TwoPE twoPE;
	ThreePE threePE;

	@Override
	protected void onInit() {
		// Create the PE prototype
		onePE = createPE(OnePE.class);
		twoPE = createPE(TwoPE.class);
		threePE = createPE(ThreePE.class);

		// Create a stream that listens to the "inputStream" stream and passes
		// events to the processPE instance.
		createInputStream("inputStream", new KeyFinder<Event>() {
			@Override
			public List<String> get(Event event) {
				return Arrays.asList(new String[] { event
						.get("levelOneStream") });
			}
		}, onePE).setParallelism(8);

		Stream<Event> twoStream = createStream("twoStream",
				new KeyFinder<Event>() {
					@Override
					public List<String> get(Event event) {

						return Arrays.asList(new String[] { event
								.get("levelTwoStream") });
					}
				}, twoPE).setParallelism(32);

		Stream<Event> threeStream = createStream("threeStream",
				new KeyFinder<Event>() {
					@Override
					public List<String> get(Event event) {

						return Arrays.asList(new String[] { event
								.get("levelThreeStream") });
					}
				}, threePE).setParallelism(16);

		// Setting DownStream in App
		onePE.setDownStream(twoStream);
		twoPE.setDownStream(threeStream);
	}

	@Override
	protected void onStart() {
		logger.info("Start Topology");
	}

	@Override
	protected void onClose() {
		logger.info("Finish experiment");
	}

}
