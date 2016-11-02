package example.fussen.mapdemo.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.UiSettings;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;

import java.util.ArrayList;
import java.util.List;

import example.fussen.mapdemo.R;
import example.fussen.mapdemo.adapter.AddressAdapter;
import example.fussen.mapdemo.bean.SearchAddressInfo;
import example.fussen.mapdemo.utils.ToastUtil;

public class ShareLocationActivity extends Activity implements GeocodeSearch.OnGeocodeSearchListener, AMap.OnMapClickListener, AMap.OnCameraChangeListener, PoiSearch.OnPoiSearchListener, AdapterView.OnItemClickListener, View.OnClickListener {

    private String addressName;
    private GeocodeSearch geocoderSearch;
    private MapView mapView;
    private ListView listView;
    private LatLonPoint latLonPoint;
    private AMap aMap;
    private Marker locationMarker;
    private LatLng mFinalChoosePosition;
    private ImageView centerImage;
    private Animation centerAnimation;


    private int currentPage = 0;// 当前页面，从0开始计数
    private PoiSearch.Query query;// Poi查询条件类
    private PoiSearch poiSearch;
    private String city;

    private PoiResult poiResult; // poi返回的结果
    private List<PoiItem> poiItems;// poi数据

    private ArrayList<SearchAddressInfo> mData = new ArrayList<>();
    public SearchAddressInfo mAddressInfoFirst = null;

    private boolean isHandDrag = true;
    private boolean isFirstLoad = true;
    private boolean isBackFromSearch = false;
    private AddressAdapter addressAdapter;
    private UiSettings uiSettings;
    private ImageButton locationButton;
    private ImageView search;
    private TextView send;
    private ImageView back;

