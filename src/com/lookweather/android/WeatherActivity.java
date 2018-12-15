package com.lookweather.android;

import java.io.IOException;
import java.util.Date;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import com.bumptech.glide.Glide;
import com.lookweather.android.gson.Forecast;
import com.lookweather.android.gson.Weather;
import com.lookweather.android.util.HttpUtil;
import com.lookweather.android.util.Utility;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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
	 * ������飬�������� ���ǲ��Խ����ֻ�иհ�װ���һ�����У���ѯ�����ݲ���ƥ��ģ�֮���ѯ�����ݶ��ǵ�һ�β鵽�Ľ����
	 * 
	 * �������⣨���˵Ŀ�������Ȼ�ǹ�����ʦ�Ľ̲ģ��Ҿ��������ﻹ���е�����ģ�
	 * 
	 * ���Լ��޸ĺ��ˣ�Ȼ����ſ�����Ĳŷ���ԭ����������Ѿ��ں��˵ġ�����
	 * 
	 * (@#$%^&*....)
	 */
	//private String getedWeatherId = null;//����ӵĴ���
	//public static String currentWeatherId = null;//����ӵĴ���
	
	public SwipeRefreshLayout refreshLayout;
	
	public DrawerLayout drawerLayout;
	
	private Button bt_nav;
	
	private ScrollView weatherLayout;

	private LinearLayout forecastLayout;

	private TextView titleCity, titleUpdateTime, degreeText, weatherInfoText,
			aqiText, pm25Text, comfortText, carWashText, sportText;

	private ImageView bingPicImg;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		if(Build.VERSION.SDK_INT>=21){
			View decorView=getWindow().getDecorView();
			//�û�Ĳ�����ʾ��״̬������
			decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN |View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
			//��״̬�����ó�͸��
			//getWindow().setStatusBarColor(color.transparent);û�������������Ϊ�Ҽ̳е���Activity������AppCompatActivity��
		}
		
		setContentView(R.layout.activity_weather);

		// ��ʼ���ؼ�
		initView();

		//getedWeatherId = getIntent().getStringExtra("weather_id");//����ӵĴ���

		// ��ȡ����
		readData();
	}

	private void readData() {
		
		// TODO Auto-generated method stub
		//if (getedWeatherId.equals(currentWeatherId)) {//����ӵĴ���
			SharedPreferences sharedPreferences = PreferenceManager
					.getDefaultSharedPreferences(this);
			String bingPic=sharedPreferences.getString("bing_pic", null);
			if(bingPic!=null){
				Glide.with(this).load(bingPic).into(bingPicImg);
			}else{
				loadBingPic();
			}
			String weatherString = sharedPreferences.getString(
					"WeatherId", null);
			final String weatherId;
			if (weatherString != null) {
				// �л���ʱ��ֱ�ӽ������������
				Weather weather = Utility.handleWeatherResponse(weatherString);
				weatherId=weather.basic.weatherId;
				showWeatherInfo(weather);
			} else {
				// �޻���ʱȥ��������ѯ
				weatherId = getIntent().getStringExtra("weather_id");
			//	Log.d("�õ���weatherId", currentWeatherId);
				weatherLayout.setVisibility(View.INVISIBLE);
				requestWeather(weatherId);
			}
			refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
				
				@Override
				public void onRefresh() {
					// TODO Auto-generated method stub
					requestWeather(weatherId);
				}
			});
	}

	
	/*
	 * ���ر�Ӧÿ��һͼ
	 */
	private void loadBingPic() {
		// TODO Auto-generated method stub
		String requestBingPic="http://guolin.tech/api/bing_pic";
		HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
			
			@Override
			public void onResponse(Call call, Response response) throws IOException {
				// TODO Auto-generated method stub
				final String bingPic=response.body().string();
				SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
				editor.putString("bing_pic", bingPic);
				editor.apply();
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
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
	 * ��������weatherId��ѯ��������
	 */
	void requestWeather(final String weatherId) {
		// TODO Auto-generated method stub
		// currentWeatherId=WeatherId;
		
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
							editor.putString("WeatherId", responseText);
							editor.apply();
							showWeatherInfo(weather);
						} else {
							Toast.makeText(WeatherActivity.this, "��ȡ������Ϣʧ��",
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
						Toast.makeText(WeatherActivity.this, "��ȡ������Ϣʧ��",
								Toast.LENGTH_SHORT).show();
						refreshLayout.setRefreshing(false);
					}
				});
			}
		});
		loadBingPic();
	}

	/*
	 * ����չʾWeatherʵ�����е�����
	 */
	private void showWeatherInfo(Weather weather) {
		// TODO Auto-generated method stub
		String cityName = weather.basic.cityName;
		String updeTime = weather.basic.update.updateTime;// ?
		String degree = weather.now.temperature + "��C";
		String weatherInfo = weather.now.more.info;
		Log.d("cityName", cityName);
		Log.d("updeTime", updeTime);
		Log.d("degree", degree);
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
		String comfort = "���ʶ�:" + weather.suggestion.comfort.info;
		String carWash = "ϴ��ָ��:" + weather.suggestion.carWash.info;
		String sport = "�˶�ָ��:" + weather.suggestion.sport.info;
		comfortText.setText(comfort);
		carWashText.setText(carWash);
		sportText.setText(sport);
		weatherLayout.setVisibility(View.VISIBLE);
	}

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
		bingPicImg=(ImageView) findViewById(R.id.iv_bing_pic_img);
		refreshLayout=(SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
		//refreshLayout.setColorSchemeResources(Color.WHITE, Color.YELLOW, Color.RED, Color.GREEN);
		drawerLayout=(DrawerLayout) findViewById(R.id.drawer_layout);
		bt_nav=(Button) findViewById(R.id.bt_nav);
		bt_nav.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				drawerLayout.openDrawer(GravityCompat.START);
			}
		});
	}

	/**
	 * ��������˳�
	 */
	private long mLastBackTime = 0;
	private long TIME_DIFF = 2 * 1000;

	@Override
	public void onBackPressed() {
		startActivity(new Intent(this, MainActivity.class));
		finish();
		long now = new Date().getTime();
		if (now - mLastBackTime < TIME_DIFF) {

		} else {
			mLastBackTime = now;
			Toast.makeText(this, "�ٰ�һ���˳�����", Toast.LENGTH_SHORT).show();
		}
	}

}
