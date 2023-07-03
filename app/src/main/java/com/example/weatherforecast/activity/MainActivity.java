package com.example.weatherforecast.activity;


import android.annotation.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;

import androidx.appcompat.app.*;
import androidx.swiperefreshlayout.widget.*;

import com.google.gson.*;
import com.example.weatherforecast.bean.*;
import com.example.weatherforecast.helper.*;
import com.example.weatherforecast.*;

import java.util.*;

import okhttp3.*;

public class MainActivity extends AppCompatActivity {
    private EditText editText;
    private ListView listview;
    private Button search_btn;
    private Button star_btn;
    private Button ref_btn;
    private TextView update_time;
    Spinner spinnerProvince;
    Spinner spinnerCity;
    SQLHelper sqlHelper;
    private SwipeRefreshLayout swipeRefreshLayout;  //下拉刷新
    private SharedPreferences cacheShrPre;   // 数据缓存
    private SharedPreferences.Editor cacheEditor;
    private SharedPreferences starShaPre;    // 城市缓存
    private SharedPreferences.Editor starEditor;
    private List<String> weatherList = new ArrayList<>();   // 天气数据集合
    private TextView city_name;
    List<Casts> forecastList = new ArrayList<>();    // 未来三天天气数据集合

    String key = "d010cdd25c2a77f8d31ee1f3642d1ae4";

    private OkHttpClient okHttpClient = new OkHttpClient();   // okHttp
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init(); //初始化
        // 缓存本次查询的城市
        cacheShrPre = getSharedPreferences("WeatherData", MODE_PRIVATE);
        cacheEditor = cacheShrPre.edit();
        // 启动时清空缓存
        cacheEditor.clear();
        cacheEditor.apply();

        // Star：收藏的城市
        starShaPre = getSharedPreferences("StarCity", Context.MODE_PRIVATE);
        starEditor = starShaPre.edit();
        String starCities = starShaPre.getString("city", "");
        // 如果存在缓存数据的话，获取并显示（这里利用 HashSet 去重）
        if (!starCities.isEmpty()) {
            weatherList.addAll(gson.fromJson(starCities, List.class));
            weatherList = new ArrayList<>(new HashSet<>(weatherList));
            updateStarButtonList(); // 更新收藏列表的UI
        }

