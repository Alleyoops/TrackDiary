package com.james.motion.ui.weight;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class SecuritySP {
    //SharedPreferences加密存储
    public static void EncryptSP(Context context, String key, String value) throws GeneralSecurityException, IOException {
        MasterKey masterKey = new MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();
        SharedPreferences security_sp = EncryptedSharedPreferences.create(context,"security_sp_file",masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
        security_sp.edit().putString(key, value).apply();
    }
    //解密取出
    public static String DecryptSP(Context context,String key) throws GeneralSecurityException, IOException {
        MasterKey masterKey = new MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();
        SharedPreferences security_sp = EncryptedSharedPreferences.create(context,"security_sp_file",masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
        return security_sp.getString(key,"");//获取key，没有则默认为空字符
    }
    //清除某项,退出登录时会用到
    public static void Remove(Context context,String key) throws GeneralSecurityException, IOException {
        MasterKey masterKey = new MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();
        SharedPreferences security_sp = EncryptedSharedPreferences.create(context,"security_sp_file",masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
        security_sp.edit().remove(key).apply();
    }
}
