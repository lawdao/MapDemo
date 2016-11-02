package example.fussen.mapdemo.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.amap.api.maps2d.model.LatLng;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;

import java.util.ArrayList;
import java.util.List;

import example.fussen.mapdemo.R;
import example.fussen.mapdemo.adapter.SearchAdapter;
import example.fussen.mapdemo.bean.SearchAddressInfo;
import example.fussen.mapdemo.utils.ToastUtil;

public class SearchAddressActivity extends AppCompatActivity implements View.OnClickListener, PoiSearch.OnPoiSearchListener, AdapterView.OnItemClickListener {

    private TextView search;
    private EditText content;
    private ListView listView;
    private SearchAdapter adapter;

    private ArrayList<SearchAddressInfo> mData = new ArrayList<>();
    private String searchText;


    private LatLonPoint lp;//
    private PoiResult poiResult; // poi返回的结果
    private List<PoiItem> poiItems;// poi数据
    private int currentPage = 0;// 当前页面，从0开始计数
    private PoiSearch.Query query;// Poi查询条件类
    private PoiSearch poiSearch;

    private String city;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_address);

        LatLng position = getIntent().getParcelableExtra("position");
        city = getIntent().getStringExtra("city");
        lp = new LatLonPoint(position.latitude, position.longitude);

        iniView();
    }

    private void iniView() {
        search = (TextView) findViewById(R.id.btn_search);
        content = (EditText) findViewById(R.id.input_edittext);
        listView = (ListView) findViewById(R.id.listview);

        search.setOnClickListener(this);

        adapter = new SearchAdapter(this, mData);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(this);

        progressDialog = new ProgressDialog(this);

        progressDialog.setMessage("搜索中...");
    }

    @Override
    public void onClick(View v) {
        if (v == search) {
            dealSearch();
        }
    }

    private void dealSearch() {
        searchText = content.getText().toString().trim();
        if (TextUtils.isEmpty(searchText)) {
            ToastUtil.show(this, "请输入搜索关键字");
            return;
        } else {
            progressDialog.show();
            doSearchQueryByKeyWord(searchText);
        }
    }


    /**
     * 按照关键字搜索附近的poi信息
     *
     * @param key
     */
    protected void doSearchQueryByKeyWord(String key) {
        currentPage = 0;
        query = new PoiSearch.Query(key, "", city);// 第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
        query.setPageSize(20);// 设置每页最多返回多少条poiitem
        query.setPageNum(currentPage);// 设置查第一页
        query.setCityLimit(true); //限定城市

        if (lp != null) {
            poiSearch = new PoiSearch(this, query);
            poiSearch.setOnPoiSearchListener(this);   // 实现  onPoiSearched  和  onPoiItemSearched
            poiSearch.setBound(new PoiSearch.SearchBound(lp, 5000, true));//
            // 设置搜索区域为以latLonPoint点为圆心，其周围5000米范围
            poiSearch.searchPOIAsyn();// 异步搜索
        }
    }

    @Override
    public void onPoiSearched(PoiResult result, int rcode) {

        progressDialog.dismiss();

        if (rcode == 1000) {
            if (result != null && result.getQuery() != null) {// 搜索poi的结果
                if (result.getQuery().equals(query)) {// 是否是同一条
                    poiResult = result;
                    poiItems = poiResult.getPois();// 取得第一页的poiitem数据，页数从数字0开始
                    List<SuggestionCity> suggestionCities = poiResult
                            .getSearchSuggestionCitys();// 当搜索不到poiitem数据时，会返回含有搜索关键字的城市信息

                    if (poiItems.size() == 0) {
                        //没有搜到数据
                        ToastUtil
                                .show(this, "对不起，没有搜索到相关数据！");
                        return;
                    }
                    mData.clear();
                    SearchAddressInfo addressInfo = null;
                    for (int i = 0; i < poiItems.size(); i++) {
                        PoiItem poiItem = poiItems.get(i);
                        if (i == 0) {
                            addressInfo = new SearchAddressInfo(poiItem.getTitle(), poiItem.getSnippet(), true, poiItem.getLatLonPoint());
                        } else {
                            addressInfo = new SearchAddressInfo(poiItem.getTitle(), poiItem.getSnippet(), false, poiItem.getLatLonPoint());
                        }

                        mData.add(addressInfo);
                    }
                    adapter.notifyDataSetChanged();
                }
            } else {
                ToastUtil
                        .show(this, "对不起，没有搜索到相关数据！");
            }
        } else {
            ToastUtil
                    .show(this, "对不起，没有搜索到相关数据！");
        }

    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Intent intent = new Intent();
        SearchAddressInfo addressInfo = mData.get(position);
        intent.putExtra("position", addressInfo);
        setResult(RESULT_OK, intent);
        finish();
    }
}
