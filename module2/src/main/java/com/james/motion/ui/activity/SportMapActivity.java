package com.james.motion.ui.activity;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PersistableBundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.LocationSource.OnLocationChangedListener;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.james.motion.R;
import com.james.motion.R2;
import com.james.motion.commmon.bean.PathRecord;
import com.james.motion.commmon.bean.SportMotionRecord;
import com.james.motion.commmon.utils.CountTimerUtil;
import com.james.motion.commmon.utils.DateUtils;
import com.james.motion.commmon.utils.LogUtils;
import com.james.motion.commmon.utils.MySp;
import com.james.motion.commmon.utils.UIHelper;
import com.james.motion.db.DataManager;
import com.james.motion.db.RealmHelper;
import com.james.motion.sport_motion.MotionUtils;
import com.james.motion.sport_motion.PathSmoothTool;
import com.james.motion.ui.BaseActivity;
import com.james.motion.ui.weight.SecuritySP;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 描述: 运动界面
 * 类名: SportMapActivity
 */
@RequiresApi(api = Build.VERSION_CODES.N)
public class SportMapActivity extends BaseActivity {

    @BindView(R2.id.sport_content)
    RelativeLayout sportContent;
    @BindView(R2.id.mapView)
    MapView mapView;
    @BindView(R2.id.rlMap)
    RelativeLayout rlMap;
    @BindView(R2.id.tv_mode)
    TextView tvMode;
    @BindView(R2.id.tv1)
    TextView tv1;
    @BindView(R2.id.tv2)
    TextView tv2;
    @BindView(R2.id.tv3)
    TextView tv3;
    @BindView(R2.id.cm_passtime)
    Chronometer cmPasstime;
    @BindView(R2.id.tvMileage)
    TextView tvMileage;
    @BindView(R2.id.tvSpeed)
    TextView tvSpeed;
    @BindView(R2.id.fl_count_timer)
    FrameLayout flCountTimer;
    @BindView(R2.id.tv_number_anim)
    TextView tvNumberAnim;
    @BindView(R2.id.progress)
    ProgressBar progressBar;
    @BindView(R2.id.tv_percent)
    TextView tvPercent;
    @BindView(R2.id.tv_plan)
    TextView tvPlan;
    @BindView(R2.id.ll1)
    LinearLayout ll1;
    @BindView(R2.id.ll2)
    LinearLayout ll2;

    private Dialog tipDialog = null;
    private String planDist,planTime,planCal,heavy;
    private int flag;
    private boolean isOk = false;//是否结束震动提醒


    //运动计算相关
    private DecimalFormat decimalFormat = new DecimalFormat("0.00");

//    private boolean isBind = false;
//    private LocationService mService = null;
//    private ServiceConnection mConnection = new ServiceConnection() {
//
//        @Override
//        public void onServiceDisconnected(ComponentName name) {
//            mService = null;
//        }
//
//        @Override
//        public void onServiceConnected(ComponentName name, IBinder service) {
//            // 获取服务上的IBinder对象，调用IBinder对象中定义的自定义方法，获取Service对象
//            LocationService.LocalBinder binder = (LocationService.LocalBinder) service;
//            mService = binder.getService();
//            mService.setInterfaceLocationed(aMapLocation -> {
//                Message msg = Message.obtain();
//                msg.what = LOCATION;
//                msg.obj = aMapLocation;
//                mHandler.sendMessage(msg);
//            });
//        }
//    };

    //地图中定位的类
    private OnLocationChangedListener mListener = null;
    private AMapLocationClient mLocationClient;
    private AMapLocationClientOption mLocationOption;
    private final Long interval = 2000L;//定位时间间隔

    private PolylineOptions polylineOptions;
    private Polyline mOriginPolyline;
    private PathRecord record;
    private DataManager dataManager = null;
    private PathSmoothTool mpathSmoothTool = null;
    private List<LatLng> mSportLatLngs = new ArrayList<>(0);

