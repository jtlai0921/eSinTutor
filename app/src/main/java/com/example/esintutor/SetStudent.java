package com.example.esintutor;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class SetStudent extends AppCompatActivity {

    private String mClassId;
    private String mStudentId;
    RadioButton rb;
    Button btn;
    EditText et;
    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setstudent);

        //設定隱藏標題, 隱藏後自己做一行
        getSupportActionBar().hide();
        //設定隱藏狀態
        //getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

        TextView tv = findViewById(R.id.txt_Class);
        tv.setText(gVariable.getgClassName());
        mClassId = gVariable.getgClassId();

        clearField();

        // 剛進入程式時 可修改 學號, 不可修改 Radio Button
        OnOffFields(true); // false.Off, true.On
    }

    public void clickSearch(View view) {
        String POSTresult;
        et = findViewById(R.id.edt_Stu);
        tv = findViewById(R.id.txt_Stu2);

        if (et.getText().toString().equals("")) return ;

        mStudentId = et.getText().toString();
        String[][] parm = {
                {"WebApiURL", gVariable.gWebURL+"setStudent.php"},
                {"ClassId",   mClassId},
                {"StudentId", mStudentId},
                {"Level", ""},
                {"Status",""},
                {"Reset",""},
                {"ShowOrUpdate","S"}
        };
        POSTresult = DBConnector.sendPOST(parm);
        try {
            JSONObject jsonData = new JSONObject(POSTresult);

            if (jsonData.getString("recs").equals("0")){
                msgBox.getDialogOK(view.getContext(), "錯誤訊息", "無此學號學員!","重新輸入");
                return ;
            }

            tv.setText(jsonData.getString("studentname"));

            rb = findViewById(R.id.radio_Active);
            rb.setChecked(jsonData.getString("studentstatus").equals("0"));
            rb = findViewById(R.id.radio_Exiting);
            rb.setChecked(jsonData.getString("studentstatus").equals("1"));

            rb = findViewById(R.id.radio_Stu);
            rb.setChecked(jsonData.getString("studentlevel").equals("0"));
            rb = findViewById(R.id.radio_Leader);
            rb.setChecked(jsonData.getString("studentlevel").equals("1"));

            // 進入儲存選項時 不可修改 學號, 可修改 Radio Button
            OnOffFields(false); // false.Off, true.On

            // 開放註冊中(全員或個人), 不可再開放學員個別註
            if (!jsonData.getString("regpassword").equals("00000")) {
                rb = findViewById(R.id.radio_Reset_yes);
                rb.setEnabled(false);
                rb = findViewById(R.id.radio_Reset_no);
                rb.setEnabled(false);
            }
            ShowBtn(2);

        } catch (JSONException e) {
            e.printStackTrace();
            return ;
        }

    }

    private void OnOffFields(Boolean OnOff) {
        et.setEnabled(OnOff);
        rb = findViewById(R.id.radio_Leader);       rb.setEnabled(!OnOff);
        rb = findViewById(R.id.radio_Stu);          rb.setEnabled(!OnOff);
        rb = findViewById(R.id.radio_Active);       rb.setEnabled(!OnOff);
        rb = findViewById(R.id.radio_Exiting);      rb.setEnabled(!OnOff);
        rb = findViewById(R.id.radio_Reset_yes);    rb.setEnabled(!OnOff);
        rb = findViewById(R.id.radio_Reset_no);     rb.setEnabled(!OnOff);
    }

    private void ShowBtn(int btnSele) {
        if (btnSele==1) {
            btn = findViewById(R.id.btn_Cancel);
            btn.setVisibility(View.INVISIBLE);
            btn = findViewById(R.id.btn_Save);
            btn.setVisibility(View.INVISIBLE);

            btn = findViewById(R.id.btn_Search);
            btn.setVisibility(View.VISIBLE);

        } else {
            btn = findViewById(R.id.btn_Cancel);
            btn.setVisibility(View.VISIBLE);
            btn = findViewById(R.id.btn_Save);
            btn.setVisibility(View.VISIBLE);

            btn = findViewById(R.id.btn_Search);
            btn.setVisibility(View.INVISIBLE);

        }
    }
    public void btn_SaveCancel(View view) {
        String POSTresult;
        if (view.getId()==R.id.btn_Save) {
            rb = findViewById(R.id.radio_Leader);
            String isLeader = (rb.isChecked() ? "1" : "0");
            rb = findViewById(R.id.radio_Exiting);
            String isExiting = (rb.isChecked() ? "1" : "0");
            rb = findViewById(R.id.radio_Reset_yes);
            String isReset = (rb.isChecked() ? "1" : "0");

            String[][] parm = {
                    {"WebApiURL", gVariable.gWebURL+"setStudent.php"},
                    {"ClassId",   mClassId},
                    {"StudentId", mStudentId},
                    {"Level",     isLeader},
                    {"Status",    isExiting},
                    {"Reset",     isReset},
                    {"ShowOrUpdate","U"}
            };

            POSTresult = DBConnector.sendPOST(parm);
            try {
                JSONObject jsonData = new JSONObject(POSTresult);

                if (jsonData.getString("ReturnCode").equals("-1")){
                    msgBox.getDialogOK(view.getContext(), "錯誤訊息", "資料更新失敗!","重來");
                } else {
                    msgBox.getDialogOK(view.getContext(), "執行訊息", "資料更新成功!","確認");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        clearField();

        // 回到查詢功能時 可修改 學號, 不可修改 Radio Button
        OnOffFields(true); // false.Off, true.On
    }

    private void clearField() {
        et = findViewById(R.id.edt_Stu);
        tv = findViewById(R.id.txt_Stu2);

        ShowBtn(1);
        et.setText("");
        tv.setText("");

        rb = findViewById(R.id.radio_Active);
        rb.setChecked(true);
        rb = findViewById(R.id.radio_Exiting);
        rb.setChecked(false);

        rb = findViewById(R.id.radio_Stu);
        rb.setChecked(true);
        rb = findViewById(R.id.radio_Leader);
        rb.setChecked(false);

        rb = findViewById(R.id.radio_Reset_no);
        rb.setChecked(true);
        rb = findViewById(R.id.radio_Reset_yes);
        rb.setChecked(false);

    }
}


