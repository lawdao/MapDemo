package example.fussen.mapdemo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;

import example.fussen.mapdemo.R;
import example.fussen.mapdemo.utils.MapUtils;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button send;
    private TextView tv_city;
    private TextView tv_lon;
    private TextView tv_lat;
    private TextView tv_location;
    private TextView tv_poi;

    private double longitude;
    private double latitude;
    private String cityCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        send = (Button) findViewById(R.id.btn_send);

        tv_city = (TextView) findViewById(R.id.city);
        tv_lon = (TextView) findViewById(R.id.lon);
        tv_lat = (TextView) findViewById(R.id.lat);
        tv_location = (TextView) findViewById(R.id.location);
        tv_poi = (TextView) findViewById(R.id.poi);

        send.setOnClickListener(this);


        //获取定位信息
        MapUtils mapUtils = new MapUtils();
        mapUtils.getLonLat(this, new MyLonLatListener());
    }


    class MyLonLatListener implements MapUtils.LonLatListener {

        @Override
        public void getLonLat(AMapLocation amapLocation) {

            if (amapLocation != null) {
                if (amapLocation.getErrorCode() == 0) {
                    //定位成功回调信息，设置相关消息
                    amapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见定位类型表
                    latitude = amapLocation.getLatitude();//获取纬度
                    longitude = amapLocation.getLongitude();//获取经度

                    cityCode = amapLocation.getCityCode();

                    tv_lat.setText("当前纬度：" + latitude);
                    tv_lon.setText("当前经度：" + longitude);

                    tv_location.setText("当前位置：" + amapLocation.getAddress());

                    tv_city.setText("当前城市：" + amapLocation.getProvince() + "-" + amapLocation.getCity() + "-" + amapLocation.getDistrict() + "-" + amapLocation.getStreet() + "-" + amapLocation.getStreetNum());

                    tv_poi.setText("当前位置："+amapLocation.getAoiName());
                    amapLocation.getAccuracy();//获取精度信息
                } else {
                    Log.e("AmapError", "location Error, ErrCode:"
                            + amapLocation.getErrorCode() + ", errInfo:"
                            + amapLocation.getErrorInfo());

                    Toast.makeText(MainActivity.this, "定位失败", Toast.LENGTH_SHORT).show();
                }
            }

        }
    }

    @Override
    public void onClick(View v) {
        if (v == send) {

            Intent intent = new Intent();

            intent.putExtra("longitude", longitude);
            intent.putExtra("latitude", latitude);
            intent.putExtra("cityCode", cityCode);

            intent.setClass(this, ShareLocationActivity.class);
            startActivity(intent);
        }
    }
}
