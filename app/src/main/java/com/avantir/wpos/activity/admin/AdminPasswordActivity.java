package com.avantir.wpos.activity.admin;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.avantir.wpos.R;
import com.avantir.wpos.activity.BaseActivity;
import com.avantir.wpos.activity.MainMenuActivity;
import com.avantir.wpos.utils.GlobalData;

/**
 * Created by lekanomotayo on 24/01/2018.
 */

public class AdminPasswordActivity extends BaseActivity implements  View.OnFocusChangeListener{

    private String TAG = "AdminPasswordActivity";

    GlobalData globalData;
    private Bundle bundle;

    EditText editText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_admin_password);

        findViewById(R.id.titleBackImage).setVisibility(View.GONE);
        findViewById(R.id.titleSettingsImage).setVisibility(View.GONE);

        super.onCreate(savedInstanceState);
    }

    //@Override
    protected void initView() {
        //Top title bar
        ((ImageView) findViewById(R.id.titleBackImage)).setOnClickListener(this);

        editText = ((EditText) findViewById(R.id.adminPasswordText));
        editText.setOnFocusChangeListener(this);
        editText.requestFocus();

        new Handler().postDelayed(new Runnable() {
            public void run() {
                editText.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN , 0, 0, 0));
                editText.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP , 0, 0, 0));
            }
        }, 200);

        ((Button) findViewById(R.id.cancel_admin_password_btn)).setOnClickListener(this);
        ((Button) findViewById(R.id.ok_admin_password_btn)).setOnClickListener(this);
        ((LinearLayout) findViewById(R.id.admin_password_page)).setOnClickListener(this);
    }

    @Override
    protected void initData() {
        globalData = GlobalData.getInstance();

        bundle = getIntent().getExtras();
        if(bundle == null)
        {
            bundle = new Bundle();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.titleBackImage:
                back();
                break;
            case R.id.admin_password_page:
                InputMethodManager imm =  (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                break;
            case R.id.cancel_admin_password_btn:
                back();
                break;
            case R.id.ok_admin_password_btn:
                verifyPassword();
                break;
            default:
                break;
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus){
        //if(!hasFocus) {
        //    InputMethodManager imm =  (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        //    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        //}
    }

    private void verifyPassword(){
        try{
            String pwd = ((EditText) findViewById(R.id.adminPasswordText)).getText().toString();
            String verifyPwd = globalData.getAdminPassword();
            if(pwd.equalsIgnoreCase(verifyPwd)){
                Intent adminIntent = new Intent(this, AdminActivity.class);
                startActivity(adminIntent);
                finish();
            }
            else{
                displayDialog("Incorrect Password");
                onBack();
            }
        }
        catch(Exception ex){
            showToast("Authentication Error!");
        }
    }

    private void back(){
        //finish();
        //skipActivityAnim(-1);
        startActivity(new Intent(this, MainMenuActivity.class));
        finish();
    }

}
