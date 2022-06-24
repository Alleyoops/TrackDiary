package com.apozas.diary;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.MapsInitializer;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.PolylineOptions;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 1：轨迹移动不平滑：参见点平滑移动：https://lbs.amap.com/api/android-sdk/guide/draw-on-map/smooth-move
 * 2：没有保存坐标数组（历史轨迹）
 * 3：没有实现绘制历史轨迹
 * 4：distance有问题
 */
public class TrackActivity extends AppCompatActivity implements AMapLocationListener,LocationSource {
    MapView mMapView = null;
    AMap aMap;
    Boolean _3d = false;
    Boolean _indoor = false;
    AMapLocationClient mLocationClient;
    AMapLocationClientOption mLocationOption;
    Boolean isFirstLoc = true;//是否第一次定位
    OnLocationChangedListener mListener;
    MyLocationStyle myLocationStyle;
    UiSettings mUiSettings;
    AMapLocation privLocation;
    LatLng mLocalLatlng;
    int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);
        //操作
        operation();
        //隐私合规接口
        MapsInitializer.updatePrivacyShow(this,true,true);
        MapsInitializer.updatePrivacyAgree(this,true);

        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.map);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);
        //初始化地图控制器对象
        if (aMap == null)
        {
            aMap = mMapView.getMap();
            //自定义地图
            setUpMap();
        }
        // 设置定位监听
        aMap.setLocationSource(this);
        // 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        aMap.setMyLocationEnabled(true);
        //初始化并开始定位
//        try {
//            location();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }


    }
    private void setUpMap() {
        // 设置小蓝点的图标
        myLocationStyle = new MyLocationStyle();
        //        myLocationStyle.myLocationIcon(BitmapDescriptorFactory
        //                .fromResource(R.drawable.navi_map_gps_locked));// 设置小蓝点的图标
        // 设置圆形的边框颜色
        myLocationStyle.strokeColor(Color.argb(50, 30, 150, 180));
        // 设置圆形的填充颜色
        myLocationStyle.radiusFillColor(Color.argb(50, 30,150, 180));
        // 设置圆形的边框粗细
        myLocationStyle.strokeWidth(1.0f);
        // 定位、且将视角移动到地图中心点,定位点依照设备方向旋转,并且会跟随设备移动。
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);
        //设置定位蓝点的Style
        aMap.setMyLocationStyle(myLocationStyle);
        //实例化UiSettings类对象
        mUiSettings = aMap.getUiSettings();
        //添加比例尺
        mUiSettings.setScaleControlsEnabled(true);
        //缩放按钮在右边中间
        mUiSettings.setZoomPosition(AMapOptions.ZOOM_POSITION_RIGHT_CENTER);
        // 设置默认定位按钮是否显示
        aMap.getUiSettings().setMyLocationButtonEnabled(true);
    }

    private  void operation(){
        Button btn1 = (Button) findViewById(R.id.btn1);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _3d = !_3d;
                Log.d("TrackActivity",_3d.toString());
                Toast.makeText(TrackActivity.this, "切换3D模式需要时间缓冲，请稍等", Toast.LENGTH_SHORT).show();
                MapsInitializer.setTerrainEnable(!_3d);
            }
        });
        Button btn2 = (Button) findViewById(R.id.btn2);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _indoor = !_indoor;
                Toast.makeText(TrackActivity.this, "室内模式一般在商圈附近可见", Toast.LENGTH_SHORT).show();
                Log.d("TrackActivity", _indoor.toString());
                aMap.showIndoorMap(_indoor);
            }
        });

    }



