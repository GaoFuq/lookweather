package com.lookweather.android.gson;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class Weather {

	/*
	 * 返回的状态码
	 */
	public String status;
	
	public Basic basic;
	
	public AQI aqi;
	
	public Now now;
	
	public Suggestion suggestion;
	
	
	/*
	 * 每日天气预测
	 * 是一个数组，这里用List集合来作为容器
	 * 集合里面每一个元素都包含了 Basic,AQI,Now,Suggestion
	 * 
	 */	
	@SerializedName("daily_forecast")
	public List<Forecast> forecastList;
}
