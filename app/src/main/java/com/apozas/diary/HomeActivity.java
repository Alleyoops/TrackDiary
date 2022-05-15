package com.apozas.diary;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;

import com.example.epidemicbigdata.MainActivity2;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class HomeActivity extends AppCompatActivity {

    @SuppressLint({"SetJavaScriptEnabled", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE); // 注意顺序
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_home);
        HideBar.hideBar(this);
        WebView webView = findViewById(R.id.web2);
        String url = "https://news.quark.cn/dailynews/v2/newslist?&aggId=8516309615177202851";
        webView.loadUrl(url);
        WebSettings mWebSettings = webView.getSettings();
        mWebSettings.setJavaScriptCanOpenWindowsAutomatically(true);//设置js可以直接打开窗口，如window.open()，默认为false
        mWebSettings.setJavaScriptEnabled(true);//是否允许JavaScript脚本运行，默认为false。设置true时，会提醒可能造成XSS漏洞
        mWebSettings.setUseWideViewPort(true);//设置此属性，可任意比例缩放。大视图模式
        mWebSettings.setLoadWithOverviewMode(true);//和setUseWideViewPort(true)一起解决网页自适应问题
        mWebSettings.setAppCacheEnabled(true);//是否使用缓存Quark//
        mWebSettings.setDomStorageEnabled(true);//开启本地DOM存储
        mWebSettings.setLoadsImagesAutomatically(true); // 加载图片
        mWebSettings.setMediaPlaybackRequiresUserGesture(false);//播放音频，多媒体需要用户手动？设置为false为可自动播放
        mWebSettings.setGeolocationEnabled(true);
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        //禁止触摸事件
        webView.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        PressAnimUtil.addScaleAnimition(findViewById(R.id.card1), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent().setClass(HomeActivity.this,MainActivity.class));
                    }
                },200);

            }
        },0.8f);
        PressAnimUtil.addScaleAnimition(findViewById(R.id.card2), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent().setClass(HomeActivity.this,MainActivity2.class));
                    }
                },200);

            }
        },0.8f);
        PressAnimUtil.addScaleAnimition(findViewById(R.id.card3), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent().setClass(HomeActivity.this,QuarkActivity.class));
                    }
                },200);

            }
        },0.8f);
        PressAnimUtil.addScaleAnimition(findViewById(R.id.card4), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),"Coming···",Toast.LENGTH_SHORT).show();
                    }
                },200);

            }
        },0.8f);



    }
    // 按返回键不销毁当前Activity
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == android.view.KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}