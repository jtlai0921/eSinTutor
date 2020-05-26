package com.example.esintutor;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SetClass extends AppCompatActivity {
    /***
     * 以下為 定位用變數
     */
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 6000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    private final static String KEY_REQUESTING_LOCATION_UPDATES = "requesting-location-updates";
    private final static String KEY_LOCATION = "location";
    private final static String KEY_LAST_UPDATED_TIME_STRING = "last-updated-time-string";
    private FusedLocationProviderClient mFusedLocClient;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocRequest;
    private LocationSettingsRequest mLocSettingsRequest;
    private LocationCallback mLocCallback;
    private Location mCurrentLoc;
    private Button mStartUpdBtn;
    private Button mStopUpdBtn;
    private TextView tvLastUpdTime;
    private String mLatitudeLbl;
    private String mLongitudeLbl;
    private String mLastUpdTimeLbl;
    private Boolean mReqLocUpdates;
    private String mLastUpdTime;

    /***
     * 結束 定位用變數　========================
     */

    private String mClassid;
    private String mClassName;
    private String mTutorId;
    private String mTutorName;
    private String mAddUpd;

    // 畫面變數
    EditText etYear, etTerm, etClassName, etMaxStudent, etDistance;
    TextView tvClassId, tvTutorId, tvLongitude, tvLatitude,
            tvStartDate, tvEndDate, tvSin01STime, tvSin01ETime, tvSin02STime, tvSin02ETime;

    TextView imgTextView;
    Switch   swReg;
    TextView tvRegPWD;
    Double   rdnRegPWD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setclass);

        //設定隱藏標題, 隱藏後自己做一行
        getSupportActionBar().hide();
        //設定隱藏狀態
        //getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

        // Locate the UI widgets.
        mStartUpdBtn    = findViewById(R.id.startUpdatesButton);
        mStopUpdBtn     = findViewById(R.id.stopUpdatesButton);
        tvClassId       = findViewById(R.id.txtClassId);
        tvTutorId       = findViewById(R.id.txtTutorId);
        tvLatitude      = findViewById(R.id.latitude_text);
        tvLongitude     = findViewById(R.id.longitude_text);
        tvLastUpdTime   = findViewById(R.id.last_update_time_text);

        // Set labels.
        mLatitudeLbl    = "緯度";
        mLongitudeLbl   = "經度";
        mLastUpdTimeLbl = getResources().getString(R.string.last_update_time_label);

        mReqLocUpdates  = false;
        mLastUpdTime    = "";

        // Update values using data stored in the Bundle.