    private long seconds = 0;//秒数(时间)
    private long mStartTime = 0;
    private long mEndTime = 0;
    private double distance;//路程
//    private float calorie;//卡路里
//    private float speed;//速度

    private boolean ISSTARTUP = false;

    private ValueAnimator apperaAnim1;
    private ValueAnimator hiddenAnim1;

    private ValueAnimator apperaAnim2;
    private ValueAnimator hiddenAnim2;

    private ValueAnimator apperaAnim3;
    private ValueAnimator hiddenAnim3;

    private AMap aMap;

    private boolean mode = true;

    private final int LOCATION = 0;
    private Handler mHandler = new Handler(Looper.getMainLooper())
//    {
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case LOCATION://用handler刷新数据
//                    updateLocation((AMapLocation) msg.obj);
//                    break;
//                default:
//                    break;
//            }
//        }
//    }
            ;

    private class MyRunnable implements Runnable {
        @Override
        public void run() {
            cmPasstime.setText(formatseconds());
            mHandler.postDelayed(this, 1000);
        }
    }

    private MyRunnable mRunnable = null;

    @Override
    public int getLayoutId() {
        return R.layout.activity_sportmap;
    }

    @Override
    public void initData(Bundle savedInstanceState) throws GeneralSecurityException, IOException {



        mapView.onCreate(savedInstanceState);// 此方法必须重写

        record = new PathRecord();

        dataManager = new DataManager(new RealmHelper());

        //显示倒计时
        CountTimerUtil.start(tvNumberAnim, new CountTimerUtil.AnimationState() {
            @Override
            public void start() {

            }

            @Override
            public void repeat() {

            }

            @Override
            public void end() {
                flCountTimer.setVisibility(View.GONE);
                hiddenAnim1.start();
//                apperaAnim2.start_bg();
                hiddenAnim3.start();

                ISSTARTUP = true;

                seconds = 0;
                cmPasstime.setBase(SystemClock.elapsedRealtime());

                mStartTime = System.currentTimeMillis();
                if (record == null)
                    record = new PathRecord();
                record.setStartTime(mStartTime);

                if (mRunnable == null)
                    mRunnable = new MyRunnable();
                mHandler.postDelayed(mRunnable, 0);

                startUpLocation();

            }
        });

        //获取计划值
        setPlan();
        //显示进度条
        setModeFlag();
        setProgressbar();

        if (aMap == null) {
            aMap = mapView.getMap();
            setUpMap();
        }

        initPolyline();

        setMode();
    }

    private void initPolyline() {
        polylineOptions = new PolylineOptions();
        polylineOptions.color(getResources().getColor(R.color.colorAccent));
        polylineOptions.width(20f);
        polylineOptions.useGradient(true);

        mpathSmoothTool = new PathSmoothTool();
        mpathSmoothTool.setIntensity(4);
    }

    private void startUpLocation() {
        //绑定服务
//        isBind = bindService(new Intent(this, LocationService.class), mConnection, Service.BIND_AUTO_CREATE);

        //屏幕保持常亮
        if (null != mapView)
            sportContent.setKeepScreenOn(true);

        startLocation();
    }

    private void unBindService() {
        //解除绑定服务
//        if (isBind && null != mService) {
//            unbindService(mConnection);
//            isBind = false;
//        }
//        mService = null;

        //屏幕取消常亮
        if (null != mapView)
            sportContent.setKeepScreenOn(false);

        //停止定位
        if (null != mLocationClient) {
            mLocationClient.stopLocation();
            mLocationClient.unRegisterLocationListener(aMapLocationListener);
            mLocationClient.onDestroy();
            mLocationClient = null;
        }
    }

