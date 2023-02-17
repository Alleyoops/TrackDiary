package com.james.motion.ui.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.james.motion.R;
import com.james.motion.R2;
import com.james.motion.ui.BaseActivity;
import com.james.motion.ui.weight.SecuritySP;

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