package com.example.motion.commmon.utils;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正则表达式
 */
public class Validator {



    public static boolean checkColor(@NonNull String color) {

        return color.matches("^#([0-9a-fA-F]{6}|[0-9a-fA-F]{8})$");
    }


}
