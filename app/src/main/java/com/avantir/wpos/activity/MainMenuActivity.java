package com.avantir.wpos.activity;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.widget.Toast;
import com.avantir.wpos.R;
import com.avantir.wpos.activity.admin.AdminActivity;
import com.avantir.wpos.activity.admin.AdminPasswordActivity;
import com.avantir.wpos.activity.admin.SupervisorPinActivity;
import com.avantir.wpos.activity.transactions.InsertCardActivity;
import com.avantir.wpos.utils.ConstantUtils;
import com.avantir.wpos.utils.GlobalData;
import wangpos.sdk4.libbasebinder.Core;
import wangpos.sdk4.libbasebinder.Printer;
import wangpos.sdk4.libkeymanagerbinder.Key;

/**
 * Created by lekanomotayo on 28/02/2018.
 */
public class MainMenuActivity extends BaseActivity {

    private Key mKey;
    private Printer mPrinter;
//    private Core mCore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        initView();
        setActionBar();

        new Thread() {
            @Override
            public void run() {
                mKey = new Key(getApplicationContext());
                mPrinter = new Printer(getApplicationContext());
                //mCore = new Core(getApplicationContext());

                AlarmManager am = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
                am.setTimeZone(ConstantUtils.TIMEZONE_LAGOS);

            }
        }.start();


        findViewById(R.id.titleBackImage).setVisibility(View.GONE);
        findViewById(R.id.titleSettingsImage).setVisibility(View.GONE);

        this.findViewById(R.id.purchase_btn).setOnClickListener(this);
        this.findViewById(R.id.balance_btn).setOnClickListener(this);
        this.findViewById(R.id.refund_btn).setOnClickListener(this);
        this.findViewById(R.id.cashback_btn).setOnClickListener(this);
        this.findViewById(R.id.cash_advance_btn).setOnClickListener(this);
        this.findViewById(R.id.reprint_btn).setOnClickListener(this);
        this.findViewById(R.id.end_of_day_btn).setOnClickListener(this);
        this.findViewById(R.id.admin_btn).setOnClickListener(this);

        //this.findViewById(R.id.titleSettingsImage).setOnClickListener(this);
    }


    protected void initView() {
        setTitle("Main");
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.titleSettingsImage:
                Intent settingsIntent = new Intent(this, AdminActivity.class);
                startActivity(settingsIntent);
                break;
            case R.id.purchase_btn:
                if(performTransactionChecks()){
                    Intent intent = new Intent(this, InsertCardActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putInt(ConstantUtils.TRAN_TYPE, ConstantUtils.PURCHASE);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
                break;
            case R.id.balance_btn:
                if(performTransactionChecks()){
                    Intent intent = new Intent(this, InsertCardActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putInt(ConstantUtils.TRAN_TYPE, ConstantUtils.BALANCE);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
                break;
            case R.id.refund_btn:
                if(performTransactionChecks()){
                    Intent intent = new Intent(this, SupervisorPinActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putInt(ConstantUtils.NEXT_ACTIVITY, ConstantUtils.REFUND_ACTIVITY);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
                break;
            case R.id.cashback_btn:
                if(performTransactionChecks()){
                    Intent intent = new Intent(this, InsertCardActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putInt(ConstantUtils.TRAN_TYPE, ConstantUtils.CASH_BACK);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
                break;
            case R.id.cash_advance_btn:
                if(performTransactionChecks()){
                    Intent intent = new Intent(this, InsertCardActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putInt(ConstantUtils.TRAN_TYPE, ConstantUtils.CASH_ADVANCE);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
                break;
            case R.id.reprint_btn:
                if(performPrinterChecks()){
                    Intent intent = new Intent(this, SupervisorPinActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putInt(ConstantUtils.NEXT_ACTIVITY, ConstantUtils.REPRINT_ACTIVITY);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
                break;
            case R.id.end_of_day_btn:
                if(performPrinterChecks()){
                    Intent intent = new Intent(this, SupervisorPinActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putInt(ConstantUtils.NEXT_ACTIVITY, ConstantUtils.EOD_ACTIVITY);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
                break;
            case R.id.admin_btn:
                Intent adminIntent = new Intent(this, AdminPasswordActivity.class);
                startActivity(adminIntent);
                break;
        }
    }

    public void setActionBar() {
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }


    private boolean performTransactionChecks(){
        try{
            GlobalData globalData = GlobalData.getInstance();
            if(globalData.isDemoMode())
                return true;

            boolean success = performKeyChecks(globalData);
            if(!success)
                return success;

            success = performEMVParamChecks(globalData);
            if(!success)
                return success;

            return  performPrinterChecks();
        }
        catch(Exception ex){
            ex.printStackTrace();
            return false;
        }
    }


    private boolean performKeyChecks(GlobalData globalData) {
        int ret = -1;
        try {
            if(globalData.isDemoMode())
                return true;

            // if keys are loaded (CTMK, TMK, TSK, TPK, IPEK Track 2, IPEK EMV)
            ret = mKey.checkKeyExist(ConstantUtils.APP_NAME, Key.KEY_REQUEST_TLK);
            if (ret != 0 || !globalData.getLocalMasterKeyLoadedFlag()) {
                Toast.makeText(this, "Ensure TLK is loaded", Toast.LENGTH_LONG).show();
                return false;
            }
            ret = mKey.checkKeyExist(ConstantUtils.APP_NAME, Key.KEY_REQUEST_TMK);
            if (ret != 0 || !globalData.getTMKKeyFlag()) {
                Toast.makeText(this, "Ensure TMK is loaded", Toast.LENGTH_LONG).show();
                return false;
            }
            ret = mKey.checkKeyExist(ConstantUtils.APP_NAME, Key.KEY_REQUEST_MAK);
            if (ret != 0 || !globalData.getMacKeyFlag()) {
                Toast.makeText(this, "Ensure MAK is loaded", Toast.LENGTH_LONG).show();
                return false;
            }
            ret = mKey.checkKeyExist(ConstantUtils.APP_NAME, Key.KEY_REQUEST_PEK);
            if (ret != 0 || !globalData.getPinKeyFlag()) {
                Toast.makeText(this, "Ensure PEK is loaded", Toast.LENGTH_LONG).show();
                return false;
            }

            if(!(globalData.getKeysLoadedFlag())){
                Toast.makeText(this, "Ensure all Keys are loaded", Toast.LENGTH_LONG).show();
                return false;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error while checking loaded keys", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private boolean performEMVParamChecks(GlobalData globalData){
        int ret = -1;

        if(globalData.isDemoMode())
            return true;

        // if EMV params are laoded (AID, CAPK, EMV, Term Params)
        if(!(globalData.getLogin() && globalData.getAIDLoadedFlag()
                && globalData.getCAPKLoadedFlag() && globalData.getEMVParamsLoadedFlag()
                && globalData.getTerminalParamsLoadedFlag())){
            //Toast.makeText(this, "Ensure Terminal Parameters are loaded", Toast.LENGTH_LONG).show();
            displayDialog("Ensure Terminal Parameters are loaded");
            return false;
        }
        return true;
    }

    private boolean performPrinterChecks(){

        int ret = -1;

        GlobalData globalData = GlobalData.getInstance();
        if(globalData.isDemoMode())
            return true;

        // if there is paper in printer
        int[] status = new int[1];
        ret = -1;
        try {
            ret = mPrinter.getPrinterStatus(status);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if(ret != 0 || status[0] != 0){
            //Toast.makeText(this, "Check Printer (paper)", Toast.LENGTH_LONG).show();
            displayDialog("Printer not ready");
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        finish();
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

}
