package com.james.motion.ui.activity;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.james.motion.R;
import com.james.motion.R2;
import com.james.motion.commmon.bean.SportMotionRecord;
import com.james.motion.commmon.utils.LogUtils;
import com.james.motion.commmon.utils.MySp;
import com.james.motion.db.DataManager;
import com.james.motion.db.RealmHelper;
import com.james.motion.ui.BaseActivity;
import com.james.motion.ui.permission.PermissionHelper;
import com.james.motion.ui.permission.PermissionListener;
import com.james.motion.ui.permission.Permissions;
import com.james.motion.ui.weight.SecuritySP;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.DecimalFormat;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 描述: 运动轨迹
 * 类名: SportsActivity
 */
public class SportsActivity extends BaseActivity {

    @BindView(R2.id.tv_sport_mile)
    TextView tvSportMile;
    @BindView(R2.id.tv_sport_count)
    TextView tvSportCount;
    @BindView(R2.id.tv_sport_time)
    TextView tvSportTime;
    @BindView(R2.id.tv_sport_cal)
    TextView tvSportCal;
//    @BindView(R2.id.btStart)
//    Button btStart;
    @BindView(R2.id.tip1)
    TextView tip1;
    @BindView(R2.id.tip2)
    TextView tip2;
    @BindView(R2.id.tip3)
    TextView tip3;

    @BindView(R2.id.mode1)
    TextView mode1;
    @BindView(R2.id.mode2)
    TextView mode2;
    @BindView(R2.id.mode1By1)
    TextView mode1_1;
    @BindView(R2.id.mode1By2)
    TextView mode1_2;
    @BindView(R2.id.mode1By3)
    TextView mode1_3;
    @BindView(R2.id.modeBack)
    TextView modeBack;

    //动画
    private ValueAnimator apperaAnim1;
    private ValueAnimator hiddenAnim1;

    private ValueAnimator apperaAnim2;
    private ValueAnimator hiddenAnim2;

    private ValueAnimator apperaAnim3;
    private ValueAnimator hiddenAnim3;

    private ValueAnimator apperaAnim4;
    private ValueAnimator hiddenAnim4;

    private ValueAnimator apperaAnim5;
    private ValueAnimator hiddenAnim5;

    private ValueAnimator apperaAnim6;
    private ValueAnimator hiddenAnim6;

    private DecimalFormat decimalFormat = new DecimalFormat("0.00");

    private final int SPORT = 0x0012;

    private DataManager dataManager = null;

    @Override
    public int getLayoutId() {
        return R.layout.activity_sports;
    }

    @Override
    public void initData(Bundle savedInstanceState) throws GeneralSecurityException, IOException {
        dataManager = new DataManager(new RealmHelper());
        getRunTips();
        upDateUI();
    }

    @Override
    public void initListener() {

    }




