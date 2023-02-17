package com.james.motion.ui.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.ToastUtils;
import com.james.motion.R;
import com.james.motion.R2;
import com.james.motion.ui.BaseActivity;
import com.james.motion.ui.weight.SecuritySP;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.GeneralSecurityException;

import butterknife.BindView;
import butterknife.OnClick;

public class PlanActivity extends BaseActivity {

    @BindView(R2.id.tv_title)
    TextView tvTitle;
    @BindView(R2.id.tv_tall)
    TextView tvTall;
    @BindView(R2.id.tv_heavy)
    TextView tvHeavy;
    @BindView(R2.id.tv_age)
    TextView tvAge;
    @BindView(R2.id.tv_bmi)
    TextView tvBMI;
    @BindView(R2.id.tv_bodyStyle)
    TextView tvBodyStyle;
    @BindView(R2.id.tv_healthTip)
    TextView tvHealthTip;
    @BindView(R2.id.time)
    TextView tvTime;
    @BindView(R2.id.dist)
    TextView tvDist;
    @BindView(R2.id.cal)
    TextView tvCal;
    @BindView(R2.id.et_data)
    ImageView etData;
    @BindView(R2.id.p_dist)
    com.shawnlin.numberpicker.NumberPicker pickerDist;
    @BindView(R2.id.p_time)
    com.shawnlin.numberpicker.NumberPicker pickerTime;
    @BindView(R2.id.p_cal)
    com.shawnlin.numberpicker.NumberPicker pickerCal;
    @BindView(R2.id.btSave)
    Button btSave;
    private Dialog editDialog = null;
    String[] data1 = {"100","200","400", "500", "800", "1000", "1200", "1500", "1800", "2000", "2500","3000","3500","4000","4500","5000",
            "5500","6000","6500","7000","7500","8000"};
    String[] data2 = {"1","3","5", "10", "15", "20", "30", "40", "45","50","60","70",
            "75","80","90","100","110","120","130","140","150","160","170","180"};
    String[] data3 = {"10","20","50", "100", "200", "250", "300","350", "400","450","500","550",
            "600","650","700","750","800","900","1000"};

    @Override
    public int getLayoutId() {
        return R.layout.activity_plan;
    }

