package com.example.esintutor;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class serviceError extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serviceerror);

        //設定隱藏標題, 隱藏後自己做一行
        getSupportActionBar().hide();
        //設定隱藏狀態
        //getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
    }
}
