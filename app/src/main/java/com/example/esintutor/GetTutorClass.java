package com.example.esintutor;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GetTutorClass extends AppCompatActivity {

    public String[] GetTutorClass(String TutorId) {
        String POSTresult;
        String[] aClassList;
        String[][] parm = {
                {"WebApiURL", gVariable.gWebURL+"getClassList.php"},
                {"TutorId",   TutorId}
        };

        POSTresult = DBConnector.sendPOST(parm);
        try {
            JSONArray jsonArray = new JSONArray(POSTresult);
            int RowCount = jsonArray.length();
            aClassList = new String[RowCount] ;
            for(int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonData = jsonArray.getJSONObject(i);
                aClassList[i] = jsonData.getString("classname");
            }
        } catch (JSONException e) {
            aClassList =  new String[0] ;
            e.printStackTrace();
        }
        return aClassList;
    }


}