//    public  void location() throws Exception {
//        //初始化定位
//        mLocationClient = new AMapLocationClient(getApplicationContext());
//        //设置定位回调监听
//        mLocationClient.setLocationListener(this);
//        //初始化定位参数
//        mLocationOption = new AMapLocationClientOption();
//        //设置定位模式为Hight_Accuracy高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
//        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
//        //设置是否返回地址信息（默认返回地址信息）
//        mLocationOption.setNeedAddress(true);
//        //设置是否只定位一次,默认为false
//        mLocationOption.setOnceLocation(false);
//        //设置是否强制刷新WIFI，默认为强制刷新
//        mLocationOption.setWifiActiveScan(true);
//        //设置是否允许模拟位置,默认为false，不允许模拟位置
//        mLocationOption.setMockEnable(false);
//        //设置定位间隔,单位毫秒,默认为2000ms
//        mLocationOption.setInterval(2000);
//        //给定位客户端对象设置定位参数
//        mLocationClient.setLocationOption(mLocationOption);
//        //启动定位
//        mLocationClient.startLocation();
//    }



//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
//        mMapView.onDestroy();
//    }
//    @Override
//    protected void onResume() {
//        super.onResume();
//        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
//        mMapView.onResume();
//    }
//    @Override
//    protected void onPause() {
//        super.onPause();
//        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
//        mMapView.onPause();
//    }
//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
//        mMapView.onSaveInstanceState(outState);
//    }

    //AMapLocationListener的定位回调函数，实现了获取定位的数据
    private int index = 0;
    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        count++;
