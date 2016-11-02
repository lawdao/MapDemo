# MapDemo
仿微信发送位置
## 效果演示
![效果演示](http://ww3.sinaimg.cn/large/65e4f1e6gw1f9dyq42n81g20950gc1kx.gif)
## 实现的功能
1. 根据经纬度定位
2. 根据所在的经纬度搜索附近的地点
3. 根据关键字搜索地点
4. 为地图增加标记
5. 发送选择的地点位置信息

## 你能学到什么
1. 高德地图的使用
2. 如何定位(获取当前位置信息)
3. 如何通过经纬度搜索附近的地点（也叫兴趣点poi）
4. 如何通过关键字搜索附近的兴趣点

## 微信位置发送分析
1. 进入发送页面后，自己当前位置有一个蓝色标记，它是不会变的，在蓝色的标记上有一个红色的小钉子，是用来显示你想要定位定到哪去，它在地图的中央
2. 地图下面有一个列表，是根据当前的经纬度搜索出来的兴趣点，也叫poi，默认选中第一个条目，也就是当前的位置
3. 当你点击列表的条目时，此时地图会根据你点击的条目变换到你所点击的地点去，此时，被点击的条目被选中
4. 当你手动的移动地图的时候，中间的红色小钉，不会动，当你移动完地图之后，红色小红钉会有一个上下移动的动画，表示你要定位到红色小红钉处，此时根据小红钉所处的经纬度来搜索附近的兴趣点(poi)
5. 进入搜索页面之后，通过关键字搜索地点，搜索完成之后，展示在列表里，默认选中第一个，点击条目之后，根据经纬度继续搜索附近的poi，此时列表的第一项是被点击的条目，地图同时移动到被点击的地点处
6. 点击发送后拿到当前被选中的条目的位置信息，然后发送

**分析完了微信的，下面就开始我们自己的，跟着我走，实现你自己想要的模样吧**

## 代码实现及步骤
### 1. 首页获取定位信息
	
	

```
public class MapUtils implements AMapLocationListener {
	    private AMapLocationClient locationClient = null;  // 定位
	    private AMapLocationClientOption locationOption = null;  // 定位设置
	
	    @Override
	    public void onLocationChanged(AMapLocation aMapLocation) {
	        mLonLatListener.getLonLat(aMapLocation);
	        locationClient.stopLocation();
	        locationClient.onDestroy();
	        locationClient = null;
	        locationOption = null;
	    }
	
	    private LonLatListener mLonLatListener;
	
	    public void getLonLat(Context context, LonLatListener lonLatListener) {
	        locationClient = new AMapLocationClient(context);
	        locationOption = new AMapLocationClientOption();
	        locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);// 设置定位模式为高精度模式
	        locationClient.setLocationListener(this);// 设置定位监听
	        locationOption.setOnceLocation(false); // 单次定位 每隔2秒定位一次
	        locationOption.setNeedAddress(true);//返回地址信息
	        mLonLatListener = lonLatListener;//接口
	        locationClient.setLocationOption(locationOption);// 设置定位参数
	        locationClient.startLocation(); // 启动定位
	    }
	
	    public interface LonLatListener {
	        void getLonLat(AMapLocation aMapLocation);
	    }
	
	}
	
```
	
### 2. 逆地理编码，经纬度转换
**逆地理编码步骤：**

1. 创建GeocodeSearch实例

	

```
	geocoderSearch = new GeocodeSearch(getApplicationContext());
```

2. 实现GeocodeSearch.OnGeocodeSearchListener监听

	

```
	//设置逆地理编码监听
	   	geocoderSearch.setOnGeocodeSearchListener(this);
	   	
		
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
	    	
```
	    	

**通过RegeocodeResult拿到编码后的信息**


### 3. 通过经纬度信息搜索poi


  

```
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
```
    
### 4. 通过关键字搜索poi


  

```
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
```
    
**通过poiSearch.setOnPoiSearchListener(this)实现对搜索结果的监听，在onPoiSearched()，onPoiItemSearched()回调里处理返回的结果**

### 5. 处理拿到的poi信息

	

```
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
```

### 6. 移动地图
1. 设置监听

		

```
	 //对amap添加移动地图事件监听器
	 aMap.setOnCameraChangeListener(this);
```

2. 实现监听

	

```
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
```

### 9. 最后阐述

看到这里，你想要的基本就能实现了

[Demo apk下载链接](http://dl.download.csdn.net/down11/20161102/146940987b9a97c47cff795ebb5a3a35.apk?response-content-disposition=attachment;filename=%22app-debug.apk%22&OSSAccessKeyId=9q6nvzoJGowBj4q1&Expires=1478086895&Signature=2gBHMZyj9LmHyIooDMH1Lp7yEYI=)

如果对你有用，请关注我们的微信公共号：AppCode

扫码即可关注
![](http://ww1.sinaimg.cn/large/65e4f1e6gw1f9btkbltksj2076076aaj.jpg)
