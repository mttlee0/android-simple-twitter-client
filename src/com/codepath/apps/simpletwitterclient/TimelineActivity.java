package com.codepath.apps.simpletwitterclient;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.codepath.apps.simpletwitterclient.models.Tweet;
import com.codepath.apps.simpletwitterclient.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class TimelineActivity extends Activity {
	private static final int iNumberOfTweets = 25;
	User user;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timeline);
		
		getAccountInfo();
		getHomeTimeLineAndRefresh();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.timeline, menu);		
		return true;
	}
	
	// Use the Twitter Rest client to submit retrieve a list of tweets and updates the listview. Saves tweets in JSON form in SharedPreferences
	private void getHomeTimeLineAndRefresh () {
        RequestParams params = new RequestParams();
        params.put("count", String.valueOf(TimelineActivity.iNumberOfTweets));
        
		TwitterApp.getRestClient().getHomeTimeline(params, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(JSONArray jsonTweets) {
				ArrayList<Tweet> tweets = Tweet.fromJson(jsonTweets);
				
				ListView lvTweets = (ListView)findViewById(R.id.lvTweets);
				TweetsAdapter adapter = new TweetsAdapter(getBaseContext(), tweets);
				lvTweets.setAdapter(adapter);
				
				SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				Editor edit = pref.edit();
				edit.putString("tweets", jsonTweets.toString());
				edit.commit();
			}
			
			// If we are unsuccessful at getting live data, we will get data from local storage
			@Override
			public void onFailure(Throwable error, String content) {
				Toast.makeText(getApplicationContext(), "Twitter is having issues. Offline mode.", Toast.LENGTH_SHORT).show();
				
				try
				{
					ArrayList<Tweet> tweets = Tweet.fromJson(new JSONArray(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("tweets", "")));
					
					ListView lvTweets = (ListView)findViewById(R.id.lvTweets);
					TweetsAdapter adapter = new TweetsAdapter(getBaseContext(), tweets);
					lvTweets.setAdapter(adapter);				
				} catch (JSONException e) {
					e.printStackTrace();					
				}
			} 
		});
	}
	
	// Gets the info of the user and saves it in JSON in SharedPreferences
	// TODO: Check with Tim & Nathan if commit to SharedPreferences is sync or async. Documentation says sync 
	// TODO: Ask Tim & Nathan if onSuccess and onFinish are sync or async. Guessing its async 
	// TODO: A better way would be to save the user info during the Login Activity
	// TODO: Is there a way to simulate no network activity besides disconnecting my wi-fi?
	private void getAccountInfo() {
		TwitterApp.getRestClient().getUserInfo(new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(JSONObject jsonUser) {
				SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				Editor edit = pref.edit();
				edit.putString("user", jsonUser.toString());			
				edit.commit(); 
			}
			
			@Override
			public void onFinish() {
				try
				{
					user = User.fromJson(new JSONObject(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("user", "")));
					ActionBar actionBar = getActionBar();
					actionBar.setTitle("@" + user.getScreenName());
				} catch (JSONException e) {
					e.printStackTrace();					
				}				
			}
			
		});	
	}
	
	// This method is linked to the Compose Tweet button in the action bar.
	public void onComposeTweetClick(MenuItem item) {
		// Pass the User information to the Compose Activity so we can display the screen name and show the profile image
		Intent i = new Intent(getApplicationContext(), ComposeTweetActivity.class);
		i.putExtra("profileImageUrl", user.getProfileImageUrl());
		i.putExtra("screen_name", user.getScreenName());
		startActivityForResult(i, 7);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		getHomeTimeLineAndRefresh();
	}
}
