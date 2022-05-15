package com.apozas.diary;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;
import java.security.GeneralSecurityException;


public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //全屛显示，再setContentView
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_welcome);
        HideBar.setStatusBarTranslucent(this);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    checkLogin();//自动登录
                } catch (GeneralSecurityException | IOException e) {
                    e.printStackTrace();
                }
            }
        },1500);
    }




    private void checkLogin() throws GeneralSecurityException, IOException {
        startActivity(new Intent(WelcomeActivity.this,HomeActivity.class));
    }

}