        // 初始化省份下拉框
        List<String> provinces = sqlHelper.getAllProvinces();
        ArrayAdapter<String> adapterProvince = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, provinces);
        adapterProvince.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProvince.setAdapter(adapterProvince);

        spinnerProvince.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedProvince = parent.getItemAtPosition(position).toString();
                // 查询该省份的所有城市
                List<String> cities = sqlHelper.getCities(selectedProvince);
                // 设置给城市下拉框
                ArrayAdapter<String> adapterCity = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, cities);
                adapterCity.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(adapterCity);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCity = parent.getItemAtPosition(position).toString();
                // 设置到EditText中
                editText.setText(selectedCity);
                getWeatherForecast(selectedCity);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    // @SuppressLint("HandlerLeak") 禁止在此处显示对Handler内部类的警告，因为这种情况在Android中经常发生，且不会造成实际问题。
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            // 清空页面数据
            listview.setAdapter(null);
            if (msg.what == 0) {
                Msg.shorts(MainActivity.this, "城市不存在！");
            } else if (msg.what == 1) {
                Msg.shorts(MainActivity.this, "服务器无响应！");
            } else if (msg.what == 2) {
                Msg.shorts(MainActivity.this, "未知错误~");
            } else if (msg.what == 3) {
                List<Map<String, String>> list = (List<Map<String, String>>) msg.obj;   // 获取数据
                final SimpleAdapter simpleAdapter = new SimpleAdapter(MainActivity.this, list, R.layout.activity_listview_item
                        , new String[]{"day", "weather", "temp", "wind"}
                        , new int[]{R.id.day, R.id.weather, R.id.temperature, R.id.wind});
                listview.setAdapter(simpleAdapter); // 为ListView绑定适配器
                Msg.shorts(MainActivity.this, "查询成功");
            }
        }
    };

    // 代码先根据城市名获取城市编码，再根据城市编码获取天气信息（高德开发平台请求天气查询API中区域编码 adcode 是必须项，使用编码服务获取区域编码）
    private void getWeatherForecast(final String city) {
        // 使用 cachePreShr 读取缓存数据，如果存在则直接显示
        String cacheCity = cacheShrPre.getString(city, "");
        if (!cacheCity.isEmpty()) {
            // 存在缓存数据，获取并显示
            List weatherList = gson.fromJson(cacheCity, List.class);
            painting(weatherList);
            runOnUiThread(() -> city_name.setText(city.contains("市") ? city : city + "市"));
        } else {
            String forecastURL = "https://restapi.amap.com/v3/weather/weatherInfo?key=" + key + "&city=" + city + "&extensions=all&out=json";
            final Request requestForecast = new Request.Builder().url(forecastURL).get().build();

            // 开启子线程
            new Thread(() -> {
                try (Response responseForecast = okHttpClient.newCall(requestForecast).execute()) {
                    //请求成功
                    if (responseForecast.isSuccessful()) {
                        String resultForecast = responseForecast.body().string();
                        List<Map<String, String>> weatherList = new ArrayList<>();  // 存储解析json字符串得到的天气信息
                        Weather weather = gson.fromJson(resultForecast, Weather.class);     // Gson解析

                        // 定义日期数组
                        String[] dates = {"今天", "明天", "后天", "大后天"};

                        // 切换至主线程设置时间（UI）
                        runOnUiThread(() -> {
                            try{
                                update_time.setText("更新时间:"+weather.getForecasts().get(0).getReporttime());
                                city_name.setText(weather.getForecasts().get(0).getCity());
                            }catch (Exception e){
                                e.printStackTrace();
                                Toast.makeText(MainActivity.this, "输入的城市名不正确，请检查", Toast.LENGTH_LONG).show();
                            }
                        });

                        // forecastList 是天气预报信息集合，每天的天气信息是一个对象，所以需要遍历集合，获取每天的天气信息

                            forecastList = weather.getForecasts().get(0).getCasts();

                        for (int i = 0; i < forecastList.size() && i < dates.length; i++) {
                            Casts cast = forecastList.get(i);
                            Map<String, String> map = new HashMap<>();

                            map.put("day", dates[i]);  // 日期

                            map.put("weather", cast.getDayweather() +" / "+  cast.getNightweather());     // 白天天气
                            map.put("temp", cast.getDaytemp() + "° / " + cast.getNighttemp() + "°");     // 温度
                            map.put("wind", cast.getDaywind() +"风"+ cast.getDaypower()+ "级" + " / " + cast.getNightwind() + "风" + cast.getNightpower() + "级");    // 风向
                            weatherList.add(map);
                        }
                        painting(weatherList);

                        // 将 weatherList 写入缓存
                        cacheEditor.putString(city, gson.toJson(weatherList));
                        cacheEditor.apply();
                    }
                } catch (Exception e) {
                    Message message = Message.obtain();
                    message.what = 0;
                    handler.sendMessage(message);
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private void painting(List<Map<String, String>> weatherList) {
        Message message = Message.obtain();
        message.what = 3;   // 3表示请求成功
        message.obj = weatherList;
        handler.sendMessage(message);
    }

    // 保存关注的城市
    private void saveOrUnsaveStar() {
        // 获取输入的城市名
        String city = editText.getText().toString();
        city = city.replaceFirst("市$", "");  // 删除末尾的"市"字
        // 正则表达式验证城市名是否合法：2-10个汉字
        if (!city.matches("^[\\u4e00-\\u9fa5]{2,10}$")) {
            Msg.shorts(MainActivity.this, "城市不存在！");
            return;
        } else if (city.isEmpty()) {
            Msg.shorts(MainActivity.this, "请输入城市名");
            return;
        }

        if (weatherList.contains(city)) {  // 如果已经收藏了该城市
            unsaveStar(city);  // 取消收藏
        } else {
            saveStar(city);  // 收藏
        }
    }

    private void saveStar(String city) {
        weatherList.add(city);
        starEditor.putString("city", gson.toJson(new ArrayList<>(new HashSet<>(weatherList))));
        starEditor.apply();
        Msg.shorts(MainActivity.this, "关注成功");
        updateStarButtonList(); // 更新收藏列表的UI
    }

    private void unsaveStar(String city) {
        weatherList.remove(city);
        starEditor.putString("city", gson.toJson(new ArrayList<>(new HashSet<>(weatherList))));
        starEditor.apply();
        Msg.shorts(MainActivity.this, "取消关注成功");
        updateStarButtonList(); // 更新收藏列表的UI
    }

    private void updateStarButtonList() {
        // 获取存放按钮的LinearLayout
        LinearLayout starButtonList = findViewById(R.id.start_btn_list);
        // 设置LinearLayout的方向为垂直
        starButtonList.setOrientation(LinearLayout.VERTICAL);
        // 清空所有的按钮
        starButtonList.removeAllViews();

        LinearLayout rowLayout = null;
        int buttonCount = 0;

        for (String city : weatherList) {
            // 每四个按钮换一行
            if (buttonCount % 4 == 0) {
                // 创建新的LinearLayout
                rowLayout = new LinearLayout(this);
                rowLayout.setOrientation(LinearLayout.HORIZONTAL);
                starButtonList.addView(rowLayout);
            }

            // 创建新的Button
            Button button = new Button(this);
            // 设置按钮的文字为城市名
            button.setText(city);
            // 设置按钮的宽度和权重
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            button.setLayoutParams(lp);
            // 设置按钮的点击事件
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 在这里处理点击事件，比如查询这个城市的天气
                    String selectedCity = city;
                    editText.setText(selectedCity);
                    getWeatherForecast(selectedCity);
                    updateStarButton();  // 更新收藏按钮的文本
                }
            });
            // 将按钮添加到当前行的LinearLayout中
            if (rowLayout != null) {
                rowLayout.addView(button);
            }
            // 增加按钮数量
            buttonCount++;
        }

        updateStarButton();  // 更新收藏按钮的文本
    }

    private void updateStarButton() {
        String city = editText.getText().toString();
        city = city.replaceFirst("市$", "");
        Button starButton = findViewById(R.id.start_btn);
        if (weatherList.contains(city)) {
            starButton.setText("取消");
        } else {
            starButton.setText("收藏");
        }
    }


    public void refreshDiaries() {
        String address = editText.getText().toString();
        getWeatherForecast(address);
        swipeRefreshLayout.setRefreshing(false);    // 刷新操作完成，隐藏刷新进度条
    }

    private void init() {
        editText = findViewById(R.id.editText);
        search_btn = findViewById(R.id.search_btn);
        star_btn = findViewById(R.id.start_btn);
        city_name = findViewById(R.id.city_name);
        update_time = findViewById(R.id.update_time);
        listview = findViewById(R.id.listview);
        ref_btn = findViewById(R.id.ref_btn);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        listview.setCacheColorHint(4); // 空间换时间
        swipeRefreshLayout.setOnRefreshListener(() -> refreshDiaries());         // 下拉刷新监听器
        ref_btn.setOnClickListener(e -> refreshDiaries());
        search_btn.setOnClickListener(v -> {
            String address = editText.getText().toString();
            getWeatherForecast(address);
        });
        star_btn.setOnClickListener(v -> saveOrUnsaveStar());
        sqlHelper = new SQLHelper(MainActivity.this, "weather.db", null, 1);
        spinnerProvince = (Spinner) findViewById(R.id.province_spinner);
        spinnerCity = (Spinner) findViewById(R.id.city_spinner);
    }
}