    private static final int SEARCH_ADDDRESS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_location);

        double longitude = getIntent().getDoubleExtra("longitude", 0);
        double latitude = getIntent().getDoubleExtra("latitude", 0);
        city = getIntent().getStringExtra("cityCode");

        //经纬度信息
        latLonPoint = new LatLonPoint(latitude, longitude);

        mapView = (MapView) findViewById(R.id.mapview);

        listView = (ListView) findViewById(R.id.listview);

        centerImage = (ImageView) findViewById(R.id.center_image);

        locationButton = (ImageButton) findViewById(R.id.position_btn);

        search = (ImageView) findViewById(R.id.seach);

        send = (TextView) findViewById(R.id.send);

        back = (ImageView) findViewById(R.id.base_back);

        search.setOnClickListener(this);
        send.setOnClickListener(this);
        back.setOnClickListener(this);

        locationButton.setOnClickListener(this);

        mapView.onCreate(savedInstanceState);

        centerAnimation = AnimationUtils.loadAnimation(this, R.anim.center_anim);


        addressAdapter = new AddressAdapter(this, mData);

        listView.setAdapter(addressAdapter);

        listView.setOnItemClickListener(this);
        initMap();

    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        mapView.onSaveInstanceState(outState);
    }

    private void initMap() {

        if (aMap == null) {
            aMap = mapView.getMap();

            //地图ui界面设置
            uiSettings = aMap.getUiSettings();

            //地图比例尺的开启
            uiSettings.setScaleControlsEnabled(true);

            //关闭地图缩放按钮 就是那个加号 和减号
            uiSettings.setZoomControlsEnabled(false);


            aMap.setOnMapClickListener(this);

            //对amap添加移动地图事件监听器
            aMap.setOnCameraChangeListener(this);

            locationMarker = aMap.addMarker(new MarkerOptions()
                    .anchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.marker)))
                    .position(new LatLng(latLonPoint.getLatitude(), latLonPoint.getLongitude())));

            //拿到地图中心的经纬度
            mFinalChoosePosition = locationMarker.getPosition();
        }


        setMap();
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latLonPoint.getLatitude(), latLonPoint.getLongitude()), 20));
    }

    private void setMap() {


        geocoderSearch = new GeocodeSearch(getApplicationContext());

        //设置逆地理编码监听
        geocoderSearch.setOnGeocodeSearchListener(this);
    }


    /**
     * 根据经纬度得到地址
     */
    public void getAddressFromLonLat(final LatLng latLonPoint) {
        // 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
        RegeocodeQuery query = new RegeocodeQuery(convertToLatLonPoint(latLonPoint), 200, GeocodeSearch.AMAP);
        geocoderSearch.getFromLocationAsyn(query);// 设置同步逆地理编码请求
    }


    /**
     * 把LatLng对象转化为LatLonPoint对象
     */
    public static LatLonPoint convertToLatLonPoint(LatLng latlon) {
        return new LatLonPoint(latlon.latitude, latlon.longitude);
    }


    /**
     * 把LatLonPoint对象转化为LatLon对象
     */
    public LatLng convertToLatLng(LatLonPoint latLonPoint) {
        return new LatLng(latLonPoint.getLatitude(), latLonPoint.getLongitude());
    }


    /**
     * 逆地理编码查询回调
     *
     * @param result
     * @param i
     */
    @Override
    public void onRegeocodeSearched(RegeocodeResult result, int i) {

        if (i == 1000) {//转换成功
            if (result != null && result.getRegeocodeAddress() != null
                    && result.getRegeocodeAddress().getFormatAddress() != null) {
                //拿到详细地址
                addressName = result.getRegeocodeAddress().getFormatAddress(); // 逆转地里编码不是每次都可以得到对应地图上的opi

                //条目中第一个地址 也就是当前你所在的地址
                mAddressInfoFirst = new SearchAddressInfo(addressName, addressName, false, convertToLatLonPoint(mFinalChoosePosition));

                //其实也是可以在这就能拿到附近的兴趣点的

            } else {
                ToastUtil.show(this, "没有搜到");
            }
        } else {
            ToastUtil.showerror(this, i);
        }

    }

    /**
     * 地理编码查询回调
     *
     * @param geocodeResult
     * @param i
     */
    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

    }

    @Override
    public void onMapClick(LatLng latLng) {
        Toast.makeText(this, "latitude" + String.valueOf(latLng.latitude), Toast.LENGTH_SHORT).show();
    }

    /**
     * 移动地图时调用
     *
     * @param cameraPosition
     */
    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

    }

    /**
     * 地图移动结束后调用
     *
     * @param cameraPosition
     */
    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {

        //每次移动结束后地图中心的经纬度
        mFinalChoosePosition = cameraPosition.target;

        centerImage.startAnimation(centerAnimation);


        if (isHandDrag || isFirstLoad) {//手动去拖动地图

            // 开始进行poi搜索
            getAddressFromLonLat(cameraPosition.target);
            doSearchQueryByPosition();

        } else if (isBackFromSearch) {
            //搜索地址返回后 拿到选择的位置信息继续搜索附近的兴趣点
            isBackFromSearch = false;
            doSearchQueryByPosition();
        } else {
            addressAdapter.notifyDataSetChanged();
        }
        isHandDrag = true;
        isFirstLoad = false;
    }


    /**
     * 开始进行poi搜索
     * 通过经纬度获取附近的poi信息
     * <p>
     * 1、keyword 传 ""
     * 2、poiSearch.setBound(new PoiSearch.SearchBound(lpTemp, 5000, true)); 根据
     */
    protected void doSearchQueryByPosition() {

        currentPage = 0;
        query = new PoiSearch.Query("", "", city);// 第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
        query.setPageSize(20);// 设置每页最多返回多少条poiitem
        query.setPageNum(currentPage);// 设置查第一页

        LatLonPoint llPoint = convertToLatLonPoint(mFinalChoosePosition);

        if (llPoint != null) {
            poiSearch = new PoiSearch(this, query);
            poiSearch.setOnPoiSearchListener(this);  // 实现  onPoiSearched  和  onPoiItemSearched
            poiSearch.setBound(new PoiSearch.SearchBound(llPoint, 5000, true));//
            // 设置搜索区域为以lpTemp点为圆心，其周围5000米范围
            poiSearch.searchPOIAsyn();// 异步搜索
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onPoiSearched(PoiResult result, int rcode) {

        if (rcode == 1000) {
            if (result != null && result.getQuery() != null) {// 搜索poi的结果
                if (result.getQuery().equals(query)) {// 是否是同一条
                    poiResult = result;
                    poiItems = poiResult.getPois();// 取得第一页的poiitem数据，页数从数字0开始

                    List<SuggestionCity> suggestionCities = poiResult
                            .getSearchSuggestionCitys();// 当搜索不到poiitem数据时，会返回含有搜索关键字的城市信息


                    //搜索到数据
                    if (poiItems != null && poiItems.size() > 0) {

                        mData.clear();

                        //先将 逆地理编码过的当前地址 也就是条目中第一个地址 放到集合中
                        mData.add(mAddressInfoFirst);

                        SearchAddressInfo addressInfo = null;

                        for (PoiItem poiItem : poiItems) {

                            addressInfo = new SearchAddressInfo(poiItem.getTitle(), poiItem.getSnippet(), false, poiItem.getLatLonPoint());

                            mData.add(addressInfo);
                        }
                        if (isHandDrag) {
                            mData.get(0).isChoose = true;
                        }
                        addressAdapter.notifyDataSetChanged();

                    } else if (suggestionCities != null
                            && suggestionCities.size() > 0) {
                        showSuggestCity(suggestionCities);
                    } else {
                        ToastUtil.show(ShareLocationActivity.this,
                                "对不起，没有搜索到相关数据");
                    }
                }
            } else {
                Toast.makeText(this, "对不起，没有搜索到相关数据！", Toast.LENGTH_SHORT).show();
            }
        }

    }


    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        mFinalChoosePosition = convertToLatLng(mData.get(position).latLonPoint);
        for (int i = 0; i < mData.size(); i++) {
            mData.get(i).isChoose = false;
        }
        mData.get(position).isChoose = true;

        isHandDrag = false;

        // 点击之后，改变了地图中心位置， onCameraChangeFinish 也会调用
        // 只要地图发生改变，就会调用 onCameraChangeFinish ，不是说非要手动拖动屏幕才会调用该方法
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mFinalChoosePosition.latitude, mFinalChoosePosition.longitude), 20));
    }


    /**
     * poi没有搜索到数据，返回一些推荐城市的信息
     */
    private void showSuggestCity(List<SuggestionCity> cities) {
        String infomation = "推荐城市\n";
        for (int i = 0; i < cities.size(); i++) {
            infomation += "城市名称:" + cities.get(i).getCityName() + "城市区号:"
                    + cities.get(i).getCityCode() + "城市编码:"
                    + cities.get(i).getAdCode() + "\n";
        }
        ToastUtil.show(this, infomation);

    }

    @Override
    public void onClick(View v) {
        if (v == locationButton) {
            //回到当前位置
            aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latLonPoint.getLatitude(), latLonPoint.getLongitude()), 20));
        } else if (v == back) {
            finish();
        } else if (v == search) {

            Intent intent = new Intent(this, SearchAddressActivity.class);
            intent.putExtra("position", mFinalChoosePosition);
            intent.putExtra("city", city);
            startActivityForResult(intent, SEARCH_ADDDRESS);
            isBackFromSearch = false;

        } else if (v == send) {

            sendLocaton();

        }
    }

    private void sendLocaton() {
        SearchAddressInfo addressInfo = null;
        for (SearchAddressInfo info : mData) {
            if (info.isChoose) {
                addressInfo = info;
            }
        }
        if (addressInfo != null) {

            ToastUtil.show(this, "要发送的数据："
                    + "\n 经度：" + addressInfo.latLonPoint.getLongitude()
                    + "\n 纬度：" + addressInfo.latLonPoint.getLatitude()
                    + "\n 地址：" + addressInfo.addressName);
        } else {
            ToastUtil.show(this, "请选择地址");
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SEARCH_ADDDRESS && resultCode == RESULT_OK) {
            SearchAddressInfo info = (SearchAddressInfo) data.getParcelableExtra("position");
            mAddressInfoFirst = info; // 上一个页面传过来的 位置信息
            info.isChoose = true;
            isBackFromSearch = true;
            isHandDrag = false;
            //移动地图
            aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(info.latLonPoint.getLatitude(), info.latLonPoint.getLongitude()), 20));
        }
    }
}
