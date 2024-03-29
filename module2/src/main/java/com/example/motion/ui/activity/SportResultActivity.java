package com.example.motion.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.example.motion.R;
import com.example.motion.R2;
import com.example.motion.commmon.bean.PathRecord;
import com.example.motion.commmon.bean.SportMotionRecord;
import com.example.motion.commmon.utils.LogUtils;
import com.example.motion.commmon.utils.MySp;
import com.example.motion.commmon.utils.UIHelper;
import com.example.motion.db.DataManager;
import com.example.motion.db.RealmHelper;
import com.example.motion.sport_motion.MotionUtils;
import com.example.motion.sport_motion.PathSmoothTool;
import com.example.motion.ui.BaseActivity;
import com.example.motion.ui.weight.CustomPopWindow;
import com.example.motion.ui.weight.SecuritySP;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 描述: 运动结果
 * 类名: SportResultActivity
 */
public class SportResultActivity extends BaseActivity {

    @BindView(R2.id.ivStar1)
    ImageView ivStar1;
    @BindView(R2.id.ivStar2)
    ImageView ivStar2;
    @BindView(R2.id.ivStar3)
    ImageView ivStar3;
    @BindView(R2.id.tvResult)
    TextView tvResult;
    @BindView(R2.id.tvDistancet)
    TextView tvDistancet;
    @BindView(R2.id.tvDuration)
    TextView tvDuration;
    @BindView(R2.id.tvCalorie)
    TextView tvCalorie;
    @BindView(R2.id.mapView)
    MapView mapView;

    private String planDist,planTime,planCal,heavy;
    private int modeFlag;

    private DecimalFormat decimalFormat = new DecimalFormat("0.00");
    private DecimalFormat intFormat = new DecimalFormat("#");

    private final int AMAP_LOADED = 0x0088;