    @Override
    public void initData(Bundle savedInstanceState) throws GeneralSecurityException, IOException {
        readData();
        tvTitle.setText("跑步计划");
        etData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    showEditDialog();
                } catch (GeneralSecurityException | IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void initListener() {

    }


    //读取体征数据
    public void readData() throws GeneralSecurityException, IOException {
        String tall = SecuritySP.DecryptSP(this,"tall");
        String heavy = SecuritySP.DecryptSP(this,"heavy");
        String age = SecuritySP.DecryptSP(this,"age");
        //判空(设置默认值防止闪退)
        if (tall.equals("")) tall = "178";
        if (heavy.equals("")) heavy = "60";
        if (age.equals("")) age = "22";
        float bmi = Float.parseFloat(heavy)/(Float.parseFloat(tall)*Float.parseFloat(tall)/10000);
        //体质指数（BMI）=体重（kg）÷身高^2（m）
        //中国成人居民BMI衡量标准是约等于18.4，小于等于18.4为消瘦，18.5-23.9为正常，24-27.9为超重，大于等于28为肥胖
        tvTall.setText(tall);
        tvHeavy.setText(heavy);
        tvAge.setText(age);
        tvBMI.setText(String.valueOf(new BigDecimal(bmi).setScale(1, BigDecimal.ROUND_HALF_UP).floatValue()));
        tvBodyStyle.setText(getBodyStyle(bmi));

        //跑步建议
        String tipStr = "\t\t\t\t"+getTipByAge(Integer.parseInt(age))+""+getTipByBMI(bmi);
        tvHealthTip.setText(tipStr);
        tvDist.setText(getDistByAge(Integer.parseInt(age)));
        tvCal.setText(getCalByAge(Integer.parseInt(age),Integer.parseInt(heavy)));
        String str = getTimeByBMI(bmi);
        tvTime.setText(str);
        if (str.equals("不建议跑步")){
            tvDist.setText("");
            tvCal.setText("");
        }

        //计划
        String planDist,planTime,planCal;
        setPickerAttr();
        planDist = SecuritySP.DecryptSP(context,"dist");
        planTime = SecuritySP.DecryptSP(context,"time");
        planCal = SecuritySP.DecryptSP(context,"cal");
        if (planDist.equals("")) {
            pickerDist.setValue(1) ;
        } else {
            pickerDist.setValue(findIndex(data1,planDist)+1);
        }
        if (planTime.equals("")) {
            pickerTime.setValue(1) ;
        } else {
            pickerTime.setValue(findIndex(data2,planTime)+1);
        }
        if (planCal.equals("")) {
            pickerCal.setValue(1) ;
        } else {
            pickerCal.setValue(findIndex(data3,planCal)+1);
        }


    }
    public void setPickerAttr(){

        pickerDist.setMinValue(1);
        pickerDist.setMaxValue(data1.length);
        pickerDist.setDisplayedValues(data1);

        pickerTime.setMinValue(1);
        pickerTime.setMaxValue(data2.length);
        pickerTime.setDisplayedValues(data2);

        pickerCal.setMinValue(1);
        pickerCal.setMaxValue(data3.length);
        pickerCal.setDisplayedValues(data3);
        //change plan
        btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                ToastUtils.showShort(data1[pickerDist.getValue()-1]+"."+data2[pickerTime.getValue()-1]+"."
//                        +data3[pickerCal.getValue()-1]);
                showLoadingView();
                try {
                    SecuritySP.EncryptSP(context,"dist",data1[pickerDist.getValue()-1]);
                    SecuritySP.EncryptSP(context,"time",data2[pickerTime.getValue()-1]);
                    SecuritySP.EncryptSP(context,"cal",data3[pickerCal.getValue()-1]);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            dismissLoadingView();
                            ToastUtils.showShort("已成功保存新的计划！");
                        }
                    },1500);
                } catch (GeneralSecurityException | IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    public int findIndex(String[] arr, String str) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].equals(str)) {
                return i;
            }
        }
        return -1;
    }
    public static String getBodyStyle(float bmi) {
        if (bmi <= 18.4) {
            return "体型消瘦";
        } else if (bmi > 18.4 && bmi <= 23.9) {
            return "体型正常";
        } else if (bmi > 23.9 && bmi <= 27.9) {
            return "体型超重";
        } else {
            return "体型肥胖";
        }
    }
    public static String getTipByAge(int age){
        if (age <= 14) {
            return "青少年儿童建议每日进行30~60分钟的散步或慢跑。由于该年龄段平衡能力较差，对肌肉、关节的控制能力较差，因此不建议快跑，否则容易跌倒，遭受外伤。";
        } else if (age <= 44) {
            return "中青年人群可进行每日45~60分钟左右的跑步运动，跑步速度控制在6~8公里每小时即可，最好不要超过10公里每小时。跑步之后要注意拉伸腿部，避免乳酸堆积，引起肌肉疼痛。";
        } else {
            return "中老年人在跑步时应该量力而行，体质较差的老年人主要以散步为主，间或穿插短时间的快走、慢跑运动；体质较好，有运动经验的老年人，可以进行30~40分钟的慢跑。跑步时要注意保暖、防风，避免运动出汗后受凉、感冒。";
        }
    }
    public static String getTipByBMI(float bmi) {
        if (bmi <= 18.4) {
            return "鉴于您的体质，您应该进行间断式的跑步，跑一分钟，走两分钟，以此循环，总时长30-60分钟。运动过程中应该适当补充营养，配合力量训练增强体质，并逐步增加跑步的时间，减少走路的时间。";
        } else if (bmi > 18.4 && bmi <= 23.9) {
            return "鉴于您的体质，您可以进行40-90分钟的跑步运动，这样可以起到运动养生，强身健体的作用。";
        } else if (bmi > 23.9 && bmi <= 27.9) {
            return "鉴于您的体质，您可以进行45-60分钟的跑步运动，最好配合一定强度的力量训练，这样可以起到瘦身、减脂、塑形、增强肌肉的作用。";
        } else {
            return "鉴于您的体质，不建议您进行跑步运动，否则可能会对膝关节、踝关节，以及足部各软组织结构产生严重损伤。";
        }
    }
    public static String getTimeByBMI(float bmi) {
        if (bmi <= 18.4) {
            return "30~60 min";
        } else if (bmi > 18.4 && bmi <= 23.9) {
            return "40~90 min";
        } else if (bmi > 23.9 && bmi <= 27.9) {
            return "45~95 min";
        } else {
            return "不建议跑步";
        }
    }
    public static String getDistByAge(int age){
        if (age <= 14) {
            return "3~5 km";
        } else if (age <= 44) {
            return "5~7 km";
        } else {
            return "3~5 km";
        }
    }
    public static String getCalByAge(int age,int heavy){
        //跑步消耗的能量按跑步距离算，对于平地跑步热量计算的公式为：消耗热量（大卡）=体重（公斤）乘以距离（公里）
        int cal1,cal2;
        if (age <= 14) {
            cal1 = 3*heavy;
            cal2 = 5*heavy;
        } else if (age <= 44) {
            cal1 = 5*heavy;
            cal2 = 7*heavy;
        } else {
            cal1 = 3*heavy;
            cal2 = 5*heavy;
        }
        return cal1+"~"+cal2+" kcal";
    }
    private void showEditDialog() throws GeneralSecurityException, IOException {
        editDialog = new Dialog(context,R.style.matchDialog);
        View view = LayoutInflater.from(context).inflate(R.layout.edit_dialog_layout,null);
        EditText etTall,etHeavy,etAge;
        etTall = (EditText) (view.findViewById(R.id.et_tall1));
        etHeavy = (EditText) (view.findViewById(R.id.et_heavy1));
        etAge = (EditText) (view.findViewById(R.id.et_age1));
        String oldTall,oldHeavy,oldAge;
        //获取原数据
        oldTall = SecuritySP.DecryptSP(context,"tall");
        oldHeavy = SecuritySP.DecryptSP(context,"heavy");
        oldAge = SecuritySP.DecryptSP(context,"age");
        //判空(设置默认值防止闪退)
        if (oldTall.equals("")) oldTall = "178";
        if (oldHeavy.equals("")) oldHeavy = "60";
        if (oldAge.equals("")) oldAge = "22";
        //编辑前，显示原数据
        etTall.setText(oldTall);
        etHeavy.setText(oldHeavy);
        etAge.setText(oldAge);
        //取消
        view.findViewById(R.id.cancelTV).setOnClickListener(
                v -> editDialog.dismiss());
        //确定，刷新数据
        String finalOldTall = oldTall;
        String finalOldHeavy = oldHeavy;
        String finalOldAge = oldAge;
        view.findViewById(R.id.confirmTV).setOnClickListener(v -> {
            String newTall,newHeavy,newAge;
            newTall = etTall.getText().toString();
            newHeavy = etHeavy.getText().toString();
            newAge = etAge.getText().toString();
            //保存
            try {
                //判空
                if (newTall.equals("")) newTall = finalOldTall;
                if (newHeavy.equals("")) newTall = finalOldHeavy;
                if (newAge.equals("")) newTall = finalOldAge;
                SecuritySP.EncryptSP(context,"tall",newTall);
                SecuritySP.EncryptSP(context,"heavy",newHeavy);
                SecuritySP.EncryptSP(context,"age",newAge);
                //刷新显示
                showLoadingView(false);
                Handler mHandler = new Handler(Looper.getMainLooper());
                mHandler.postDelayed(() -> {
                    dismissLoadingView();
                    try {
                        readData();
                    } catch (GeneralSecurityException | IOException e) {
                        e.printStackTrace();
                    }
                }, 1500);
                //收起软键盘
                closeKeybord(etTall,context);
                closeKeybord(etHeavy,context);
                closeKeybord(etAge,context);
            } catch (GeneralSecurityException | IOException e) {
                e.printStackTrace();
            }
            editDialog.dismiss();

        });
        editDialog.setContentView(view);
        editDialog.show();
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            //当isShouldHideInput(v, ev)为true时，表示的是点击输入框区域，则需要显示键盘，
            return super.dispatchTouchEvent(ev);
        }
        // 必不可少，否则所有的组件都不会有TouchEvent了
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }
    /**
     * 判断点击的区域是否EditText之外
     *
     * @param v
     * @param event
     *
     * @return 返回true说明点击的是输入框区域外
     */
//    public boolean isShouldHideInput(View v, MotionEvent event) {
//        if (v != null && (v instanceof EditText)) {
//            int[] leftTop = {0, 0};
//            //获取输入框当前的location位置
//            v.getLocationInWindow(leftTop);
//            int left = leftTop[0];
//            int top = leftTop[1];
//            int bottom = top + v.getHeight();
//            int right = left + v.getWidth();
//            if (event.getX() > left && event.getX() < right
//                    && event.getY() > top && event.getY() < bottom) {
//                // 点击的是输入框区域，保留点击EditText的事件
//                return false;
//            } else {
//                return true;
//            }
//        }
//        return false;
//    }
    /**
     * 关闭软键盘的方法（写在 KeyBoardUtils 的工具类里面）
     *
     * @param mEditText 输入框
     * @param mContext 上下文
     */
    public static void closeKeybord(EditText mEditText, Context mContext) {
        if (mEditText != null && mContext != null) {
            InputMethodManager imm = (InputMethodManager) mContext
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
        }
    }





}