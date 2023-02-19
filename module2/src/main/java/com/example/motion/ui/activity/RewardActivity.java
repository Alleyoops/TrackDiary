package com.example.motion.ui.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.example.motion.R;
import com.example.motion.R2;
import com.example.motion.ui.BaseActivity;
import com.example.motion.ui.weight.SecuritySP;

import java.io.IOException;
import java.security.GeneralSecurityException;

import butterknife.BindView;

public class RewardActivity extends BaseActivity {
    @BindView(R2.id.tv_reward)
    TextView tvReward;

    @Override
    public int getLayoutId() {
        return R.layout.activity_reward;
    }

    @Override
    public void initData(Bundle savedInstanceState) throws GeneralSecurityException, IOException {
        setReward();
    }

    @Override
    public void initListener() {

    }
    private void setReward() throws GeneralSecurityException, IOException {
        String reward = SecuritySP.DecryptSP(context,"reward");
        if (reward.equals("")) reward = "0";//判空
        tvReward.setText(reward);
    }
}