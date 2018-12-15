package com.lookweather.android.gson;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class Weather {

	/*
	 * 返回结果状态码
	 */
	public String status;
	
	public Basic basic;
	
	public AQI aqi;
	
	public Now now;
	
	public Suggestion suggestion;
	
	
	/*
	 * 每天天气数据的集合。
	 * 集合的一个元素代表一天的天气。
	 * 一天的天气（一个元素）包含上面的5大版块。
	 */	
	@SerializedName("daily_forecast")
	public List<Forecast> forecastList;
}
