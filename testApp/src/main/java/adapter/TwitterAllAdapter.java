/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package adapter;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.s4.base.Event;
import org.apache.s4.core.adapter.AdapterApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eda.Tweet;

import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

//import utilities.Reader;

public class TwitterAllAdapter extends AdapterApp implements Runnable {

	private static Logger logger = LoggerFactory
			.getLogger(TwitterAllAdapter.class);

	private LinkedBlockingQueue<Status> messageQueue = new LinkedBlockingQueue<Status>();
	private Thread t;

	public int eventCount = 0;

	@Override
	protected void onInit() {
		logger.info("TwitterAllAdapter");
		t = new Thread(this);
		super.onInit();
	}

	public void connectAndRead() throws Exception {
		ConfigurationBuilder cb = new ConfigurationBuilder();
		Properties twitterProperties = new Properties();
		/*File twitter4jPropsFile = new File(System.getProperty("user.home")
				+ "/twitter4j.properties");*/
		File twitter4jPropsFile = new File("../twitter4j.properties");
		if (!twitter4jPropsFile.exists()) {
			logger.error(
					"Cannot find twitter4j.properties file in this location :[{}]",
					twitter4jPropsFile.getAbsolutePath());
			return;
		}

		twitterProperties.load(new FileInputStream(twitter4jPropsFile));

		cb = new ConfigurationBuilder();

		cb.setOAuthConsumerKey(twitterProperties
				.getProperty("oauth.consumerKey"));
		cb.setOAuthConsumerSecret(twitterProperties
				.getProperty("oauth.consumerSecret"));
		cb.setOAuthAccessToken(twitterProperties
				.getProperty("oauth.accessToken"));
		cb.setOAuthAccessTokenSecret(twitterProperties
				.getProperty("oauth.accessTokenSecret"));

		cb.setDebugEnabled(false);
		cb.setPrettyDebugEnabled(false);
		cb.setIncludeMyRetweetEnabled(false);

		TwitterStream twitterStream = new TwitterStreamFactory(cb.build())
				.getInstance();

		StatusListener statusListener = new StatusListener() {

			@Override
			public void onException(Exception ex) {
			}

			@Override
			public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
			}

			@Override
			public void onStatus(Status status) {
				messageQueue.add(status);
			}

			@Override
			public void onScrubGeo(long userId, long upToStatusId) {
			}

			@Override
			public void onDeletionNotice(
					StatusDeletionNotice statusDeletionNotice) {
			}

			@Override
			public void onStallWarning(StallWarning arg0) {
			}

		};

		twitterStream.addListener(statusListener);
		twitterStream.sample();

	}

	@Override
	protected void onStart() {

		try {
			t.start();
			connectAndRead();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void run() {

		while(true){

			try {
				Status status = this.messageQueue.take();

				Event event = new Event();

				int rtCount =  0;
				int fvCount =  0;


				if(status.isRetweet()){
                   
                   	rtCount = status.getRetweetedStatus().getRetweetCount();
					fvCount = status.getRetweetedStatus().getFavoriteCount();
                } 


				Tweet tweet = new Tweet(status.getId(), status.getText(),
						status.getCreatedAt(), status.getPlace(), status
								.getUser().getScreenName(), status.getUser()
								.getLang(), status.getUser()
								.getFollowersCount(), status.getUser()
								.getFriendsCount(),
						status.getHashtagEntities(), fvCount,
						rtCount, status.getGeoLocation());
						//status.getRetweetedStatus().getRetweetCount(), status.getGeoLocation());

				

				eventCount++;
				// cantReplicas: Cantidad de PEs que se quieren generar para el proximo operador
				// Nota: recuerden que la topología no necesariamente debía ser de este estilo
				// también podía ser por un hash
				int cantReplicas = 10;
				event.put("levelTweet", Integer.class, eventCount % cantReplicas);

				event.put("id", Integer.class, eventCount);
				event.put("tweet", Tweet.class, tweet);

				getRemoteStream().put(event);

			} catch (Exception e) {
				logger.error("Error: " + e);
				logger.error("Error al crear evento");
			}

		}


	}

}