    private void setMode() {
        if (mode) {
            tvMode.setText("地图模式");
            UIHelper.setLeftDrawable(tvMode, R.mipmap.map_mode);
            rlMap.setVisibility(View.GONE);
        } else {
            tvMode.setText("跑步模式");
            UIHelper.setLeftDrawable(tvMode, R.mipmap.run_mode);
            rlMap.setVisibility(View.VISIBLE);
        }
        mode = !mode;
    }

    /**
     * 开始定位。
     */
    private void startLocation() {
        if (mLocationClient == null) {
            try {
                mLocationClient = new AMapLocationClient(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //设置定位属性
            mLocationOption = new AMapLocationClientOption();
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
            mLocationOption.setGpsFirst(true);//可选，设置是否gps优先，只在高精度模式下有效。默认关闭
            mLocationOption.setHttpTimeOut(30000);//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
            mLocationOption.setInterval(interval);//可选，设置定位间隔。默认为2秒
            mLocationOption.setNeedAddress(false);//可选，设置是否返回逆地理地址信息。默认是true
            mLocationOption.setOnceLocation(false);//可选，设置是否单次定位。默认是false
            mLocationOption.setOnceLocationLatest(false);//可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
            AMapLocationClientOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTP);//可选， 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
            mLocationOption.setSensorEnable(false);//可选，设置是否使用传感器。默认是false
            mLocationOption.setWifiScan(true); //可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
            mLocationOption.setLocationCacheEnable(true); //可选，设置是否使用缓存定位，默认为true
            mLocationOption.setGeoLanguage(AMapLocationClientOption.GeoLanguage.ZH);//可选，设置逆地理信息的语言，默认值为默认语言（根据所在地区选择语言）
            mLocationClient.setLocationOption(mLocationOption);

            // 设置定位监听
            mLocationClient.setLocationListener(aMapLocationListener);
            //开始定位
            mLocationClient.startLocation();
        }
    }

    @Override
    public void initListener() {
//        cmPasstime.setOnChronometerTickListener(chronometer -> cmPasstime.setText(formatseconds()));
    }

    @OnClick({R2.id.tv_mode, R2.id.tv1, R2.id.tv2, R2.id.tv3})
    public void onViewClicked(View view) {
        int id = view.getId();
        if (id == R.id.tv_mode) {
            setMode();
        } else if (id == R.id.tv1) {
            ISSTARTUP = true;

            mHandler.removeCallbacks(mRunnable);
            mRunnable = null;

            unBindService();

            hiddenAnim1.start();
//                apperaAnim2.start_bg();
            hiddenAnim3.start();

            //保存数据
            if (null != record && null != record.getPathline() && !record.getPathline().isEmpty()) {
                saveRecord();
            } else {
                ToastUtils.showShort("没有记录到路径!");
                finish();
            }
        } else if (id == R.id.tv2) {
            ISSTARTUP = false;

            if (null != mRunnable) {
                mHandler.removeCallbacks(mRunnable);
                mRunnable = null;
            }

            unBindService();

            mEndTime = System.currentTimeMillis();

            aMap.moveCamera(CameraUpdateFactory.newLatLngBounds(getBounds(mSportLatLngs), 20));

            apperaAnim1.start();
            hiddenAnim2.start();
            apperaAnim3.start();
        } else if (id == R.id.tv3) {
            ISSTARTUP = true;

            if (mRunnable == null)
                mRunnable = new MyRunnable();
            mHandler.postDelayed(mRunnable, 0);

            startUpLocation();

            hiddenAnim1.start();
            apperaAnim2.start();
            hiddenAnim3.start();
        }
    }

    private void saveRecord() {

        showLoadingView(false);
        ToastUtils.showShort("正在保存运动数据!");

        try {
            SportMotionRecord sportMotionRecord = new SportMotionRecord();

            List<LatLng> locations = record.getPathline();
            LatLng firstLocaiton = locations.get(0);
            LatLng lastLocaiton = locations.get(locations.size() - 1);

            sportMotionRecord.setId(System.currentTimeMillis());
            sportMotionRecord.setMaster(Integer.parseInt(SPUtils.getInstance().getString(MySp.USERID, "0")));
            sportMotionRecord.setDistance(distance);
            sportMotionRecord.setDuration(seconds);
            sportMotionRecord.setmStartTime(mStartTime);
            sportMotionRecord.setmEndTime(mEndTime);
            sportMotionRecord.setStratPoint(MotionUtils.amapLocationToString(firstLocaiton));
            sportMotionRecord.setEndPoint(MotionUtils.amapLocationToString(lastLocaiton));
            sportMotionRecord.setPathLine(MotionUtils.getLatLngPathLineString(locations));
            double sportMile = distance / 1000d;
            //获取体重用来计算卡路里
            String heavy = SecuritySP.DecryptSP(context,"heavy");
            sportMotionRecord.setCalorie(MotionUtils.calculationCalorie(Integer.parseInt(heavy), sportMile));
            sportMotionRecord.setSpeed(sportMile / ((double) seconds / 3600));
            sportMotionRecord.setDistribution(record.getDistribution());
            sportMotionRecord.setDateTag(DateUtils.getStringDateShort(mEndTime));

            dataManager.insertSportRecord(sportMotionRecord);

            //保存reward
            saveReward();
        } catch (Exception e) {
            LogUtils.e("保存运动数据失败", e);
        }

        mHandler.postDelayed(() -> {
            dismissLoadingView();
            setResult(RESULT_OK);

            SportResultActivity.StartActivity(this, mStartTime, mEndTime);

            finish();
        }, 1500);

    }


    /**
     * 设置一些amap的属性
     */
    private void setUpMap() {
        aMap.setLocationSource(locationSource);// 设置定位监听
        // 自定义系统定位小蓝点
        MyLocationStyle myLocationStyle = new MyLocationStyle();
//        myLocationStyle.myLocationIcon(BitmapDescriptorFactory
//                .fromResource(R.drawable.mylocation_point));// 设置小蓝点的图标
        myLocationStyle.strokeColor(Color.TRANSPARENT);// 设置圆形的边框颜色
        myLocationStyle.radiusFillColor(Color.argb(0, 0, 0, 0));// 设置圆形的填充颜色
        // myLocationStyle.anchor(int,int)//设置小蓝点的锚点
        // 设置定位的类型为定位模式 ，定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);
//        myLocationStyle.interval(interval);//设置发起定位请求的时间间隔
//        myLocationStyle.showMyLocation(true);//设置是否显示定位小蓝点，true 显示，false不显示
        myLocationStyle.strokeWidth(1.0f);// 设置圆形的边框粗细
        aMap.setMyLocationStyle(myLocationStyle);
        aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        aMap.getUiSettings().setZoomControlsEnabled(false);// 设置默认缩放按钮是否显示
        aMap.getUiSettings().setCompassEnabled(false);// 设置默认指南针是否显示
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
    }

    private LocationSource locationSource = new LocationSource() {
        @Override
        public void activate(OnLocationChangedListener onLocationChangedListener) {
            mListener = onLocationChangedListener;
            startLocation();
        }

        @Override
        public void deactivate() {
            mListener = null;
            if (mLocationClient != null) {
                mLocationClient.stopLocation();
                mLocationClient.onDestroy();
            }
            mLocationClient = null;
        }
    };

    /**
     * 定位结果回调
     *
     * @param aMapLocation 位置信息类
     */
    private AMapLocationListener aMapLocationListener = aMapLocation -> {
        if (null == aMapLocation)
            return;
        if (aMapLocation.getErrorCode() == 0) {
            //先暂时获得经纬度信息，并将其记录在List中
            LogUtils.d("纬度信息为" + aMapLocation.getLatitude() + "\n经度信息为" + aMapLocation.getLongitude());
//            LatLng locationValue = new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude());

            //定位成功
            try {
                updateLocation(aMapLocation);
            } catch (GeneralSecurityException | IOException e) {
                e.printStackTrace();
            }

        } else {
            String errText = "定位失败," + aMapLocation.getErrorCode() + ": " + aMapLocation.getErrorInfo();
            LogUtils.e("AmapErr", errText);
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void updateLocation(AMapLocation aMapLocation) throws GeneralSecurityException, IOException {
        //原始轨迹
//        if (mOriginList != null && mOriginList.size() > 0) {
//            mOriginPolyline = aMap.addPolyline(new PolylineOptions().addAll(mOriginList).color(Color.GREEN));
//            aMap.moveCamera(CameraUpdateFactory.newLatLngBounds(getBounds(mOriginList), 200));
//        }


        record.addpoint(new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude()));

        //计算配速
        distance = getDistance(record.getPathline());

        double sportMile = distance / 1000d;
        if (seconds > 0 ) {
            double distribution = (double) seconds / 60d / sportMile;
            record.setDistribution(distribution);
            tvSpeed.setText(decimalFormat.format(distribution));//配速
            tvMileage.setText(decimalFormat.format(sportMile));//公里
        } else {
            record.setDistribution(0d);
            tvSpeed.setText(String.valueOf("0.00"));
            tvMileage.setText(String.valueOf("0.00"));
        }
        //刷新进度条
        freshProgressbar();

        mSportLatLngs.clear();
        //轨迹平滑优化
        mSportLatLngs = new ArrayList<>(mpathSmoothTool.pathOptimize(record.getPathline()));
        //抽稀
//        mSportLatLngs = new ArrayList<>(mpathSmoothTool.reducerVerticalThreshold(MotionUtils.parseLatLngList(record.getPathline())));
        //不做处理
//        mSportLatLngs = new ArrayList<>(MotionUtils.parseLatLngList(record.getPathline()));

        if (!mSportLatLngs.isEmpty()) {
            polylineOptions.add(mSportLatLngs.get(mSportLatLngs.size() - 1));
            if (mListener != null)
                mListener.onLocationChanged(aMapLocation);// 显示系统小蓝点
//            aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(getBounds(mSportLatLngs), 18));
            aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude()), 18));
        }
        mOriginPolyline = aMap.addPolyline(polylineOptions);
    }

    @Override
    protected void onDestroy() {
        mapView.onDestroy();

        // 清除动画，如果有
        if (tvNumberAnim != null) tvNumberAnim.clearAnimation();
        if (tv1 != null) tv1.clearAnimation();
        if (tv2 != null) tv2.clearAnimation();
        if (tv3 != null) tv3.clearAnimation();

        if (null != mRunnable) {
            mHandler.removeCallbacks(mRunnable);
            mRunnable = null;
        }

        unBindService();

        if (null != dataManager)
            dataManager.closeRealm();

        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            setApperaAnimationView();
            setHiddenAnimationView();
        }
        super.onWindowFocusChanged(hasFocus);
    }

    /**
     * 创建动画
     */
    public void setApperaAnimationView() {

        apperaAnim1 = ValueAnimator.ofFloat(tv1.getHeight() * 2, 0);
        apperaAnim1.setDuration(500);
        apperaAnim1.setTarget(tv1);
        apperaAnim1.addUpdateListener(animation -> tv1.setTranslationY((Float) animation.getAnimatedValue()));
        apperaAnim1.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                tv1.setEnabled(false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                tv1.setEnabled(true);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                tv1.setEnabled(true);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });


        apperaAnim2 = ValueAnimator.ofFloat(tv2.getHeight() * 2, 0);
        apperaAnim2.setDuration(500);
        apperaAnim2.setTarget(tv2);
        apperaAnim2.addUpdateListener(animation -> tv2.setTranslationY((Float) animation.getAnimatedValue()));
        apperaAnim2.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                tv2.setEnabled(false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                tv2.setEnabled(true);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                tv2.setEnabled(true);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        apperaAnim3 = ValueAnimator.ofFloat(tv3.getHeight() * 2, 0);
        apperaAnim3.setDuration(500);
        apperaAnim3.setTarget(tv3);
        apperaAnim3.addUpdateListener(animation -> tv3.setTranslationY((Float) animation.getAnimatedValue()));
        apperaAnim3.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                tv3.setEnabled(false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                tv3.setEnabled(true);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                tv3.setEnabled(true);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    /**
     * 创建动画
     */
    public void setHiddenAnimationView() {

        hiddenAnim1 = ValueAnimator.ofFloat(0, tv1.getHeight() * 2);
        hiddenAnim1.setDuration(500);
        hiddenAnim1.setTarget(tv1);
        hiddenAnim1.addUpdateListener(animation -> tv1.setTranslationY((Float) animation.getAnimatedValue()));
        hiddenAnim1.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                tv1.setEnabled(false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                tv1.setEnabled(true);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                tv1.setEnabled(true);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        hiddenAnim2 = ValueAnimator.ofFloat(0, tv2.getHeight() * 2);
        hiddenAnim2.setDuration(500);
        hiddenAnim2.setTarget(tv2);
        hiddenAnim2.addUpdateListener(animation -> tv2.setTranslationY((Float) animation.getAnimatedValue()));
        hiddenAnim2.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                tv2.setEnabled(false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                tv2.setEnabled(true);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                tv2.setEnabled(true);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        hiddenAnim3 = ValueAnimator.ofFloat(0, tv3.getHeight() * 2);
        hiddenAnim3.setDuration(500);
        hiddenAnim3.setTarget(tv3);
        hiddenAnim3.addUpdateListener(animation -> tv3.setTranslationY((Float) animation.getAnimatedValue()));
        hiddenAnim3.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                tv3.setEnabled(false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                tv3.setEnabled(true);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                tv3.setEnabled(true);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    public String formatseconds() {
        String hh = seconds / 3600 > 9 ? seconds / 3600 + "" : "0" + seconds
                / 3600;
        String mm = (seconds % 3600) / 60 > 9 ? (seconds % 3600) / 60 + ""
                : "0" + (seconds % 3600) / 60;
        String ss = (seconds % 3600) % 60 > 9 ? (seconds % 3600) % 60 + ""
                : "0" + (seconds % 3600) % 60;

        seconds++;

        return hh + ":" + mm + ":" + ss;
    }

    private LatLngBounds getBounds(List<LatLng> pointlist) {
        LatLngBounds.Builder b = LatLngBounds.builder();
        if (pointlist == null) {
            return b.build();
        }
        for (LatLng latLng : pointlist) {
            b.include(latLng);
        }
        return b.build();

    }

    //计算距离
    private float getDistance(List<LatLng> list) {
        float distance = 0;
        if (list == null || list.size() == 0) {
            return distance;
        }
        for (int i = 0; i < list.size() - 1; i++) {
            LatLng firstLatLng = list.get(i);
            LatLng secondLatLng = list.get(i + 1);
            double betweenDis = AMapUtils.calculateLineDistance(firstLatLng,
                    secondLatLng);
            distance = (float) (distance + betweenDis);
        }
        return distance;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_BACK) { // 表示按返回键 时的操作
                //是否正在运动记录数据
                if (ISSTARTUP) {
                    ToastUtils.showShort("退出请点击暂停按钮，结束运动!");
                    return true;

                }
                //是否有运动记录
                if (null != record && null != record.getPathline() && !record.getPathline().isEmpty()) {
                    showTipDialog("确定退出?",
                            "退出将删除本次运动记录,如要保留运动数据,请点击完成!",
                            new TipCallBack() {
                                @Override
                                public void confirm() {
                                    finish();
                                }

                                @Override
                                public void cancle() {

                                }
                            });
                    return true;
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        //是否正在运动记录数据
        if (ISSTARTUP) {
            ToastUtils.showShort("退出请点击暂停按钮，在结束运动!");
            return;
        }
        //是否有运动记录
        if (null != record && null != record.getPathline() && !record.getPathline().isEmpty()) {
            showTipDialog("确定退出?",
                    "退出将删除本次运动记录,如要保留运动数据,请点击完成!",
                    new TipCallBack() {
                        @Override
                        public void confirm() {
                            finish();
                        }

                        @Override
                        public void cancle() {

                        }
                    });
            return;
        }
        super.onBackPressed();
    }

    private void showTipDialog(String title, String tips, TipCallBack tipCallBack) {
        tipDialog = new Dialog(context, R.style.matchDialog);
        View view = LayoutInflater.from(context).inflate(R.layout.tip_dialog_layout, null);
        ((TextView) (view.findViewById(R.id.title))).setText(title);
        ((TextView) (view.findViewById(R.id.tips))).setText(tips);
        view.findViewById(R.id.cancelTV).setOnClickListener(
                v -> {
                    tipCallBack.cancle();
                    tipDialog.dismiss();
                });
        view.findViewById(R.id.confirmTV).setOnClickListener(v -> {
            tipCallBack.confirm();
            tipDialog.dismiss();
        });
        tipDialog.setContentView(view);
        tipDialog.show();
    }

    private interface TipCallBack {
        void confirm();

        void cancle();
    }

    //判断模式mode
    public void setModeFlag() throws GeneralSecurityException, IOException {
        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        //顺便把modeFlag存到SecuritySP，方便SportMapActivity使用
        flag = (int)bundle.get("mode");
        SecuritySP.EncryptSP(context,"modeFlag",String.valueOf(flag));
    }
    //设置进度条
    public void setProgressbar(){
        if (flag == 0)//随心跑，不显示进度条
        {
            ll1.setVisibility(View.INVISIBLE);
            ll2.setVisibility(View.INVISIBLE);
        }
    }
    //获取计划值
    public void setPlan() throws GeneralSecurityException, IOException {
        planDist = SecuritySP.DecryptSP(context,"dist");
        planTime = SecuritySP.DecryptSP(context,"time");
        planCal = SecuritySP.DecryptSP(context,"cal");
        heavy = SecuritySP.DecryptSP(context,"heavy");
    }
    //刷新进度条
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void freshProgressbar(){
        double percentage = 0d;//百分比
        int progress = 0;//进度条的进度
        if (flag == 1)//里程模式
        {
            double dist = Double.parseDouble(planDist);//计划值
            String str = (int)dist+"\t"+"m";
            tvPlan.setText(str);
            percentage = new BigDecimal(distance/dist*100).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            if (percentage<100)
            {
                progress = (int)(percentage);
            }else {
                progress = 100;
                percentage = 100;
                //震动提醒
                if (!isOk)//false说明第一次震动
                {
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(3000);
                    isOk = true;//下次不再震动
                }
            }
            tvPercent.setText(String.valueOf(percentage));
            progressBar.setProgress(progress);


        }
        if (flag == 2)//时长模式
        {
            double time = Double.parseDouble(planTime)*60;//计划值
            String str = Integer.parseInt(planTime)+"\t"+"min";
            tvPlan.setText(str);
            percentage = new BigDecimal(seconds/time*100).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            if (percentage<100)
            {
                progress = (int)(percentage);
            }else {
                progress = 100;
                percentage = 100;
                //震动提醒
                if (!isOk)//false说明第一次震动
                {
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(3000);
                    isOk = true;//下次不再震动
                }
            }
            tvPercent.setText(String.valueOf(percentage));
            progressBar.setProgress(progress);
        }
        if (flag == 3)//热量模式
        {
            double calorie = Double.parseDouble(planCal);//计划值
            String str = Integer.parseInt(planCal)+"\t"+"kcal";
            tvPlan.setText(str);
            double cal = MotionUtils.calculationCalorie(Integer.parseInt(heavy), distance/1000);
            percentage = new BigDecimal(cal/calorie*100).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            if (percentage<100)
            {
                progress = (int)(percentage);
            }else {
                progress = 100;
                percentage = 100;
                //震动提醒
                if (!isOk)//false说明第一次震动
                {
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(3000);
                    isOk = true;//下次不再震动
                }
            }
            tvPercent.setText(String.valueOf(percentage));
            progressBar.setProgress(progress);
        }
    }

    //计算并存储记录奖励值
    private void saveReward() throws GeneralSecurityException, IOException {
        //获取旧奖励值
        String oldReward = SecuritySP.DecryptSP(context,"reward");
        if (oldReward.equals("")) oldReward = "0";
        //获取新奖励值
        String reward = getReward();
        int num = Integer.parseInt(oldReward)+Integer.parseInt(reward);
        //更新存储奖励值
        SecuritySP.EncryptSP(context,"reward",String.valueOf(num));
    }
    public String getReward() throws GeneralSecurityException, IOException {
        //获取计划值
        String planDist = SecuritySP.DecryptSP(context, "dist");
        String planTime = SecuritySP.DecryptSP(context, "time");
        String planCal = SecuritySP.DecryptSP(context, "cal");
        String heavy = SecuritySP.DecryptSP(context,"heavy");
        double dist = Double.parseDouble(planDist);//计划值
        double time = Double.parseDouble(planTime)*60;//计划值
        double calorie = Double.parseDouble(planCal);//计划值
        //获取当前值
        double now_dist = distance;//单位：米
        double now_time = seconds;//单位：秒
        double sportMile = distance / 1000d;
        double now_cal = MotionUtils.calculationCalorie(Integer.parseInt(heavy), sportMile);;//单位：千卡
        /*
        奖励值计算方式：（60分钟跑6000+米消耗600千卡）
        1.里程：1000~2000m  +2          2000~4000m   +4           4000~6000m   +6           6000m以上   +8
        2.时长：5~20min     +3          20~40min     +6           40~60min     +9           60min以上   +12
        3.热量：50~200kcal  +3          200~400kcal  +6           400~600kcal  +9           600kcal以上 +12
        4.任务：完成1项任务   +1          完成2项任务    +3           完成3项任务    +5
         */
        //①里程
        int reward1;
        if (now_dist < 1000) {
            reward1 = 0;
        }else if (now_dist >= 1000 && now_dist < 2000){
            reward1 = 2;
        }else if (now_dist >= 2000 && now_dist < 4000){
            reward1 = 4;
        }else if (now_dist >= 4000 && now_dist < 6000){
            reward1 = 6;
        }else reward1 = 8;
        //②时长
        int reward2;
        if (now_time < 300) {
            reward2 = 0;
        }else if (now_time >= 300 && now_time < 1200){
            reward2 = 3;
        }else if (now_time >= 1200 && now_time < 2400){
            reward2 = 6;
        }else if (now_time >= 2400 && now_time < 3600){
            reward2 = 9;
        }else reward2 = 12;
        //③热量
        int reward3;
        if (now_cal < 50) {
            reward3 = 0;
        }else if (now_cal >= 50 && now_cal < 200){
            reward3 = 3;
        }else if (now_cal >= 200 && now_cal < 400){
            reward3 = 6;
        }else if (now_cal >= 400 && now_cal < 600){
            reward3 = 9;
        }else reward3 = 12;
        //④完成任务数
        int reward4,num = 0;
        if (now_dist > dist) num+=1;
        if (now_time > time) num+=1;
        if (now_cal > calorie) num+=1;
        if (num == 0) {
            reward4 = 0;
        } else if (num == 1){
            reward4 = 1;
        } else if (num == 2){
            reward4 = 3;
        } else {
            reward4 = 5;
        }

        return String.valueOf(reward1+reward2+reward3+reward4);
    }

}