    @OnClick({R2.id.mode1,R2.id.mode2,R2.id.mode1By1,R2.id.mode1By2,R2.id.mode1By3,R2.id.modeBack})
    public void onViewClicked(View view) {
        int id = view.getId();
        Intent intent = new Intent(this, SportMapActivity.class);
        Bundle bundle = new Bundle();

        if (id==R.id.mode2)
        PermissionHelper.requestPermissions(this, Permissions.PERMISSIONS_LOCATION,
                getResources().getString(R.string.app_name) + "需要获取位置", new PermissionListener() {
                    @Override
                    public void onPassed() {
                        bundle.putInt("mode", 0);//0：随心跑
                        intent.putExtras(bundle);
                        startActivityForResult(intent, SPORT);
                    }
                });
        if (id==R.id.mode1) {
            mode1_1.setVisibility(View.VISIBLE);
            mode1_2.setVisibility(View.VISIBLE);
            mode1_3.setVisibility(View.VISIBLE);
            modeBack.setVisibility(View.VISIBLE);
            apperaAnim3.start();
            apperaAnim4.start();
            apperaAnim5.start();
            apperaAnim6.start();
            hiddenAnim2.start();
            hiddenAnim1.start();
        }
        if (id==R.id.mode1By1) {
            PermissionHelper.requestPermissions(this, Permissions.PERMISSIONS_LOCATION,
                    getResources().getString(R.string.app_name) + "需要获取位置", new PermissionListener() {
                        @Override
                        public void onPassed() {
                            bundle.putInt("mode", 1);//1：计划跑——里程
                            intent.putExtras(bundle);
                            startActivityForResult(intent, SPORT);
                            hiddenAnim3.start();
                            hiddenAnim4.start();
                            hiddenAnim5.start();
                            hiddenAnim6.start();
                            apperaAnim1.start();
                            apperaAnim2.start();
                        }
                    });
        }
        if (id==R.id.mode1By2) {
            PermissionHelper.requestPermissions(this, Permissions.PERMISSIONS_LOCATION,
                    getResources().getString(R.string.app_name) + "需要获取位置", new PermissionListener() {
                        @Override
                        public void onPassed() {
                            bundle.putInt("mode", 2);//2：计划跑--时长
                            intent.putExtras(bundle);
                            startActivityForResult(intent, SPORT);
                            hiddenAnim3.start();
                            hiddenAnim4.start();
                            hiddenAnim5.start();
                            hiddenAnim6.start();
                            apperaAnim1.start();
                            apperaAnim2.start();
                        }
                    });
        }
        if (id==R.id.mode1By3) {
            PermissionHelper.requestPermissions(this, Permissions.PERMISSIONS_LOCATION,
                    getResources().getString(R.string.app_name) + "需要获取位置", new PermissionListener() {
                        @Override
                        public void onPassed() {
                            bundle.putInt("mode", 3);//3：计划跑--卡路里
                            intent.putExtras(bundle);
                            startActivityForResult(intent, SPORT);
                            hiddenAnim3.start();
                            hiddenAnim4.start();
                            hiddenAnim5.start();
                            hiddenAnim6.start();
                            apperaAnim1.start();
                            apperaAnim2.start();
                        }
                    });
        }
        if (id==R.id.modeBack) {
            hiddenAnim3.start();
            hiddenAnim4.start();
            hiddenAnim5.start();
            hiddenAnim6.start();
            apperaAnim1.start();
            apperaAnim2.start();
        }
    }


