package com.lookweather.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

public class WelcomeActivity extends Activity{
@Override
protected void onCreate(Bundle savedInstanceState) {
	// TODO Auto-generated method stub
	super.onCreate(savedInstanceState);
	requestWindowFeature(Window.FEATURE_NO_TITLE);
	getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
			WindowManager.LayoutParams.FLAG_FULLSCREEN);

	setContentView(R.layout.activity_welcome);
	
	new Handler().postDelayed(new Runnable() {
		@Override
		public void run() {

			finish();
			startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
		}
	}, 2500);
}
}