//        updateValuesFromBundle(savedInstanceState);

        mFusedLocClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);

        // Kick off the process of building the LocationCallback, LocationRequest, and
        // LocationSettingsRequest objects.
        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();

        mClassid    = gVariable.getgClassId();
        mClassName  = gVariable.getgClassName();
        mTutorId    = gVariable.getgTutorId();
        mTutorName  = gVariable.getgTutorName();

        etYear       = findViewById(R.id.edtYear);
        etTerm       = findViewById(R.id.edtTerm);
        etClassName  = findViewById(R.id.edtClassName);
        etMaxStudent = findViewById(R.id.edtMaxStudent);
        tvStartDate  = findViewById(R.id.txtStartDate);
        tvEndDate    = findViewById(R.id.txtEndDate);
        tvSin01STime = findViewById(R.id.txtSin01STime);
        tvSin01ETime = findViewById(R.id.txtSin01ETime);
        tvSin02STime = findViewById(R.id.txtSin02STime);
        tvSin02ETime = findViewById(R.id.txtSin02ETime);
        etDistance   = findViewById(R.id.edtDistance);
        swReg        = findViewById(R.id.swhRegSts);
        tvRegPWD     = findViewById(R.id.txtRegPassWord);

        // 進入本畫面先鎖定不可修改
        onoffFields(false);

        // 顯示 gClassId 全部欄位,
        if (mClassid.equals("")) {
            ClearField();
        } else {
            ShowField();
        }

        // 設定註冊 switch Listener
        swReg.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    rdnRegPWD = Math.random() * (99999-10001 + 1) + 10001;
                    tvRegPWD.setText(String.valueOf(rdnRegPWD).substring(0,5));
                } else {
                    tvRegPWD.setText("00000");
                }
            }
        });
    }

    public void classBtnControl(View view) {
        Button btn = (Button)view;
        if (btn.getId()==R.id.btnAddClass){
            if (btn.getText().toString().equals("開新班級")) {
                mAddUpd = "Add";
                ClearField();
                setButtonText(view,"YN");
                onoffFields(true);
            } else {
                setButtonText(view,"Org");
                UpdateFields();
                onoffFields(false);
            }
        } else {
            if (btn.getText().toString().equals("修改設定")) {
                mAddUpd = "Upd";
                setButtonText(view,"YN");
                onoffFields(true);
            } else {
                setButtonText(view,"Org");
                ShowField();
                onoffFields(false);
            }
        }
    }

    private void ClearField() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date curDate = new Date(System.currentTimeMillis()); // 獲取當前時間

        tvClassId.setText("存檔時系統自動給號");
        tvTutorId.setText(mTutorId+"."+mTutorName);
        etYear.setText("");
        etTerm.setText("");
        etClassName.setText("");
        etMaxStudent.setText("");
        swReg.setEnabled(false);
        tvStartDate.setText(sdf.format(curDate));
        tvEndDate.setText(sdf.format(curDate));
        tvSin01STime.setText("07:30");
        tvSin01ETime.setText("08:30");
        tvSin02STime.setText("12:45");
        tvSin02ETime.setText("13:45");
        etDistance.setText("15");
        tvLongitude.setText("經度:");
        tvLatitude.setText("緯度:");
    }

    private void onoffFields(Boolean OnOff) {
        etYear.setEnabled(OnOff);
        etTerm.setEnabled(OnOff);
        etClassName.setEnabled(OnOff);
        etMaxStudent.setEnabled(OnOff);
        swReg.setEnabled(OnOff);
        etDistance.setEnabled(OnOff);
    }

    private void setButtonText(View view,String mTYpe){
        Button bt = (Button) view;
        if (mTYpe.equals("YN")) {
            bt = findViewById(R.id.btnAddClass);    bt.setText("確定存檔");
            bt = findViewById(R.id.btnEditClass);   bt.setText("取消");
        } else {
            bt = findViewById(R.id.btnAddClass);    bt.setText("開新班級");
            bt = findViewById(R.id.btnEditClass);   bt.setText("修改設定");
        }
    }

    private void ShowField() {
        String POSTresult;
        EditText et;
        TextView tv;

        String[][] parm = {
                {"WebApiURL", gVariable.gWebURL+"getClassFields.php"},
                {"ClassId",   mClassid}
        };
        POSTresult = DBConnector.sendPOST(parm);
        try {
            JSONObject jsonData = new JSONObject(POSTresult);
            tvClassId.setText(mClassid);
            tvTutorId.setText(mTutorId+"."+mTutorName);
            etYear.setText(jsonData.getString("year"));
            etTerm.setText(jsonData.getString("term"));
            etClassName.setText(jsonData.getString("classname"));
            etMaxStudent.setText(jsonData.getString("maxstudent"));
            tvRegPWD.setText(jsonData.getString("regpassword"));
            if (jsonData.getString("regpassword").compareTo("00000")>0) {
                swReg.setChecked(true);
            } else {
                swReg.setChecked(false);
            }
            tvStartDate.setText(jsonData.getString("startdate"));
            tvEndDate.setText(jsonData.getString("enddate"));
            tvSin01STime.setText(jsonData.getString("sin01stime").substring(0,5));
            tvSin01ETime.setText(jsonData.getString("sin01etime").substring(0,5));
            tvSin02STime.setText(jsonData.getString("sin02stime").substring(0,5));
            tvSin02ETime.setText(jsonData.getString("sin02etime").substring(0,5));
            etDistance.setText(jsonData.getString("distance"));
            tvLongitude.setText("經度:"+jsonData.getString("longitude"));
            tvLatitude.setText("緯度:"+jsonData.getString("latitude"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void UpdateFields() {
        String POSTresult;
        // 缺下列欄位 無法產生班級代號, 直接結束存檔按鍵功能
        if (etYear.getText().toString().equals("")||
            etTerm.getText().toString().equals("")||
            etClassName.getText().toString().equals("")) {
            msgBox.getDialogOK(this,"錯誤訊息","年度,梯次或班名未輸入","請輸入");
            return;
        }
        etYear = findViewById(R.id.edtYear);
        String[][] parm = {
                {"WebApiURL", gVariable.gWebURL+"updClassFields.php"},
                {"ClassId",   mClassid},
                {"TutorId",   mTutorId},
                {"Year",      etYear.getText().toString()},
                {"Term",      etTerm.getText().toString()},
                {"ClassName", etClassName.getText().toString()},
                {"MaxStudent",etMaxStudent.getText().toString()},
                {"StartDate", tvStartDate.getText().toString()},
                {"EndDate",   tvEndDate.getText().toString()},
                {"Sin01STime",tvSin01STime.getText().toString()+":00"},
                {"Sin01ETime",tvSin01ETime.getText().toString()+":00"},
                {"Sin02STime",tvSin02STime.getText().toString()+":00"},
                {"Sin02ETime",tvSin02ETime.getText().toString()+":00"},
                {"Distance",  etDistance.getText().toString()},
                {"Longitude", tvLongitude.getText().toString().substring(3)},
                {"Latitude",  tvLatitude.getText().toString().substring(3)},
                {"AddOrUpd",  mAddUpd}
        };
        POSTresult = DBConnector.sendPOST(parm);
        try {
            JSONObject jsonData = new JSONObject(POSTresult);
            if (mAddUpd.equals("Add")) {
                // 開新班級存檔成功時,傳回訊息後5碼為班級代號
                String msg = jsonData.getString("ReturnCode");
                tvClassId.setText(msg.substring(msg.length()-5));
                msgBox.getDialogOK(this, "處理訊息", msg.substring(3,msg.length()-5), "確認");

            } else {
                msgBox.getDialogOK(this, "處理訊息", jsonData.getString("ReturnCode").substring(3), "確認");
            }
        } catch (JSONException e) {
            msgBox.getDialogOK(this, "錯誤訊息", "資料存檔錯誤!","重來");
            e.printStackTrace();
        }
    }

/*** ===================================================
*
* 以下為 Android Studio 定位範例修改而來
*
**** =================================================== */
//private void updateValuesFromBundle(Bundle savedInstanceState) {
//    if (savedInstanceState != null) {
//        // Update the value of mReqLocUpdates from the Bundle, and make sure that
//        // the Start Updates and Stop Updates buttons are correctly enabled or disabled.
//        if (savedInstanceState.keySet().contains(KEY_REQUESTING_LOCATION_UPDATES)) {
//            mReqLocUpdates = savedInstanceState.getBoolean(
//                    KEY_REQUESTING_LOCATION_UPDATES);
//        }
//
//        // Update the value of mCurrentLoc from the Bundle and update the UI to show the
//        // correct latitude and longitude.
//        if (savedInstanceState.keySet().contains(KEY_LOCATION)) {
//            // Since KEY_LOCATION was found in the Bundle, we can be sure that mCurrentLoc
//            // is not null.
//            mCurrentLoc = savedInstanceState.getParcelable(KEY_LOCATION);
//        }
//
////        // Update the value of mLastUpdTime from the Bundle and update the UI.
//        if (savedInstanceState.keySet().contains(KEY_LAST_UPDATED_TIME_STRING)) {
//            mLastUpdTime = savedInstanceState.getString(KEY_LAST_UPDATED_TIME_STRING);
//        }
//        updateUI();
//    }
//}

    private void createLocationRequest() {
        mLocRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Creates a callback for receiving location events.
     */
    private void createLocationCallback() {
        mLocCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                mCurrentLoc = locationResult.getLastLocation();
                mLastUpdTime = DateFormat.getTimeInstance().format(new Date());
                updLocUI();
            }
        };
    }

    /**
     * Uses a {@link com.google.android.gms.location.LocationSettingsRequest.Builder} to build
     * a {@link com.google.android.gms.location.LocationSettingsRequest} that is used for checking
     * if a device has the needed location settings.
     */
    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocRequest);
        mLocSettingsRequest = builder.build();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i(TAG, "User agreed to make required location settings changes.");
                        // Nothing to do. startLocUpdates() gets called in onResume again.
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(TAG, "User chose not to make required location settings changes.");
                        mReqLocUpdates = false;
                        updateUI();
                        break;
                }
                break;
        }
    }

    /**
     * Handles the Start Updates button and requests start of location updates. Does nothing if
     * updates have already been requested.
     */
    public void btnStartUpdLoc(View view) {
        if (!mReqLocUpdates) {
            mReqLocUpdates = true;
            setButtonsEnabledState();
            startLocUpdates();
        }
    }

    /**
     * Handles the Stop Updates button, and requests removal of location updates.
     */
    public void btnStopUpdLoc(View view) {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        stopLocUpdates();
    }

    /**
     * Requests location updates from the FusedLocationApi. Note: we don't call this unless location
     * runtime permission has been granted.
     */
    private void startLocUpdates() {
        // Begin by checking if the device has the necessary location settings.
        mSettingsClient.checkLocationSettings(mLocSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i(TAG, "All location settings are satisfied.");

                        //noinspection MissingPermission
                        mFusedLocClient.requestLocationUpdates(mLocRequest,
                                mLocCallback, Looper.myLooper());

                        updateUI();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                        "location settings ");
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(SetClass.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);
                                Toast.makeText(SetClass.this, errorMessage, Toast.LENGTH_LONG).show();
                                mReqLocUpdates = false;
                        }

                        updateUI();
                    }
                });
    }

    /**
     * Updates all UI fields.
     */
    private void updateUI() {
        setButtonsEnabledState();
        updLocUI();
    }

    /**
     * Disables both buttons when functionality is disabled due to insuffucient location settings.
     * Otherwise ensures that only one button is enabled at any time. The Start Updates button is
     * enabled if the user is not requesting location updates. The Stop Updates button is enabled
     * if the user is requesting location updates.
     */
    private void setButtonsEnabledState() {
        if (mReqLocUpdates) {
            mStartUpdBtn.setEnabled(false);
            mStartUpdBtn.setTextColor(Color.parseColor("#bbbbbb"));
            mStopUpdBtn.setEnabled(true);
            mStopUpdBtn.setTextColor(Color.parseColor("#222222"));
        } else {
            mStartUpdBtn.setEnabled(true);
            mStartUpdBtn.setTextColor(Color.parseColor("#222222"));
            mStopUpdBtn.setEnabled(false);
            mStopUpdBtn.setTextColor(Color.parseColor("#bbbbbb"));
        }
    }

    /**
     * Sets the value of the UI fields for the location latitude, longitude and last update time.
     */
    private void updLocUI() {
        if (mCurrentLoc != null) {
            tvLatitude.setText(String.format(Locale.ENGLISH, "%s: %f", mLatitudeLbl,
                    mCurrentLoc.getLatitude()));
            tvLongitude.setText(String.format(Locale.ENGLISH, "%s: %f", mLongitudeLbl,
                    mCurrentLoc.getLongitude()));
            tvLastUpdTime.setText(String.format(Locale.ENGLISH, "%s: %s",
                    mLastUpdTimeLbl, mLastUpdTime));
        }
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    private void stopLocUpdates() {
        if (!mReqLocUpdates) {
            Log.d(TAG, "stopLocUpdates: updates never requested, no-op.");
            return;
        }

        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        mFusedLocClient.removeLocationUpdates(mLocCallback)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        mReqLocUpdates = false;
                        setButtonsEnabledState();
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Within {@code onPause()}, we remove location updates. Here, we resume receiving
        // location updates if the user has requested them.
        if (mReqLocUpdates && checkPermissions()) {
            startLocUpdates();
        } else if (!checkPermissions()) {
            requestPermissions();
        }

        updateUI();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Remove location updates to save battery.
        stopLocUpdates();
    }

    /**
     * Stores activity data in the Bundle.
     */
//    public void onSaveInstanceState(Bundle savedInstanceState) {
//        savedInstanceState.putBoolean(KEY_REQUESTING_LOCATION_UPDATES, mReqLocUpdates);
//        savedInstanceState.putParcelable(KEY_LOCATION, mCurrentLoc);
//        savedInstanceState.putString(KEY_LAST_UPDATED_TIME_STRING, mLastUpdTime);
//        super.onSaveInstanceState(savedInstanceState);
//    }

    /**
     * Shows a {@link Snackbar}.
     *
     * @param mainTextStringId The id for the string resource for the Snackbar text.
     * @param actionStringId   The text of the action item.
     * @param listener         The listener associated with the Snackbar action.
     */
    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(
                findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            showSnackbar(R.string.permission_rationale,
                    android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(SetClass.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    });
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(SetClass.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mReqLocUpdates) {
                    Log.i(TAG, "Permission granted, updates requested, starting location updates");
                    startLocUpdates();
                }
            } else {
                // Permission denied.

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                showSnackbar(R.string.permission_denied_explanation,
                        R.string.settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
            }
        }
    }

    public void clickDatePicker(View view) {
        Button btn = findViewById((R.id.btnEditClass));
        if (btn.getText().toString().equals("修改設定")) { return; }

        if (view.getId()==R.id.imgStartDate) {
            imgTextView = findViewById(R.id.txtStartDate);
        } else {
            imgTextView = findViewById(R.id.txtEndDate);
        }

        String ss;
        ss = imgTextView.getText().toString();
        int year = Integer.parseInt(ss.substring(0,4));
        int month = Integer.parseInt(ss.substring(5,7))-1;
        int day = Integer.parseInt(ss.substring(8,10));

        new DatePickerDialog(view.getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                imgTextView.setText(Integer.toString(year) + "-" +
                        ((month<9) ? "0" : "")+Integer.toString(month + 1) + "-" +
                        ((day<10) ? "0" : "")+Integer.toString(day));
            }
        }, year, month, day).show();
    }

    public void clickTimePicker(View view) {
        Button btn = findViewById((R.id.btnEditClass));
        if (btn.getText().toString().equals("修改設定")) { return; }

        switch (view.getId()) {
            case R.id.imgSin01STime:
                imgTextView = findViewById(R.id.txtSin01STime);
                break;
            case R.id.imgSin01ETime:
                imgTextView = findViewById(R.id.txtSin01ETime);
                break;
            case R.id.imgSin02STime:
                imgTextView = findViewById(R.id.txtSin02STime);
                break;
            case R.id.imgSin02ETime:
                imgTextView = findViewById(R.id.txtSin02ETime);
                break;
        }

        String ss;
        ss = imgTextView.getText().toString();
        int hour = Integer.parseInt(ss.substring(0,2));
        int minute = Integer.parseInt(ss.substring(3,5));

        new TimePickerDialog(view.getContext(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hour, int minute) {
                imgTextView.setText(((hour<10) ? "0" : "")+Integer.toString(hour) + ":" +
                        ((minute<10) ? "0" : "")+Integer.toString(minute));
            }
        }, hour, minute,true).show();

    }
}