    private void upDateUI() throws GeneralSecurityException, IOException {
        try {
            List<SportMotionRecord> records = dataManager.queryRecordList(Integer.parseInt(SPUtils.getInstance().getString(MySp.USERID, "0")));
            if (null != records) {

                double sportMile = 0;
                long sportTime = 0;
                double sportCal = 0;
                for (SportMotionRecord record : records) {
                    sportMile += record.getDistance();
                    sportTime += record.getDuration();
                    sportCal += record.getCalorie();
                }
                tvSportMile.setText(decimalFormat.format(sportMile / 1000d));
                tvSportCount.setText(String.valueOf(records.size()));
                tvSportTime.setText(decimalFormat.format((double) sportTime / 60d));
                tvSportCal.setText(decimalFormat.format(sportCal));
            }
        } catch (Exception e) {
            LogUtils.e("获取运动数据失败", e);
        }
        String planDist = SecuritySP.DecryptSP(context,"dist");
        String planTime = SecuritySP.DecryptSP(context,"time");
        String planCal = SecuritySP.DecryptSP(context,"cal");
        //判空，给默认值，防止闪退，并保存在本地
        if (planDist.equals("")) planDist = "100";//默认计划100
        if (planTime.equals("")) planTime= "1";//默认计划1分钟
        if (planCal.equals("")) planCal = "10";//默认计划10千卡
        SecuritySP.EncryptSP(context,"dist",planDist);
        SecuritySP.EncryptSP(context,"time",planTime);
        SecuritySP.EncryptSP(context,"cal",planCal);
        String str1 = (Double.parseDouble(planDist)/1000)+"\n"+"公里";
        String str2 = Integer.parseInt(planTime)+"\n"+"分钟";
        String str3 = Integer.parseInt(planCal)+"\n"+"卡";
        mode1_1.setText(str1);
        mode1_2.setText(str2);
        mode1_3.setText(str3);
        mode1_1.setVisibility(View.INVISIBLE);
        mode1_2.setVisibility(View.INVISIBLE);
        mode1_3.setVisibility(View.INVISIBLE);
        modeBack.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK)
            return;
        switch (requestCode) {
            case SPORT:
                try {
                    upDateUI();
                } catch (GeneralSecurityException | IOException e) {
                    e.printStackTrace();
                }
                setResult(RESULT_OK);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {

        // 清除动画，如果有
        if (mode1 != null) mode1.clearAnimation();
        if (mode2 != null) mode2.clearAnimation();
        if (mode1_1 != null) mode1_1.clearAnimation();
        if (mode1_2 != null) mode1_2.clearAnimation();
        if (mode1_3 != null) mode1_3.clearAnimation();
        if (modeBack != null) modeBack.clearAnimation();


        if (null != dataManager)
            dataManager.closeRealm();

        super.onDestroy();
    }


    //跑步方法
    public void getRunTips() throws GeneralSecurityException, IOException {
        String tall = SecuritySP.DecryptSP(this,"tall");
        String heavy = SecuritySP.DecryptSP(this,"heavy");
        float bmi = Float.parseFloat(heavy)/(Float.parseFloat(tall)*Float.parseFloat(tall)/10000);
        tip1.setText(getTimeByBMI(bmi));
        tip3.setText(getTipByBMI(bmi));
    }
    public static String getTipByBMI(float bmi) {
        if (bmi <= 18.4) {
            return "运动过程中适当补充营养，并逐步增加跑步的时间，减少走路的时间。";
        } else if (bmi > 18.4 && bmi <= 23.9) {
            return "运动养生，强身健体，注意拉伸腿部，避免乳酸堆积，引起肌肉疼痛。";
        } else if (bmi > 23.9 && bmi <= 27.9) {
            return "请配合一定强度的力量训练，这样可以起到瘦身、减脂、塑形、增强肌肉的作用。";
        } else {
            return "您的体质不适合跑步运动，跑步可能会对您的膝关节、踝关节，以及足部各软组织结构产生严重损伤。";
        }
    }
    public static String getTimeByBMI(float bmi) {
        if (bmi <= 18.4) {
            return "请保持30到60分钟的运动时间；";
        } else if (bmi > 18.4 && bmi <= 23.9) {
            return "请保持40到90分钟的运动时间；";
        } else if (bmi > 23.9 && bmi <= 27.9) {
            return "请保持45到95分钟的运动时间；";
        } else {
            return "跑步并非唯一的的锻炼方式；";
        }
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

        apperaAnim1 = ValueAnimator.ofFloat(mode1.getHeight() * 3, 0);
        apperaAnim1.setDuration(500);
        apperaAnim1.setTarget(mode1);
        apperaAnim1.addUpdateListener(animation -> mode1.setTranslationY((Float) animation.getAnimatedValue()));
        apperaAnim1.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mode1.setEnabled(false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mode1.setEnabled(true);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mode1.setEnabled(true);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });


        apperaAnim2 = ValueAnimator.ofFloat(mode2.getHeight() * 3, 0);
        apperaAnim2.setDuration(500);
        apperaAnim2.setTarget(mode2);
        apperaAnim2.addUpdateListener(animation -> mode2.setTranslationY((Float) animation.getAnimatedValue()));
        apperaAnim2.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mode2.setEnabled(false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mode2.setEnabled(true);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mode2.setEnabled(true);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        apperaAnim3 = ValueAnimator.ofFloat(mode1_1.getHeight() * 3, 0);
        apperaAnim3.setDuration(500);
        apperaAnim3.setTarget(mode1_1);
        apperaAnim3.addUpdateListener(animation -> mode1_1.setTranslationY((Float) animation.getAnimatedValue()));
        apperaAnim3.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mode1_1.setEnabled(false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mode1_1.setEnabled(true);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mode1_1.setEnabled(true);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        apperaAnim4 = ValueAnimator.ofFloat(mode1_2.getHeight() * 3, 0);
        apperaAnim4.setDuration(500);
        apperaAnim4.setTarget(mode1_2);
        apperaAnim4.addUpdateListener(animation -> mode1_2.setTranslationY((Float) animation.getAnimatedValue()));
        apperaAnim4.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mode1_2.setEnabled(false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mode1_2.setEnabled(true);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mode1_2.setEnabled(true);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        apperaAnim5 = ValueAnimator.ofFloat(mode1_3.getHeight() * 3, 0);
        apperaAnim5.setDuration(500);
        apperaAnim5.setTarget(mode1_3);
        apperaAnim5.addUpdateListener(animation -> mode1_3.setTranslationY((Float) animation.getAnimatedValue()));
        apperaAnim5.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mode1_3.setEnabled(false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mode1_3.setEnabled(true);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mode1_3.setEnabled(true);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        apperaAnim6 = ValueAnimator.ofFloat(modeBack.getHeight() * 3, 0);
        apperaAnim6.setDuration(500);
        apperaAnim6.setTarget(modeBack);
        apperaAnim6.addUpdateListener(animation -> modeBack.setTranslationY((Float) animation.getAnimatedValue()));
        apperaAnim6.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                modeBack.setEnabled(false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                modeBack.setEnabled(true);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                modeBack.setEnabled(true);
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

        hiddenAnim1 = ValueAnimator.ofFloat(0, mode1.getHeight() * 3);
        hiddenAnim1.setDuration(500);
        hiddenAnim1.setTarget(mode1);
        hiddenAnim1.addUpdateListener(animation -> mode1.setTranslationY((Float) animation.getAnimatedValue()));
        hiddenAnim1.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mode1.setEnabled(false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mode1.setEnabled(true);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mode1.setEnabled(true);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });


        hiddenAnim2 = ValueAnimator.ofFloat(0, mode2.getHeight() * 3);
        hiddenAnim2.setDuration(500);
        hiddenAnim2.setTarget(mode2);
        hiddenAnim2.addUpdateListener(animation -> mode2.setTranslationY((Float) animation.getAnimatedValue()));
        hiddenAnim2.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mode2.setEnabled(false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mode2.setEnabled(true);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mode2.setEnabled(true);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        hiddenAnim3 = ValueAnimator.ofFloat(0, mode1_1.getHeight() * 3);
        hiddenAnim3.setDuration(500);
        hiddenAnim3.setTarget(mode1_1);
        hiddenAnim3.addUpdateListener(animation -> mode1_1.setTranslationY((Float) animation.getAnimatedValue()));
        hiddenAnim3.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mode1_1.setEnabled(false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mode1_1.setEnabled(true);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mode1_1.setEnabled(true);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        hiddenAnim4 = ValueAnimator.ofFloat(0, mode1_2.getHeight() * 3);
        hiddenAnim4.setDuration(500);
        hiddenAnim4.setTarget(mode1_2);
        hiddenAnim4.addUpdateListener(animation -> mode1_2.setTranslationY((Float) animation.getAnimatedValue()));
        hiddenAnim4.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mode1_2.setEnabled(false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mode1_2.setEnabled(true);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mode1_2.setEnabled(true);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        hiddenAnim5 = ValueAnimator.ofFloat(0, mode1_3.getHeight() * 3);
        hiddenAnim5.setDuration(500);
        hiddenAnim5.setTarget(mode1_3);
        hiddenAnim5.addUpdateListener(animation -> mode1_3.setTranslationY((Float) animation.getAnimatedValue()));
        hiddenAnim5.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mode1_3.setEnabled(false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mode1_3.setEnabled(true);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mode1_3.setEnabled(true);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        hiddenAnim6 = ValueAnimator.ofFloat(0, modeBack.getHeight() * 3);
        hiddenAnim6.setDuration(500);
        hiddenAnim6.setTarget(modeBack);
        hiddenAnim6.addUpdateListener(animation -> modeBack.setTranslationY((Float) animation.getAnimatedValue()));
        hiddenAnim6.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                modeBack.setEnabled(false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                modeBack.setEnabled(true);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                modeBack.setEnabled(true);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }


}
