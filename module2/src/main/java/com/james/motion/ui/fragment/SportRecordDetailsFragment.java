package com.james.motion.ui.fragment;

import android.graphics.Paint;
import android.os.Bundle;
import android.widget.TextView;

import com.james.motion.R;
import com.james.motion.R2;
import com.james.motion.commmon.bean.PathRecord;
import com.james.motion.sport_motion.MotionUtils;
import com.james.motion.ui.BaseFragment;
import com.james.motion.ui.activity.SportRecordDetailsActivity;
import com.james.motion.ui.weight.SecuritySP;
import com.king.view.circleprogressview.CircleProgressView;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.text.DecimalFormat;

import butterknife.BindView;

/**
 * 描述: 运动记录详情-详情
 * 类名: SportRecordDetailsFragment
 */
public class SportRecordDetailsFragment extends BaseFragment {

    @BindView(R2.id.tvDistance)
    TextView tvDistance;
    @BindView(R2.id.tvDuration)
    TextView tvDuration;
    @BindView(R2.id.tvSpeed)
    TextView tvSpeed;
    @BindView(R2.id.tvDistribution)
    TextView tvDistribution;
    @BindView(R2.id.tvCalorie)
    TextView tvCalorie;
    @BindView(R2.id.progress_dist)
    CircleProgressView progressDist;
    @BindView(R2.id.progress_time)
    CircleProgressView progressTime;
    @BindView(R2.id.progress_cal)
    CircleProgressView progressCal;



    private String planDist,planTime,planCal;
    private PathRecord pathRecord = null;

    private DecimalFormat decimalFormat = new DecimalFormat("0.00");
    private DecimalFormat intFormat = new DecimalFormat("#");

    @Override
    public int getLayoutId() {
        return R.layout.fragment_sportrecorddetails;
    }

    @Override
    public void initData(Bundle savedInstanceState) throws GeneralSecurityException, IOException {
        Bundle bundle = getArguments();
        if (bundle != null) {
            pathRecord = bundle.getParcelable(SportRecordDetailsActivity.SPORT_DATA);
        }

        if (null != pathRecord) {
            tvDistance.setText(decimalFormat.format(pathRecord.getDistance() / 1000d));
            tvDuration.setText(MotionUtils.formatseconds(pathRecord.getDuration()));
            tvSpeed.setText(decimalFormat.format(pathRecord.getSpeed()));
            tvDistribution.setText(decimalFormat.format(pathRecord.getDistribution()));
            tvCalorie.setText(intFormat.format(pathRecord.getCalorie()));
        }
        setProgressCircle();

    }

    @Override
    public void initListener() {

    }
    //设置进度条
    public void setProgressCircle() throws GeneralSecurityException, IOException {
        //获取计划值
        planDist = SecuritySP.DecryptSP(context,"dist");
        planTime = SecuritySP.DecryptSP(context,"time");
        planCal = SecuritySP.DecryptSP(context,"cal");
        double dist = Double.parseDouble(planDist);//计划值
        double time = Double.parseDouble(planTime)*60;//计划值
        double calorie = Double.parseDouble(planCal);//计划值
        //获取当前值
        double now_dist = pathRecord.getDistance();
        double now_time = pathRecord.getDuration();
        double now_cal = pathRecord.getCalorie();
        //计算百分比
        double percentage1 = 0d;
        double percentage2 = 0d;
        double percentage3 = 0d;
        percentage1 = new BigDecimal(now_dist/dist*100).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        percentage2 = new BigDecimal(now_time/time*100).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        percentage3 = new BigDecimal(now_cal/calorie*100).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        //显示
        if (percentage1<100){
            progressDist.setLabelText(String.valueOf((int)percentage1));//修改后不显示百分号
            progressDist.setProgress((int)percentage1);
        } else {
            progressDist.setLabelText(String.valueOf(100));
            progressDist.setProgress(100);
        }
        if (percentage2<100){
            progressTime.setLabelText(String.valueOf((int)percentage2));
            progressTime.setProgress((int)percentage2);
        } else {
            progressTime.setLabelText(String.valueOf(100));
            progressTime.setProgress(100);
        }
        if (percentage3<100){
            progressCal.setLabelText(String.valueOf((int)percentage3));
            progressCal.setProgress((int)percentage3);
        } else {
            progressCal.setLabelText(String.valueOf(100));
            progressCal.setProgress(100);
        }



    }




}
