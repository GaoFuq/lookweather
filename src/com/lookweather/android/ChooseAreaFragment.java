package com.lookweather.android;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import org.litepal.crud.DataSupport;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.lookweather.android.db.City;
import com.lookweather.android.db.County;
import com.lookweather.android.db.Province;
import com.lookweather.android.util.HttpUtil;

import com.lookweather.android.util.Utility;

public class ChooseAreaFragment extends Fragment {

	public static final int LEVEL_PROVINCE = 0;
	public static final int LEVEL_CITY = 1;
	public static final int LEVEL_COUNTY = 2;
	private ProgressDialog progressDialog;
	private TextView tv_title;
	private Button bt_back;
	private ListView listView;
	private ArrayAdapter<String> adapter;

	// 要显示的数据
	private List<String> dataList = new ArrayList<>();

	/*
	 * 省列表
	 */
	private List<Province> provincesList;
	/*
	 * 市列表
	 */
	private List<City> cityList;
	/*
	 * 县列表
	 */
	private List<County> countyList;

	/*
	 * 选中的省份
	 */
	private Province selectedProvince;

	/*
	 * 选中的市
	 */
	private City selectedCity;

	/*
	 * 当前选中的级别
	 */
	private int currentLevel;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.choose_area, container, false);
		tv_title = (TextView) view.findViewById(R.id.tv_title);
		bt_back = (Button) view.findViewById(R.id.bt_back);
		listView = (ListView) view.findViewById(R.id.list_view);
		adapter = new ArrayAdapter<>(getActivity(),
				android.R.layout.simple_list_item_1, dataList);
		listView.setAdapter(adapter);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		queryProvinces();
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				if (currentLevel == LEVEL_PROVINCE) {
					selectedProvince = provincesList.get(position);
					queryCities();
				} else if (currentLevel == LEVEL_CITY) {
					selectedCity = cityList.get(position);
					queryCountis();
				} else if (currentLevel == LEVEL_COUNTY) {
					String weatherId = countyList.get(position).getWeatherId();
					Intent inten = new Intent(getActivity(),
							WeatherActivity.class);
					inten.putExtra("weather_id", weatherId);
					Log.d("传入的weatherId", weatherId);//这里没问题
					startActivity(inten);
					
				}
			}
		});

		bt_back.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (currentLevel == LEVEL_COUNTY) {
					queryCities();
				} else if (currentLevel == LEVEL_CITY) {
					queryProvinces();
				}
			}
		});

	}

	/*
	 * 查询全国所有的省，优先从数据库查询，如果没有查询到，再去服务器上查询
	 */
	private void queryProvinces() {
		// TODO Auto-generated method stub
		tv_title.setText("中国");
		bt_back.setVisibility(View.GONE);
		provincesList = DataSupport.findAll(Province.class);
		if (provincesList.size() > 0) {
			dataList.clear();
			for (Province province : provincesList) {
				dataList.add(province.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			currentLevel = LEVEL_PROVINCE;
		} else {
			String address = "http://guolin.tech/api/china";
			queryFromService(address, "province");
		}
	}

	/*
	 * 查询全国所有的市，优先从数据库查询，如果没有查询到，再去服务器上查询
	 */
	private void queryCities() {
		// TODO Auto-generated method stub
		tv_title.setText(selectedProvince.getProvinceName());
		bt_back.setVisibility(View.VISIBLE);
		cityList = DataSupport.where("provinceid=?",
				String.valueOf(selectedProvince.getId())).find(City.class);
		if (cityList.size() > 0) {
			dataList.clear();// 清空ListView之前显示的数据
			for (City city : cityList) {
				dataList.add(city.getCityName());// 把当前要显示的数据添加到ListView中
			}
			adapter.notifyDataSetChanged();// 刷新ListView
			listView.setSelection(0);
			currentLevel = LEVEL_CITY;
		} else {
			int provinceCode = selectedProvince.getProvinceCode();
			String address = "http://guolin.tech/api/china/" + provinceCode;
			queryFromService(address, "city");
		}
	}

	/*
	 * 查询全国所有的县，优先从数据库查询，如果没有查询到，再去服务器上查询
	 */
	private void queryCountis() {
		// TODO Auto-generated method stub
		tv_title.setText(selectedCity.getCityName());
		bt_back.setVisibility(View.VISIBLE);
		countyList = DataSupport.where("cityid=?",
				String.valueOf(selectedCity.getId())).find(County.class);
		if (countyList.size() > 0) {
			dataList.clear();
			for (County county : countyList) {
				dataList.add(county.getCountyName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			currentLevel = LEVEL_COUNTY;
		} else {
			int provinceCode = selectedProvince.getProvinceCode();
			int cityCode = selectedCity.getCityCode();
			String address = "http://guolin.tech/api/china/" + provinceCode
					+ "/" + cityCode;
			queryFromService(address, "county");
		}
	}

	/*
	 * 根据传入的地址和类型从服务器上查询省市县的数据
	 */
	private void queryFromService(String address, final String type) {
		// TODO Auto-generated method stub
		showProgressDialog();
		HttpUtil.sendOkHttpRequest(address, new Callback() {

			@Override
			// 查询成功的回调方法
			public void onResponse(Call call, Response response)
					throws IOException {
				// TODO Auto-generated method stub
				String responseText = response.body().string();
				boolean result = false;
				if ("province".equals(type)) {
					result = Utility.handleProvinceResponse(responseText);
				} else if ("city".equals(type)) {
					result = Utility.handleCityResponse(responseText,
							selectedProvince.getId());
				} else if ("county".equals(type)) {
					result = Utility.handleCountyResponse(responseText,
							selectedCity.getId());
				}
				if (result) {
					getActivity().runOnUiThread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							closeProgressDialog();
							if ("province".equals(type)) {
								queryProvinces();
							} else if ("city".equals(type)) {
								queryCities();
							} else if ("county".equals(type)) {
								queryCountis();
							}
						}
					});
				}
			}

			@Override
			// 查询失败时的回调方法
			public void onFailure(Call arg0, IOException arg1) {
				// TODO Auto-generated method stub
				getActivity().runOnUiThread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						closeProgressDialog();
						Toast.makeText(getActivity(), "ss", Toast.LENGTH_SHORT)
								.show();
					}
				});
			}
		});
	}

	private void showProgressDialog() {
		// TODO Auto-generated method stub
		if (progressDialog == null) {
			progressDialog = new ProgressDialog(getActivity());
			progressDialog.setTitle("你好~");
			progressDialog.setMessage("努力加载中...");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}

	private void closeProgressDialog() {
		// TODO Auto-generated method stub
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
	}

}