    private Handler handler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case AMAP_LOADED:
                    setupRecord();
                    break;
                default:
                    break;
            }
        }
    };

    private AMap aMap;

    private PathRecord pathRecord = null;

    private DataManager dataManager = null;

    private ExecutorService mThreadPool;
    private List<LatLng> mOriginLatLngList;
    private Marker mOriginStartMarker, mOriginEndMarker;
    private Polyline mOriginPolyline;
    private PathSmoothTool mpathSmoothTool = null;
    private PolylineOptions polylineOptions;

    public static String SPORT_START = "SPORT_START";
    public static String SPORT_END = "SPORT_END";

    public static void StartActivity(Activity activity, long mStartTime, long mEndTime) {
        Intent intent = new Intent();
        intent.putExtra(SPORT_START, mStartTime);
        intent.putExtra(SPORT_END, mEndTime);
        intent.setClass(activity, SportResultActivity.class);
        activity.startActivity(intent);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_sportresult;
    }

    @Override
    public void initData(Bundle savedInstanceState) {

        mapView.onCreate(savedInstanceState);// 此方法必须重写

        dataManager = new DataManager(new RealmHelper());

        if (!getIntent().hasExtra(SPORT_START) || !getIntent().hasExtra(SPORT_END)) {
            ToastUtils.showShort("参数错误!");
            finish();
        }

        int threadPoolSize = Runtime.getRuntime().availableProcessors() * 2 + 3;
        mThreadPool = Executors.newFixedThreadPool(threadPoolSize);

        initPolyline();

        if (aMap == null)
            aMap = mapView.getMap();

        setUpMap();

    }

    private void initPolyline() {
        polylineOptions = new PolylineOptions();
        polylineOptions.color(getResources().getColor(R.color.colorAccent));
        polylineOptions.width(20f);
        polylineOptions.useGradient(true);

        mpathSmoothTool = new PathSmoothTool();
        mpathSmoothTool.setIntensity(4);
    }

    private void setupRecord() {
        try {
            SportMotionRecord records = dataManager.queryRecord(
                    Integer.parseInt(SPUtils.getInstance().getString(MySp.USERID, "0")),
                    getIntent().getLongExtra(SPORT_START, 0),
                    getIntent().getLongExtra(SPORT_END, 0));
            if (null != records) {
                pathRecord = new PathRecord();
                pathRecord.setId(records.getId());
                pathRecord.setDistance(records.getDistance());
                pathRecord.setDuration(records.getDuration());
                pathRecord.setPathline(MotionUtils.parseLatLngLocations(records.getPathLine()));
                pathRecord.setStartpoint(MotionUtils.parseLatLngLocation(records.getStratPoint()));
                pathRecord.setEndpoint(MotionUtils.parseLatLngLocation(records.getEndPoint()));
                pathRecord.setStartTime(records.getmStartTime());
                pathRecord.setEndTime(records.getmEndTime());
                pathRecord.setCalorie(records.getCalorie());
                pathRecord.setSpeed(records.getSpeed());
                pathRecord.setDistribution(records.getDistribution());
                pathRecord.setDateTag(records.getDateTag());

                upDataUI();
            } else {
                pathRecord = null;
                ToastUtils.showShort("获取运动数据失败!");
            }
        } catch (Exception e) {
            pathRecord = null;
            ToastUtils.showShort("获取运动数据失败!");
            LogUtils.e("获取运动数据失败", e);
        }
    }
    //获取计划值
    public void setPlan() throws GeneralSecurityException, IOException {
        planDist = SecuritySP.DecryptSP(context,"dist");
        planTime = SecuritySP.DecryptSP(context,"time");
        planCal = SecuritySP.DecryptSP(context,"cal");
        heavy = SecuritySP.DecryptSP(context,"heavy");
    }
    private void upDataUI() throws GeneralSecurityException, IOException {
        tvDistancet.setText(decimalFormat.format(pathRecord.getDistance() / 1000d));
        tvDuration.setText(MotionUtils.formatseconds(pathRecord.getDuration()));
        tvCalorie.setText(intFormat.format(pathRecord.getCalorie()));

        //评分规则：分为计划跑和随心跑，前者按照任务划分等级，距离大于0为一星，70%为二星，100%为三星，后者根据跑步推荐的计划进行分级
        //1.先获取plan数值
        setPlan();
        //2.获取modeFlag
        modeFlag = Integer.parseInt(SecuritySP.DecryptSP(context,"modeFlag"));
        //3.计算比例
        double numOfDist,numOfTime,numOfCal;
        numOfDist = pathRecord.getDistance()/Double.parseDouble(planDist);
        numOfTime = pathRecord.getDuration()/(Double.parseDouble(planTime)*60);
        numOfCal = pathRecord.getCalorie()/Double.parseDouble(planCal);
        //4.根据flag判断分级标准
        if (modeFlag == 1)//里程模式
        {
            if (numOfDist>=0 &&numOfDist<0.7)
            {
                ivStar1.setImageResource(R.mipmap.small_no_star);
                ivStar2.setImageResource(R.mipmap.big_star);
                ivStar3.setImageResource(R.mipmap.small_no_star);
                tvResult.setText("跑步效果一般");
            }else if (numOfDist>=0.7&&numOfDist<1)
            {
                ivStar1.setImageResource(R.mipmap.small_star);
                ivStar2.setImageResource(R.mipmap.big_star);
                ivStar3.setImageResource(R.mipmap.small_no_star);
                tvResult.setText("跑步效果不错");
            } else {
                ivStar1.setImageResource(R.mipmap.small_star);
                ivStar2.setImageResource(R.mipmap.big_star);
                ivStar3.setImageResource(R.mipmap.small_star);
                tvResult.setText("跑步效果完美");
            }
        }
        if (modeFlag == 2)//里程模式
        {
            if (numOfTime>=0 &&numOfTime<0.7)
            {
                ivStar1.setImageResource(R.mipmap.small_no_star);
                ivStar2.setImageResource(R.mipmap.big_star);
                ivStar3.setImageResource(R.mipmap.small_no_star);
                tvResult.setText("跑步效果一般");
            }else if (numOfTime>=0.7&&numOfTime<1)
            {
                ivStar1.setImageResource(R.mipmap.small_star);
                ivStar2.setImageResource(R.mipmap.big_star);
                ivStar3.setImageResource(R.mipmap.small_no_star);
                tvResult.setText("跑步效果不错");
            } else {
                ivStar1.setImageResource(R.mipmap.small_star);
                ivStar2.setImageResource(R.mipmap.big_star);
                ivStar3.setImageResource(R.mipmap.small_star);
                tvResult.setText("跑步效果完美");
            }
        }
        if (modeFlag == 3)//里程模式
        {
            if (numOfCal>=0 &&numOfCal<0.7)
            {
                ivStar1.setImageResource(R.mipmap.small_no_star);
                ivStar2.setImageResource(R.mipmap.big_star);
                ivStar3.setImageResource(R.mipmap.small_no_star);
                tvResult.setText("跑步效果一般");
            }else if (numOfCal>=0.7&&numOfCal<1)
            {
                ivStar1.setImageResource(R.mipmap.small_star);
                ivStar2.setImageResource(R.mipmap.big_star);
                ivStar3.setImageResource(R.mipmap.small_no_star);
                tvResult.setText("跑步效果不错");
            } else {
                ivStar1.setImageResource(R.mipmap.small_star);
                ivStar2.setImageResource(R.mipmap.big_star);
                ivStar3.setImageResource(R.mipmap.small_star);
                tvResult.setText("跑步效果完美");
            }
        }
        if (modeFlag == 0)//随心跑
        {
            //先获取跑步计划的推荐值
            //but，有点问题，故此处评分规则：依次判断 距离大于0 ★；运动时间大于40分钟 ★★；速度在3~6km/h之间 ★★★
        if (pathRecord.getDuration() > (40 * 60) && pathRecord.getSpeed() > 3) {
            ivStar1.setImageResource(R.mipmap.small_star);
            ivStar2.setImageResource(R.mipmap.big_star);
            ivStar3.setImageResource(R.mipmap.small_star);
            tvResult.setText("跑步效果完美");
        } else if (pathRecord.getDuration() > (40 * 60)) {
            ivStar1.setImageResource(R.mipmap.small_star);
            ivStar2.setImageResource(R.mipmap.big_star);
            ivStar3.setImageResource(R.mipmap.small_no_star);
            tvResult.setText("跑步效果不错");
        } else {
            ivStar1.setImageResource(R.mipmap.small_no_star);
            ivStar2.setImageResource(R.mipmap.big_star);
            ivStar3.setImageResource(R.mipmap.small_no_star);
            tvResult.setText("跑步效果一般");
        }
        }


        {
            List<LatLng> recordList = pathRecord.getPathline();
            LatLng startLatLng = pathRecord.getStartpoint();
            LatLng endLatLng = pathRecord.getEndpoint();
            if (recordList == null || startLatLng == null || endLatLng == null) {
                return;
            }
            mOriginLatLngList = mpathSmoothTool.pathOptimize(recordList);
            addOriginTrace(startLatLng, endLatLng, mOriginLatLngList);
        }
    }

    @Override
    public void initListener() {
        aMap.setOnMapLoadedListener(() -> {
            Message msg = handler.obtainMessage();
            msg.what = AMAP_LOADED;
            handler.sendMessage(msg);
        });
    }

    @OnClick({R2.id.tvResult, R2.id.ll_share, R2.id.ll_details})
    public void onViewClicked(View view) {
        int id = view.getId();
        if (id == R.id.tvResult) {
            new CustomPopWindow.PopupWindowBuilder(this)
                    .setView(R.layout.layout_sport_result_tip)
                    .setFocusable(true)
                    .setOutsideTouchable(true)
                    .create()
                    .showAsDropDown(tvResult, -260, 10);
        } else if (id == R.id.ll_share) {
            if (null != pathRecord) {
                systemShareTxt();
            } else {
                ToastUtils.showShort("获取运动数据失败!");
            }
        } else if (id == R.id.ll_details) {
            if (null != pathRecord) {
                SportRecordDetailsActivity.StartActivity(this, pathRecord);
            } else {
                ToastUtils.showShort("获取运动数据失败!");
            }
        }
    }

    /**
     * 设置一些amap的属性
     */
    private void setUpMap() {
        // 自定义系统定位小蓝点
        MyLocationStyle myLocationStyle = new MyLocationStyle();
//        myLocationStyle.myLocationIcon(BitmapDescriptorFactory
//                .fromResource(R.drawable.mylocation_point));// 设置小蓝点的图标
        myLocationStyle.strokeColor(Color.TRANSPARENT);// 设置圆形的边框颜色
        myLocationStyle.radiusFillColor(Color.argb(0, 0, 0, 0));// 设置圆形的填充颜色
        // myLocationStyle.anchor(int,int)//设置小蓝点的锚点
        myLocationStyle.strokeWidth(1.0f);// 设置圆形的边框粗细
        aMap.setMyLocationStyle(myLocationStyle);
        aMap.getUiSettings().setMyLocationButtonEnabled(false);// 设置默认定位按钮是否显示
        aMap.getUiSettings().setScaleControlsEnabled(true);// 设置比例尺显示
        aMap.getUiSettings().setZoomControlsEnabled(false);// 设置默认缩放按钮是否显示
        aMap.getUiSettings().setCompassEnabled(false);// 设置默认指南针是否显示
        aMap.setMyLocationEnabled(false);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
    }

    /**
     * 地图上添加原始轨迹线路及起终点、轨迹动画小人
     *
     * @param startPoint
     * @param endPoint
     * @param originList
     */
    private void addOriginTrace(LatLng startPoint, LatLng endPoint,
                                List<LatLng> originList) {
        polylineOptions.addAll(originList);
        mOriginPolyline = aMap.addPolyline(polylineOptions);
        mOriginStartMarker = aMap.addMarker(new MarkerOptions().position(
                startPoint).icon(
                BitmapDescriptorFactory.fromResource(R.drawable.sport_start)));
        mOriginEndMarker = aMap.addMarker(new MarkerOptions().position(
                endPoint).icon(
                BitmapDescriptorFactory.fromResource(R.drawable.sport_end)));

        try {
            aMap.moveCamera(CameraUpdateFactory.newLatLngBounds(getBounds(), 16));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private LatLngBounds getBounds() {
        LatLngBounds.Builder b = LatLngBounds.builder();
        if (mOriginLatLngList == null) {
            return b.build();
        }
        for (LatLng latLng : mOriginLatLngList) {
            b.include(latLng);
        }
        return b.build();
    }

    /**
     * 调用系统分享文本
     */
    private void systemShareTxt() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, UIHelper.getString(R.string.app_name) + "运动");
        intent.putExtra(Intent.EXTRA_TEXT, "我在" + UIHelper.getString(R.string.app_name) + "运动跑了" + decimalFormat.format(pathRecord.getDistance())
                + "公里,运动了" + decimalFormat.format(pathRecord.getDuration() / 60) + "分钟!快来加入吧!");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(Intent.createChooser(intent, "分享到"));
    }

    /**
     * 调用系统分享图片
     */
    private void systemSharePic(String imagePath) {
        //由文件得到uri
        Uri imageUri = Uri.fromFile(new File(imagePath));
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        shareIntent.setType("image/*");
        startActivity(Intent.createChooser(shareIntent, "分享到"));
    }

    @Override
    protected void onDestroy() {
        mapView.onDestroy();

        if (mThreadPool != null)
            mThreadPool.shutdownNow();

        if (null != dataManager)
            dataManager.closeRealm();

        super.onDestroy();
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}
