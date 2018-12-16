package com.lookweather.android.gson;

import com.google.gson.annotations.SerializedName;

public class Basic {
	
	@SerializedName("city")
	public String cityName;//使JSON数据和Java相映射

	@SerializedName("id")
	public String weatherId;
	
	public Update update;
	
	public class Update{
		
		@SerializedName("loc")
		public String updateTime;
	}



}
