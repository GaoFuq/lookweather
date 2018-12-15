package com.lookweather.android.gson;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class Weather {

	/*
	 * ���ؽ��״̬��
	 */
	public String status;
	
	public Basic basic;
	
	public AQI aqi;
	
	public Now now;
	
	public Suggestion suggestion;
	
	
	/*
	 * ÿ���������ݵļ��ϡ�
	 * ���ϵ�һ��Ԫ�ش���һ���������
	 * һ���������һ��Ԫ�أ����������5���顣
	 */	
	@SerializedName("daily_forecast")
	public List<Forecast> forecastList;
}
