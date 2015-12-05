package eda;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import twitter4j.GeoLocation;
import twitter4j.HashtagEntity;
import twitter4j.Place;

public class Tweet implements Cloneable {
	private int idTweet;

	// Data Tweet
	private long id;
	private String text;
	private Date date;
	private String country;

	// Data user
	private String userName;
	private String userLanguage;
	private int followersCount;
	private int friendsCount;

	// Statistics Tweet
	private HashtagEntity[] hashtag;
	private int favoriteCount;
	private int retweetCount;

	//Sentiment
	private String classSent;

	// Geolocation
	private GeoLocation geolocation;

	// HashMap
	HashMap<String, String> others = new HashMap<String, String>();

	public Tweet() {
		this.idTweet = 0;
		this.id = 0;
		this.text = "";
		this.date = null;
		this.country = "";
		this.userName = "";
		this.userLanguage = "";
		this.followersCount = 0;
		this.friendsCount = 0;
		this.hashtag = null;
		this.favoriteCount = 0;
		this.retweetCount = 0;
		this.geolocation = null;
	}

	public Tweet(long id, String text, Date date, Place place, String userName,
			String userLanguage, int followersCount, int friendsCount,
			HashtagEntity[] hashtag, int favoriteCount, int retweetCount,
			GeoLocation geolocation) {
		this.id = id;
		this.text = text;
		this.date = date;
		if (place == null) {
			this.country = "nc";
		} else {
			this.country = place.getCountry();
		}
		this.userName = userName;
		this.userLanguage = userLanguage;
		this.followersCount = followersCount;
		this.friendsCount = friendsCount;
		this.hashtag = hashtag;
		this.favoriteCount = favoriteCount;
		this.retweetCount = retweetCount;
		this.geolocation = geolocation;

	}

	public Tweet getClone() {

		try {
			return (Tweet) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void addHashMap(String key, String value) {
		others.put(key, value);
	}

	public int getIdTweet() {
		return idTweet;
	}

	public void setIdTweet(int idTweet) {
		this.idTweet = idTweet;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserLanguage() {
		return userLanguage;
	}

	public void setUserLanguage(String userLanguage) {
		this.userLanguage = userLanguage;
	}

	public int getFollowersCount() {
		return followersCount;
	}

	public void setFollowersCount(int followersCount) {
		this.followersCount = followersCount;
	}

	public int getFriendsCount() {
		return friendsCount;
	}

	public void setFriendsCount(int friendsCount) {
		this.friendsCount = friendsCount;
	}

	public HashtagEntity[] getHashtag() {
		return hashtag;
	}

	public void setHashtag(HashtagEntity[] hashtag) {
		this.hashtag = hashtag;
	}

	public int getFavoriteCount() {
		return favoriteCount;
	}

	public void setFavoriteCount(int favoriteCount) {
		this.favoriteCount = favoriteCount;
	}

	public int getRetweetCount() {
		return retweetCount;
	}

	public void setRetweetCount(int retweetCount) {
		this.retweetCount = retweetCount;
	}

	public GeoLocation isGeolocation() {
		return geolocation;
	}

	public void setGeolocation(GeoLocation geolocation) {
		this.geolocation = geolocation;
	}

	@Override
	public String toString() {

		return "userName: " + userName + " | text: " + text;

	}

	public DBObject storeEvent() {

		DBObject objMongo = new BasicDBObject();

		objMongo.put("id", id);
		objMongo.put("text", text);
		objMongo.put("date", date);
		objMongo.put("country", country);

		objMongo.put("userName", userName);
		objMongo.put("userLanguage", userLanguage);
		objMongo.put("followersCount", followersCount);
		objMongo.put("friendsCount", friendsCount);

		objMongo.put("favoriteCount", favoriteCount);
		objMongo.put("retweetCount", retweetCount);

		for (HashtagEntity hashtagCurrent : hashtag) {
			DBObject hashtagEntityMongo = new BasicDBObject();
			hashtagEntityMongo.put("hashtag", hashtagCurrent.getText());
			hashtagEntityMongo.put("init", hashtagCurrent.getStart());
			hashtagEntityMongo.put("final", hashtagCurrent.getEnd());

			objMongo.put("hashtag", hashtagEntityMongo);
		}

		DBObject geolocationMongo = new BasicDBObject();
		if (geolocation != null) {
			geolocationMongo.put("latitude", geolocation.getLatitude());
			geolocationMongo.put("longitude", geolocation.getLongitude());
			objMongo.put("geolocation", geolocationMongo);
		} else {
			objMongo.put("geolocation", "false");
		}

		for (Map.Entry<String, String> entry : others.entrySet()) {
			objMongo.put(entry.getKey(), entry.getValue());
		}

		return objMongo;
	}

}
