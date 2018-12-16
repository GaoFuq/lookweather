package com.lookweather.android;

import java.io.IOException;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import com.bumptech.glide.Glide;
import com.lookweather.android.gson.Forecast;
import com.lookweather.android.gson.Weather;
import com.lookweather.android.service.AutoUpdateService;
import com.lookweather.android.util.HttpUtil;
import com.lookweather.android.util.Utility;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class WeatherActivity extends Activity {
	/*
	 * 此处有坑，不多提了
	 */

	//public String getedWeatherId =null;// 我添加的代码

	//public static String currentWeatherId = null;// 我添加的代码
	//private static String getedWeatherId;
	public SwipeRefreshLayout refreshLayout;

	public DrawerLayout drawerLayout;

	private Button bt_nav;

	private ScrollView weatherLayout;

	private LinearLayout forecastLayout;

	private TextView titleCity, titleUpdateTime, degreeText, weatherInfoText,
			aqiText, pm25Text, comfortText, carWashText, sportText;

	private ImageView bingPicImg;
	private String mWeatherId;//修复bug

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		if (Build.VERSION.SDK_INT >= 21) {
			View decorView = getWindow().getDecorView();
			decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
					| View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
		}

		setContentView(R.layout.activity_weather);

		initView();
		
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		 String weatherString = prefs.getString("weather", null);
		if (weatherString != null) {
			// 读取缓存中的数据
			Weather weather = Utility.handleWeatherResponse(weatherString);
			mWeatherId = weather.basic.weatherId;
			showWeatherInfo(weather);
		} else {
			// 从服务器查询数据
			mWeatherId = getIntent().getStringExtra("weather_id");
			weatherLayout.setVisibility(View.INVISIBLE);
			requestWeather(mWeatherId);
		}
		refreshLayout
				.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
					@Override
					public void onRefresh() {
						requestWeather(mWeatherId);
					}
				});
		bt_nav.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				drawerLayout.openDrawer(GravityCompat.START);
			}
		});
		String bingPic = prefs.getString("bing_pic", null);
		if (bingPic != null) {
			Glide.with(this).load(bingPic).into(bingPicImg);
		} else {
			loadBingPic();
		}
	}

	

	/*
	 * 加载必应每日一图
	 */
	private void loadBingPic() {
		// TODO Auto-generated method stub
		String requestBingPic = "http://guolin.tech/api/bing_pic";
		HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {

			@Override
			public void onResponse(Call call, Response response)
					throws IOException {
				// TODO Auto-generated method stub
				final String bingPic = response.body().string();
				SharedPreferences.Editor editor = PreferenceManager
						.getDefaultSharedPreferences(WeatherActivity.this)
						.edit();
				editor.putString("bing_pic", bingPic);
				editor.apply();
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						Glide.with(WeatherActivity.this).load(bingPic)
								.into(bingPicImg);
					}
				});
			}

			@Override
			public void onFailure(Call arg0, IOException arg1) {
				// TODO Auto-generated method stub
				arg1.printStackTrace();
			}
		});
	}

	/*
	 * 根据weatherId到服务器查询
	 */
	void requestWeather(final String weatherId) {
		// TODO Auto-generated method stub
		String weatherUrl = "http://guolin.tech/api/weather?cityid="
				+ weatherId + "&key=76738de3c18241459c3e1a85e6ce975d";
		HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {

			@Override
			public void onResponse(Call call, Response response)
					throws IOException {
				// TODO Auto-generated method stub
				final String responseText = response.body().string();
				final Weather weather = Utility
						.handleWeatherResponse(responseText);
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						if (weather != null && "ok".equals(weather.status)) {
							SharedPreferences.Editor editor = PreferenceManager
									.getDefaultSharedPreferences(
											WeatherActivity.this).edit();

							editor.putString("weather", responseText);
							editor.apply();
							mWeatherId = weather.basic.weatherId;
							showWeatherInfo(weather);
						} else {
							Toast.makeText(WeatherActivity.this, "获取失败",
									Toast.LENGTH_SHORT).show();

						}
						refreshLayout.setRefreshing(false);
					}

				});

			}

			@Override
			public void onFailure(Call arg0, IOException e) {
				// TODO Auto-generated method stub
				e.printStackTrace();
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						Toast.makeText(WeatherActivity.this, "获取失败",
								Toast.LENGTH_SHORT).show();
						refreshLayout.setRefreshing(false);
					}
				});
			}
		});
		loadBingPic();
	}

	/*
	 * 显示天气信息
	 */
	private void showWeatherInfo(Weather weather) {
		//if ((weather != null) && ("ok".equals(weather.status))) {
			// TODO Auto-generated method stub
			String cityName = weather.basic.cityName;
			String updeTime = weather.basic.update.updateTime.split(" ")[1];
			String degree = weather.now.temperature + "°C";
			String weatherInfo = weather.now.more.info;
			titleCity.setText(cityName);
			titleUpdateTime.setText(updeTime);
			degreeText.setText(degree);
			weatherInfoText.setText(weatherInfo);
			forecastLayout.removeAllViews();
			for (Forecast forecast : weather.forecastList) {

				View view = LayoutInflater.from(this).inflate(
						R.layout.weather_forecast_item, forecastLayout, false);
				TextView dateText = (TextView) view
						.findViewById(R.id.tv_forecast_date);
				TextView infoText = (TextView) view
						.findViewById(R.id.tv_forecast_info);
				TextView maxText = (TextView) view
						.findViewById(R.id.tv_forecast_max);
				TextView minText = (TextView) view
						.findViewById(R.id.tv_forecast_min);
				dateText.setText(forecast.date);
				infoText.setText(forecast.more.info);
				maxText.setText(forecast.temperature.max);
				minText.setText(forecast.temperature.min);
				forecastLayout.addView(view);
			}
			if (weather.aqi != null) {
				aqiText.setText(weather.aqi.city.aqi);
				pm25Text.setText(weather.aqi.city.pm25);
			}
			String comfort = "舒适度:" + weather.suggestion.comfort.info;
			String carWash = "洗车指数:" + weather.suggestion.carWash.info;
			String sport = "运动指数:" + weather.suggestion.sport.info;
			comfortText.setText(comfort);
			carWashText.setText(carWash);
			sportText.setText(sport);
			weatherLayout.setVisibility(View.VISIBLE);

			Intent intent = new Intent(this, AutoUpdateService.class);
			startService(intent);
		} 
	//}

	private void initView() {
		// TODO Auto-generated method stub
		weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
		forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
		titleCity = (TextView) findViewById(R.id.tv_title_city);
		titleUpdateTime = (TextView) findViewById(R.id.tv_title_update_time);
		degreeText = (TextView) findViewById(R.id.tv_degree);
		weatherInfoText = (TextView) findViewById(R.id.tv_weather_info);
		aqiText = (TextView) findViewById(R.id.tv_weather_aqi);
		pm25Text = (TextView) findViewById(R.id.tv_weather_aqi_pm25);
		comfortText = (TextView) findViewById(R.id.tv_weather_suggestion_comfort_text);
		carWashText = (TextView) findViewById(R.id.tv_weather_suggestion_car_wash_text);
		sportText = (TextView) findViewById(R.id.tv_weather_suggestion_sport_text);
		bingPicImg = (ImageView) findViewById(R.id.iv_bing_pic_img);
		refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
		refreshLayout.setColorSchemeResources(R.color.holo_green_dark,
				R.color.holo_orange_dark, R.color.holo_blue_dark,
				R.color.holo_red_dark);
		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		bt_nav = (Button) findViewById(R.id.bt_nav);
		bt_nav.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				drawerLayout.openDrawer(GravityCompat.START);
			}
		});
	}

}
