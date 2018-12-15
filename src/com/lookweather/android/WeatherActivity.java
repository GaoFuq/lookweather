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
	 * 经过检查，代码无误。 但是测试结果是只有刚安装后第一次运行，查询的数据才是匹配的，之后查询的数据都是第一次查到的结果。
	 * 
	 * 发现问题（个人的看法，虽然是郭霖老师的教材，我觉得他这里还是有点问题的）
	 * 
	 * 我自己修改好了，然后接着看后面的才发现原来这个坑是已经挖好了的。。。
	 * 
	 * (@#$%^&*....)
	 */
	//private String getedWeatherId = null;//我添加的代码
	//public static String currentWeatherId = null;//我添加的代码
	
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
			//让活动的布局显示在状态栏上面
			decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN |View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
			//将状态栏设置成透明
			//getWindow().setStatusBarColor(color.transparent);没有这个方法。因为我继承的是Activity，不是AppCompatActivity。
		}
		
		setContentView(R.layout.activity_weather);

		// 初始化控件
		initView();

		//getedWeatherId = getIntent().getStringExtra("weather_id");//我添加的代码

		// 读取数据
		readData();
	}

	private void readData() {
		
		// TODO Auto-generated method stub
		//if (getedWeatherId.equals(currentWeatherId)) {//我添加的代码
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
				// 有缓存时，直接解析缓存的数据
				Weather weather = Utility.handleWeatherResponse(weatherString);
				weatherId=weather.basic.weatherId;
				showWeatherInfo(weather);
			} else {
				// 无缓存时去服务器查询
				weatherId = getIntent().getStringExtra("weather_id");
			//	Log.d("得到的weatherId", currentWeatherId);
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
	 * 加载必应每日一图
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
	 * 根据天气weatherId查询天气数据
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
							Toast.makeText(WeatherActivity.this, "获取天气信息失败",
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
						Toast.makeText(WeatherActivity.this, "获取天气信息失败",
								Toast.LENGTH_SHORT).show();
						refreshLayout.setRefreshing(false);
					}
				});
			}
		});
		loadBingPic();
	}

	/*
	 * 处理并展示Weather实体类中的数据
	 */
	private void showWeatherInfo(Weather weather) {
		// TODO Auto-generated method stub
		String cityName = weather.basic.cityName;
		String updeTime = weather.basic.update.updateTime;// ?
		String degree = weather.now.temperature + "°C";
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
		String comfort = "舒适度:" + weather.suggestion.comfort.info;
		String carWash = "洗车指数:" + weather.suggestion.carWash.info;
		String sport = "运动指数:" + weather.suggestion.sport.info;
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
	 * 监听软件退出
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
			Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
		}
	}

}
