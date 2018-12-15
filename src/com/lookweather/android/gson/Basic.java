package com.lookweather.android.gson;

import com.google.gson.annotations.SerializedName;

public class Basic {
	
	@SerializedName("city")
	public String cityName;//使JSON字段与Java字段建立映射

	@SerializedName("id")
	public String weatherId;
	
	public Update update;
	
	public class Update{
		
		@SerializedName("loc")
		public String updateTime;
	}



}
