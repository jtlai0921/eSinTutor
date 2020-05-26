package com.example.esintutor;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private long mExitTime;

    private String mDate;
    private String mClassId;
    private String mClassName;

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //設定隱藏標題, 隱藏後自己做一行
        getSupportActionBar().hide();
        //設定隱藏狀態
        //getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

        // Android嚴苛模式設定
        setStrictMode();

        if (!DBConnector.checkURLStatus(gVariable.gWebURL+"SignList.php").equals("OK")) {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setClass(MainActivity.this, serviceError.class);
            startActivity(intent);
        } else {

            // 檢查手機是否註冊, 未註冊則跳轉至註冊頁面
            String mTutorId = getSharedPreferences("Tutor", MODE_PRIVATE)
                    .getString("TutorId", "");
            String mTutorName = getSharedPreferences("Tutor", MODE_PRIVATE)
                    .getString("TutorName", "");

            if (mTutorId.equals("")) {
                Intent intent = new Intent();
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setClass(MainActivity.this, PhoneRegister.class);
                startActivity(intent);
            }

            // 從手機取得的資料寫回 全域變數
            gVariable.setgTutorId(mTutorId);
            gVariable.setgTutorName(mTutorName);

            // 處理班級 spinner, 順便設定 mClassId, gVariable.java 中的 gClassId 供其它頁面使用
            showClass();

            // 顯示今天日期, 順便設定 mDate
            showSysDate();

            // 顯示今天簽到 mClassId & mDate 在上2行已處理
            showSignlist(mClassId, mDate);
        }
    }

    private void setStrictMode() {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .penaltyLog()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .penaltyLog()
                .penaltyDeath()
                .build());
    }

    // 班級 或 學員 設定 click event
    public void buttonControl(View view) {
        Intent intent = new Intent();
        Button btn = (Button)view;
        if (btn.getId()==R.id.btnSetClass) {
            intent.setClass(MainActivity.this, SetClass.class);
        } else {
            intent.setClass(MainActivity.this, SetStudent.class);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    // 抓系統日放 Header & mDate
    private void showSysDate() {
        TextView mtxtDate = findViewById(R.id.txtDate);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        Date curDate = new Date(System.currentTimeMillis()); // 獲取當前時間
        mDate = sdf.format(curDate);
        mtxtDate.setText(mDate.substring(5));
    }

    // 班級 spinner, 順便放 mClassId
    private void showClass(){

        // 從資料庫抓導師目前有效的班級列表
        GetTutorClass cl = new GetTutorClass();
        String[] classList = cl.GetTutorClass("T0001");

        List<String> all;
        Spinner mSpnClass;
        ArrayAdapter<String> adapter;

        all = new ArrayList<String>(Arrays.asList(classList));

        adapter = new ArrayAdapter<String>(this, R.layout.myspinner, all);
        adapter.setDropDownViewResource(R.layout.myspinner);

        mSpnClass = findViewById(R.id.spnClass);
        mSpnClass.setAdapter(adapter);

        // 抓點選項次的 mClassId & gClassName
        mClassId = mSpnClass.getSelectedItem().toString().substring(0,5);
        gVariable.setgClassId(mClassId) ;
        mClassName = mSpnClass.getSelectedItem().toString().substring(6);
        gVariable.setgClassName(mClassName) ;

        // 設定班級 spinner Listener
        mSpnClass.setOnItemSelectedListener(new spinnerSelectedListenner());  //綁定事件監聽
    }

    // 班級 spinner Listener
    private class spinnerSelectedListenner implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int position, long id) {                 //當列表項被選擇時
            // TODO Auto-generated method stub
            String select = parent.getItemAtPosition(position).toString();  //取得被選中的清單項的文字
            mClassId = select.substring(0,5) ;
            mClassName = select.substring(6) ;

            gVariable.setgClassId(mClassId);
            gVariable.setgClassName(mClassName);

            // 有異動就直接刷新簽到畫面資料
            showSignlist(mClassId,mDate);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // TODO Auto-generated method stub
        }
    }

    // 當日, 前一日, 次一日
    public void changeDate(View view) {
        Button btn = (Button)view;
        int changeValue = 0;
        if (btn.getId()==R.id.btnPrevDate) changeValue -= 1;
        else if (btn.getId()==R.id.btnNextDate) changeValue += 1;

        TextView txtDate = findViewById(R.id.txtDate);
        // mDate = dateConvert.addDay(txtDate.getText().toString(), changeValue);
        mDate = dateConvert.addDay(mDate, changeValue);
        txtDate.setText(mDate.substring(5));

        showSignlist(mClassId,mDate);
    }

    // 顯示 簽到表
    private void showSignlist(String mClassId,String mDate){
        TableLayout tblSign = findViewById(R.id.tblSignList);

        SignList sl = new SignList();
        sl.ShowSignList(tblSign,mClassId,mDate);
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

}