//        if (aMapLocation != null) {
//            if (aMapLocation.getErrorCode() == 0) {
//                //定位成功回调信息，设置相关消息
//                aMapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见官方定位类型表
//                aMapLocation.getLatitude();//获取纬度
//                aMapLocation.getLongitude();//获取经度
//                aMapLocation.getAccuracy();//获取精度信息
//                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                Date date = new Date(aMapLocation.getTime());
//                df.format(date);//定位时间
//                aMapLocation.getAddress();//地址，如果option中设置isNeedAddress为false，则没有此结果，网络定位结果中会有地址信息，GPS定位不返回地址信息。
//                aMapLocation.getCountry();//国家信息
//                aMapLocation.getProvince();//省信息
//                aMapLocation.getCity();//城市信息
//                aMapLocation.getDistrict();//城区信息
//                aMapLocation.getStreet();//街道信息
//                aMapLocation.getStreetNum();//街道门牌号信息
//                aMapLocation.getCityCode();//城市编码
//                aMapLocation.getAdCode();//地区编码
//
//
//                String s = "第"+count+"次"+date.toString()+String.valueOf(aMapLocation.getLongitude());
//                ((TextView)findViewById(R.id.test)).setText(s);
//
//                // 如果不设置标志位，此时再拖动地图时，它会不断将地图移动到当前的位置
//                if (isFirstLoc) {
//                    //设置缩放级别
//                    aMap.moveCamera(CameraUpdateFactory.zoomTo(17));
//                    //将地图移动到定位点
//                    aMap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude())));
//                    //点击定位按钮 能够将地图的中心移动到定位点
//                    mListener.onLocationChanged(aMapLocation);
//                    //添加图钉
//                    //  aMap.addMarker(getMarkerOptions(amapLocation));
//                    //获取定位信息
//                    StringBuffer buffer = new StringBuffer();
//                    buffer.append(aMapLocation.getLatitude() + "，"
//                            + aMapLocation.getLongitude());
//                    Toast.makeText(getApplicationContext(), buffer.toString(), Toast.LENGTH_LONG).show();
//                    isFirstLoc = false;
//
//                }
//            }
//        }

        if (mListener != null && aMapLocation != null) {
            if (aMapLocation.getErrorCode() == 0) {
                mListener.onLocationChanged(aMapLocation);// 显示系统小蓝点
                if(isFirstLoc) {
                    //构造经纬度对象，并通过amapLocation对象传入经纬度数据
                    mLocalLatlng = new LatLng(aMapLocation.getLatitude(),aMapLocation.getLongitude());
                    //图层交互的方法，用于缩放地图
                    aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLocalLatlng, 18));
                    isFirstLoc = false;
                }
            } else {
                String errText = "定位失败," + aMapLocation.getErrorCode()+ ": " + aMapLocation.getErrorInfo();
                Log.e("TrackActivity",errText);
            }
        }
        if (aMapLocation != null) {
            mListener.onLocationChanged(aMapLocation); //显示系统小蓝点,不写这一句无法显示到当前位置
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date(aMapLocation.getTime());
            df.format(date);//定位时间
            aMapLocation.getLocationType(); //获取当前定位结果来源，如网络定位结果，详见定位类型表
            aMapLocation.getAccuracy(); //获取精度信息
            aMapLocation.getBearing(); //获取方向角信息
            aMapLocation.getSpeed(); //获取速度信息  单位：米/秒
            aMapLocation.getLocationType(); //查看是什么类型的点
//            Log.e("TrackActivity", "获取经纬度集合" + aMapLocation);//打Log记录点是否正确
//            Log.e("TrackActivity", "获取点的类型" + aMapLocation.getLocationType());
            String s = "第"+count+"次定位\nDate："+date.toString()+"\nLocation："+"["+aMapLocation.getLongitude()+"，"+aMapLocation.getLatitude()+"]"
                    +"\nAddress："+aMapLocation.getAddress();

            ((TextView)findViewById(R.id.test)).setText(s);
            //一边定位一边连线
            drawLines(aMapLocation);
            privLocation = aMapLocation;
//            Log.d("TrackActivity", "距离"+String.valueOf(Distance(aMapLocation)));
            //将坐标点存于数组里
            if (aMapLocation.getLatitude() != 0 && aMapLocation.getLongitude() != 0) {
                //latLngPoints.add(new LatLngPoint(index++, new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude())));
            }
        } else {
            //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
            Log.e("TrackActivity", "location Error, ErrCode:"
                    + aMapLocation.getErrorCode() + ", errInfo:" + aMapLocation.getErrorInfo());
        }

    }

    public void drawLines(AMapLocation curLocation) {

        if (null == privLocation) {
            return;
        }
        if (curLocation.getLatitude() != 0.0 && curLocation.getLongitude() != 0.0
                && privLocation.getLongitude() != 0.0 && privLocation.getLatitude() != 0.0) {
            PolylineOptions options = new PolylineOptions();
            //上一个点的经纬度
            options.add(new LatLng(privLocation.getLatitude(), privLocation.getLongitude()));
            //当前的经纬度
            options.add(new LatLng(curLocation.getLatitude(), curLocation.getLongitude()));
            options.width(10).geodesic(true).color(Color.BLUE);
            aMap.addPolyline(options);
        }

    }

    private double Distance(AMapLocation curLocation) {
        double distance = 0.0;
        distance = AMapUtils.calculateLineDistance(new LatLng(privLocation.getLatitude(),
                privLocation.getLongitude()), new LatLng(curLocation.getLatitude(),
                curLocation.getLongitude()));
        distance += distance;
        return distance;
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
        if (mLocationClient == null) {
            //初始化定位
            try {
                mLocationClient = new AMapLocationClient(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //初始化定位参数
            mLocationOption = new AMapLocationClientOption();
            //设置定位回调监听
            mLocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //设置定位间隔,单位毫秒,默认为2000ms
            mLocationOption.setInterval(2000);
            //设置是否返回地址信息（默认返回地址信息）
            mLocationOption.setNeedAddress(true);
            //设置定位参数
            mLocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mLocationClient.startLocation();//启动定位
        }
    }

    @Override
    public void deactivate() {
        mListener = null;
        if (mLocationClient != null) {
//            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }
        mLocationClient = null;
    }

//    @Override
//    protected void onStop() {
//        super.onStop();
//        mLocationClient.stopLocation();//停止定位
//    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        mMapView.onDestroy();
//        if(null != mLocationClient){
//            mLocationClient.onDestroy();
//        }
//    }
}






























