package com.example.esintutor;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;

// 以下為取得定位權限所import

public class PhoneRegister extends AppCompatActivity {
    Button btn;
    int ACCESS_FINE_LOCATION = 1;

    private long mExitTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.phoneregister);

        //設定隱藏標題, 隱藏後自己做一行
        getSupportActionBar().hide();
        //設定隱藏狀態
        //getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

        // 要求授權, 未取得前註冊鍵先關掉
        btn = findViewById(R.id.btnRegMenu);
        btn.setEnabled(false);
        getPermission();
    }

    public Boolean btnReg(View view) {
        //判斷導師帳號 導師姓名 密碼 都有輸入值 才執行
        final EditText mTutorId = findViewById(R.id.edtTutorId);
        final EditText mTutorName = findViewById(R.id.edtTutorName);
        final EditText mPassWord = findViewById(R.id.edtPassWord);
        String POSTresult;

        if (mTutorId.getText().toString().equals("") ||
            mPassWord.getText().toString().equals("") ) {
            msgBox.getDialogOK(view.getContext(), "錯誤訊息", "帳號密碼不可空白!","重新輸入");
            return false;
        } else {
            // check table
            String[][] parm = {
                    {"WebApiURL", gVariable.gWebURL+"getTutorData.php"},
                    {"TutorId",   mTutorId.getText().toString()},
                    {"Password",  mPassWord.getText().toString()}
            };

            POSTresult = DBConnector.sendPOST(parm);
            try {
                JSONObject jsonData = new JSONObject(POSTresult);

                if (jsonData.getString("recs").equals("0")){
                    msgBox.getDialogOK(view.getContext(), "錯誤訊息", "帳號密碼錯誤!","重新輸入");
                    return false;
                }
                mTutorName.setText(jsonData.getString("tutorname"));
            } catch (JSONException e) {
                msgBox.getDialogOK(view.getContext(), "錯誤訊息", "資料庫存取錯誤!","重來");
                e.printStackTrace();
                return false;
            }

            // 設定全域變數
            gVariable.setgTutorId(mTutorId.getText().toString());
            gVariable.setgTutorName(mTutorName.getText().toString());

            // 資料寫回手機
            SharedPreferences pref = getSharedPreferences("Tutor", MODE_PRIVATE);
            pref.edit()
                    .putString("TutorId",   mTutorId.getText().toString())
                    .putString("TutorName", mTutorName.getText().toString())
                    .apply();

            msgBox.getDialogOK(view.getContext(), "註冊訊息", "導師手機註冊完成!","確認");

            // 切回主畫面
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setClass(PhoneRegister.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        }
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //判斷用戶是否點擊了“返回鍵”
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //與上次點擊返回鍵時刻作差
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                //大於2000ms則認為是誤操作，使用Toast進行提示
                Toast.makeText(this, "再按一次退出程式", Toast.LENGTH_SHORT).show();
                //並記錄下本次點擊“返回鍵”的時刻，以便下次進行判斷
                mExitTime = System.currentTimeMillis();
            } else {
                //小於2000ms則認為是使用者確實希望退出程式-調用System.exit()方法進行退出
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }



    // 以下為取得定位權限的程式
private void getPermission() {
    // 要求授權
    if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
            Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

        ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION},
                ACCESS_FINE_LOCATION);
    }
}

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // 授權 callback
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            // 正常取得授權, 開啟註冊按鍵
            btn.setEnabled(true);
        } else {
            // 未取得全部授權
            if (msgBox.getDialogYesNo(this,"未取得全部授權","請提供授權程式才能正常執行","重新授權","拒絕授權")=="Y") {
                // 重新取得檔案寫入的權限
                getPermission();
            } else {
                // 拒絕授權
                msgBox.getDialogOK(this,"錯誤訊息","無法取得權限","結束離開");
                // 強迫結束APP
                System.exit(0);
            }
        }
    }
}