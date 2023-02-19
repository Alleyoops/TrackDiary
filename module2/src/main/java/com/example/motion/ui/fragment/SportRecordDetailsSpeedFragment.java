package com.example.motion.ui.fragment;

import android.os.Bundle;
import android.widget.TextView;

import com.example.motion.R;
import com.example.motion.R2;
import com.example.motion.commmon.bean.PathRecord;
import com.example.motion.ui.BaseFragment;
import com.example.motion.ui.activity.SportRecordDetailsActivity;
import com.example.motion.ui.weight.SecuritySP;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.DecimalFormat;

import butterknife.BindView;

/**
 * 描述: 运动记录详情-奖励值
 * 类名: SportRecordDetailsSpeedFragment
 */
public class SportRecordDetailsSpeedFragment extends BaseFragment {

    @BindView(R2.id.tvReward)
    TextView tvReward;
    @BindView(R2.id.tvDist)
    TextView tvDist;
    @BindView(R2.id.tvTime)
    TextView tvTime;
    @BindView(R2.id.tvCal)
    TextView tvCal;
    @BindView(R2.id.tvFinishPlan)
    TextView tvFinish;

    private PathRecord pathRecord = null;

    private DecimalFormat decimalFormat = new DecimalFormat("0");

    @Override
    public int getLayoutId() {
        return R.layout.fragment_sportrecorddetailsspeed;
    }

    @Override
    public void initData(Bundle savedInstanceState) throws GeneralSecurityException, IOException {
        Bundle bundle = getArguments();
        if (bundle != null) {
            pathRecord = bundle.getParcelable(SportRecordDetailsActivity.SPORT_DATA);
        }

        if (null != pathRecord)
        {
            //tvDistribution.setText(decimalFormat.format(pathRecord.getDistribution()));
            setReward();
        }
    }

    @Override
    public void initListener() {

    }

    public void setReward() throws GeneralSecurityException, IOException {
        //获取计划值
        String planDist = SecuritySP.DecryptSP(context, "dist");
        String planTime = SecuritySP.DecryptSP(context, "time");
        String planCal = SecuritySP.DecryptSP(context, "cal");
        double dist = Double.parseDouble(planDist);//计划值
        double time = Double.parseDouble(planTime)*60;//计划值
        double calorie = Double.parseDouble(planCal);//计划值
        //获取当前值
        double now_dist = pathRecord.getDistance();//单位：米
        double now_time = pathRecord.getDuration();//单位：秒
        double now_cal = pathRecord.getCalorie();//单位：千卡
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
            tvDist.setTextColor(this.getResources().getColor(R.color.text_color_2));
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
            tvTime.setTextColor(this.getResources().getColor(R.color.text_color_2));
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
            tvCal.setTextColor(this.getResources().getColor(R.color.text_color_2));
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
            tvFinish.setTextColor(this.getResources().getColor(R.color.text_color_2));
        } else if (num == 1){
            reward4 = 1;
        } else if (num == 2){
            reward4 = 3;
        } else {
            reward4 = 5;
        }

        //赋值
        tvDist.setText(String.valueOf(reward1));
        tvTime.setText(String.valueOf(reward2));
        tvCal.setText(String.valueOf(reward3));
        tvFinish.setText(String.valueOf(reward4));
        tvReward.setText(String.valueOf(reward1+reward2+reward3+reward4));

    }

}
