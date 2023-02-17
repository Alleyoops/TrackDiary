package com.james.motion.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.flyco.tablayout.SlidingTabLayout;
import com.james.motion.MyApplication;
import com.james.motion.R;
import com.james.motion.R2;
import com.james.motion.commmon.utils.Conn;
import com.james.motion.commmon.utils.MySp;
import com.james.motion.db.DataManager;
import com.james.motion.db.RealmHelper;
import com.james.motion.ui.BaseActivity;
import com.james.motion.ui.fragment.FastLoginFragment;
import com.james.motion.ui.fragment.PsdLoginFragment;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;

public class LoginActivity extends BaseActivity {

    @BindView(R2.id.slidingTabLayout)
    SlidingTabLayout slidingTabLayout;
    @BindView(R2.id.vp)
    ViewPager vp;
    @BindView(R2.id.btLogin)
    Button btLogin;
    @BindView(R2.id.btReg)
    Button btReg;
    @BindView(R2.id.wcLogin)
    ImageButton wcLogin;
    @BindView(R2.id.qqLogin)
    ImageButton qqLogin;

    /**
     * 上次点击返回键的时间
     */
    private long lastBackPressed;

    //上次点击返回键的时间
    public static final int QUIT_INTERVAL = 2500;

    private final String[] mTitles = {"普通登录", "快速登录"};

    private ArrayList<Fragment> mFragments = new ArrayList<>();

    private boolean isPsd = true;//是否是密码登录

    private PsdLoginFragment psdLoginFragment = new PsdLoginFragment();
    private FastLoginFragment fastLoginFragment = new FastLoginFragment();

    private DataManager dataManager = null;

    @Override
    public int getLayoutId() {
        return R.layout.activity_login;
    }

    @Override
    public void initData(Bundle savedInstanceState) {

        dataManager = new DataManager(new RealmHelper());

        MyPagerAdapter mAdapter = new MyPagerAdapter(getSupportFragmentManager());
        vp.setAdapter(mAdapter);

        mFragments.add(psdLoginFragment);
        mFragments.add(fastLoginFragment);

        slidingTabLayout.setViewPager(vp, mTitles, this, mFragments);

        isPsd = true;
    }

    @Override
    public void initListener() {
        vp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                isPsd = i == 0;
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }

    @OnClick({R2.id.container, R2.id.btLogin, R2.id.btReg, R2.id.qqLogin, R2.id.wcLogin})
    public void onViewClicked(View view) {
        int id = view.getId();
        if (id == R.id.container) {
            hideSoftKeyBoard();
        } else if (id == R.id.btLogin) {
            hideSoftKeyBoard();
            if (isPsd) {
                psdLoginFragment.checkAccount(this::login);
            } else {
                fastLoginFragment.checkAccount(this::login);
            }
        } else if (id == R.id.btReg) {
            hideSoftKeyBoard();
            startActivity(new Intent(LoginActivity.this, RegistActivity.class));
        } else if (id == R.id.qqLogin || id == R.id.wcLogin) {
            ToastUtils.showShort("功能开发中....");
        }
    }

    /**
     * 登录
     */
    public void login(String account, String psd) {
        btLogin.setEnabled(false);
        showLoadingView();
        new Handler().postDelayed(() -> {
            dismissLoadingView();
            btLogin.setEnabled(true);
            if (isPsd) {
                if (dataManager.checkAccount(account, psd))
                    loginSuccess(account, psd);
                else
                    ToastUtils.showShort("账号或密码错误!");
            } else {
                if (dataManager.checkAccount(account))
                    loginSuccess(account, "");
                else
                    ToastUtils.showShort("账号不存在!");
            }
        }, Conn.Delayed);
    }

    private void loginSuccess(String account, String psd) {
        SPUtils.getInstance().put(MySp.ISLOGIN, true);

        SPUtils.getInstance().put(MySp.USERID, account.substring(8));

        SPUtils.getInstance().put(MySp.PHONE, account);
        SPUtils.getInstance().put(MySp.PASSWORD, psd);

        Intent intent = new Intent();
        intent.setAction( "my.android.action.test");
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        startActivity(intent);

        //startActivity(new Intent(LoginActivity.this, HomepageActivity.class));
        ToastUtils.showShort("恭喜您,登录成功...");

        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_BACK) { // 表示按返回键 时的操作
                long backPressed = System.currentTimeMillis();
                if (backPressed - lastBackPressed > QUIT_INTERVAL) {
                    lastBackPressed = backPressed;
                    ToastUtils.showShort("再按一次退出");
                } else {
                    moveTaskToBack(false);
                    MyApplication.closeApp(this);
                    finish();
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private class MyPagerAdapter extends FragmentPagerAdapter {
        MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitles[position];
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }
    }

    @Override
    protected void onDestroy() {
        if (null != dataManager)
            dataManager.closeRealm();
        super.onDestroy();
    }

}
