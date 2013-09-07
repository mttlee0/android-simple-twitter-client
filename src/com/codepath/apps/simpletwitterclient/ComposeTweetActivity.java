package com.codepath.apps.simpletwitterclient;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.ImageLoader;

// Controller for the compose tweet screen
public class ComposeTweetActivity extends Activity {
	ImageView ivUserProfilePic;
	TextView tvUserScreenName;
	EditText etTweetBody;
	TextView tvCharacterCount;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_compose_tweet);    
		setupViews();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.compose_tweet, menu);
		return true;
	}

	private void setupViews() {
		ivUserProfilePic = (ImageView) findViewById(R.id.ivUserProfilePic);
		tvUserScreenName = (TextView) findViewById(R.id.tvUserScreenName);
		etTweetBody = (EditText) findViewById(R.id.etTweetBody);
		tvCharacterCount = (TextView)findViewById(R.id.tvCharacterCount);
		
        ImageLoader.getInstance().displayImage(getIntent().getStringExtra("profileImageUrl"), ivUserProfilePic);
        tvUserScreenName.setText("@" + getIntent().getStringExtra("screen_name"));
        
        // TODO: My failed attempt at bringing up the soft keyboard
        etTweetBody.setImeOptions(EditorInfo.IME_ACTION_DONE);        
        etTweetBody.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);                    
                    imm.showSoftInput(v,InputMethodManager.SHOW_IMPLICIT);
                }
                else {
                	InputMethodManager imm =(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                	imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        });
        
        // Add a text change listener to the tweet body text so we can show how close they are to the character limit 
        etTweetBody.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable textView) {				
				int numCharacters = etTweetBody.length();
				if (numCharacters <= 140) {
					tvCharacterCount.setText(String.valueOf(140 - numCharacters) + " characters remaining");
				} else {
					tvCharacterCount.setText(String.valueOf(140 - numCharacters) + " too many characters");
				}
													
				return;
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				return;
			}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int count, int after) {
				return;
			}
		});		
	}
	
	// This method is attached to the onClick for "Tweet" button
	public void onPostTweetClick(View view) {
		RequestParams params = new RequestParams();
		params.put("status", etTweetBody.getText().toString());	
		
		// Use the Rest client to submit a HTTP POST to Twitter. 
		// TODO: Ask Tim+Nathan if onFinish() or onSuccess() is the better place to close the Activity and return th result
		TwitterApp.getRestClient().postTweet(params, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(JSONObject jsonTweets) {
				Toast.makeText(getApplicationContext(), "Tweeted!!!", Toast.LENGTH_SHORT).show();
			}
			
			@Override
			public void onFinish() {
				Intent data = new Intent();
				if (getParent() == null) {
				    setResult(Activity.RESULT_OK, data);
				} else {
				    getParent().setResult(Activity.RESULT_OK, data);
				}
				finish();
			}
			
			@Override
			public void onFailure(Throwable error, String content) {
				Toast.makeText(getApplicationContext(), "Twitter Whale Fail!!!", Toast.LENGTH_SHORT).show();
			}
		});
	}
}
