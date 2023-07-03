package com.example.weatherforecast.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SQLHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "Weather.db";
    public static final String PROVINCE_CITY_TABLE_NAME = "ProvinceCity";
    public static final String CREATE_PROVINCE_CITY = "create table " + PROVINCE_CITY_TABLE_NAME + "(" +
            "id integer primary key autoincrement," +
            "province text," +
            "city text)";

    private Context mContext;

    public SQLHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext = context;
    }

    public List<Map<String, String>> getAllProvinceCity() {
        SQLiteDatabase database = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + PROVINCE_CITY_TABLE_NAME;
        Cursor cursor = database.rawQuery(selectQuery, null);
        List<Map<String, String>> list = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                Map<String, String> map = new HashMap<>();
                String id = cursor.getString(0);
                String province = cursor.getString(1);
                String city = cursor.getString(2);
                map.put("id", id);
                map.put("province", province);
                map.put("city", city);
                list.add(map);
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return list;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 创建名为"ProvinceCity"的表
        db.execSQL(CREATE_PROVINCE_CITY);
        insertProvinceCity(db,"北京", new String[]{"北京市"});
        insertProvinceCity(db,"上海", new String[]{"上海市"});
        insertProvinceCity(db,"深圳", new String[]{"深圳市"});
        insertProvinceCity(db,"天津", new String[]{"天津市"});
        insertProvinceCity(db,"重庆", new String[]{"重庆市"});
        insertProvinceCity(db,"广东省", new String[]{"广州市","深圳市","珠海市","汕头市","佛山市","韶关市","湛江市","肇庆市","江门市","茂名市","惠州市","梅州市","汕尾市","河源市","阳江市","清远市","东莞市","中山市","潮州市","揭阳市","云浮市"});
        insertProvinceCity(db,"海南省",new String[]{"海口市","三亚市","三沙市","儋州市","五指山市","琼海市","文昌市","万宁市","东方市"});
        insertProvinceCity(db,"陕西省",new String[]{"西安市","咸阳市","宝鸡市","渭南市","汉中市","安康市","商洛市","延安市","榆林市","铜川市"});
        insertProvinceCity(db,"甘肃省",new String[]{"兰州市","天水市","白银市","庆阳市","平凉市","酒泉市","张掖市","武威市","定西市","陇南市","金昌市"});
        insertProvinceCity(db,"青海省",new String[]{"西宁市","海东市"});
        insertProvinceCity(db,"四川省",new String[]{"成都市","绵阳市","德阳市","广元市","自贡市","攀枝花市","乐山市","南充市","内江市","遂宁市","广安市","泸州市","达州市","眉山市","宜宾市","雅安市","资阳市","巴中市"});
        insertProvinceCity(db,"云南省",new String[]{"昆明市","曲靖市","玉溪市","丽江市","昭通市","普洱市","临沧市","保山市","安宁市","宣威市"});
        insertProvinceCity(db,"贵州省",new String[]{"贵阳市","遵义市","安顺市","六盘水市","兴义市"});
        insertProvinceCity(db,"湖北省",new String[]{"武汉市","宜昌市","襄阳市","荆州市","恩施市","孝感市","黄冈市","十堰市","咸宁市","黄石市","仙桃市","随州市","天门市","荆门市","潜江市","鄂州市"});
        insertProvinceCity(db,"湖南省",new String[]{"长沙市","株洲市","湘潭市","衡阳市","岳阳市","郴州市","永州市","邵阳市","怀化市","常德市","益阳市","张家界市","娄底市","浏阳市","醴陵市","湘乡市","耒阳市","沅江市","涟源市","常宁市","吉首市"});
        insertProvinceCity(db,"江西省",new String[]{"南昌市","赣州市","上饶市","宜春市","景德镇市","新余市","九江市","萍乡市","抚州市","鹰潭市","吉安市"});
        insertProvinceCity(db,"广西壮族自治区",new String[]{"南宁市","桂林市","柳州市","梧州市","贵港市","玉林市","钦州市","北海市","防城港市","百色市","河池市","来宾市","贺州市","崇左市"});
        insertProvinceCity(db,"西藏自治区",new String[]{"拉萨市","日喀则市"});
        insertProvinceCity(db,"宁夏回族自治区",new String[]{"银川市","吴忠市","中卫市","石嘴山市","固原市"});
        insertProvinceCity(db,"新疆维吾尔自治区",new String[]{"乌鲁木齐市","克拉玛依市"});
        insertProvinceCity(db,"内蒙古自治区",new String[]{"呼和浩特市","包头市","赤峰市","鄂尔多斯市","通辽市","呼伦贝尔市","巴彦淖尔市","乌兰察布市","锡林郭勒盟","兴安盟","乌海市","阿拉善盟"});
        insertProvinceCity(db,"黑龙江省",new String[]{"哈尔滨市","大庆市","齐齐哈尔市","佳木斯市","伊春市","牡丹江市","鸡西市","黑河市","绥化市","鹤岗市","双鸭山市","七台河市","大兴安岭地区"});
        insertProvinceCity(db,"吉林省",new String[]{"长春市","吉林市","通化市","白山市","四平市","辽源市","松原市","白城市","延边朝鲜族自治州","公主岭市"});
        insertProvinceCity(db,"辽宁省",new String[]{"沈阳市","大连市","鞍山市","锦州市","抚顺市","营口市","盘锦市","朝阳市","丹东市","辽阳市","本溪市","葫芦岛市","铁岭市","阜新市"});
        insertProvinceCity(db,"台湾省",new String[]{"台北市","台南市","台中市","高雄市","基隆市","新竹市","嘉义市"});
        insertProvinceCity(db,"香港特别行政区",new String[]{"香港特别行政区"});
        insertProvinceCity(db,"澳门特别行政区",new String[]{"澳门特别行政区"});
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + PROVINCE_CITY_TABLE_NAME);
        onCreate(db);
    }

    private void insertProvinceCity(SQLiteDatabase db, String province, String[] cities) {
        for (String city : cities) {
            ContentValues values = new ContentValues();
            values.put("province", province);
            values.put("city", city);
            db.insert(PROVINCE_CITY_TABLE_NAME, null, values);
        }
    }
    public List<String> getAllProvinces() {
        List<String> provinces = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT DISTINCT province FROM ProvinceCity", null);
        if (cursor.moveToFirst()) {
            do {
                provinces.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return provinces;
    }

    public List<String> getCities(String province) {
        List<String> cities = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT city FROM ProvinceCity WHERE province = ?", new String[]{province});
        if (cursor.moveToFirst()) {
            do {
                cities.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return cities;
    }


}
