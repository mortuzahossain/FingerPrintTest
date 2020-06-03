package io.github.mortuzahossain.fingerprinttest;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.nitgen.SDK.AndroidBSP.NBioBSPJNI;
import com.nitgen.SDK.AndroidBSP.StaticVals;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    public static final int QUALITY_LIMIT = 60;
    public static final int APPROVED_QUALITY_LIMIT = 70;
    private NBioBSPJNI bsp;
    private NBioBSPJNI.Export exportEngine;
    private NBioBSPJNI.IndexSearch indexSearch;

    private int nFIQ = 0;
    private String msg = "";
    private boolean bCapturedFirst, bAutoOn = false;
    String fingerData = "";
    //DialogFragment sampleDialogFragment;

    private boolean customerFingerVerificationRequired = false;


    @BindView(R.id.tv_fp_act_device_status)
    TextView tvFpDeviceStatus;
    @BindView(R.id.tv_fp_act_finger_print_status)
    TextView tvFpPrintStatus;
    @BindView(R.id.tv_fp_act_finger_print_quality)
    TextView tvFpQuality;
    @BindView(R.id.tv_top_title)
    TextView tvTitleTop;
    @BindView(R.id.imv_finger_print)
    ImageView imvFpPrint;
    @BindView(R.id.btn_fp_act_open_device)
    Button btnFpOpenDevice;
    @BindView(R.id.btn_fp_act_capture)
    Button btnFpCapture;
    @BindView(R.id.btn_fp_act_finger_print_verify)
    Button btnFpVerify;
    @BindView(R.id.btn_fp_act_finger_print_submit)
    Button btnFpSubmit;
    @BindView(R.id.progressbar_finger_print)
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        init();
        initData();
        bsp.OpenDevice();

        //sampleDialogFragment.show(getSupportFragmentManager(), "DIALOG_TYPE_PROGRESS");

    }

    private void init() {

        fingerData = "";
        tvFpDeviceStatus.setText("Not connected");
        tvFpDeviceStatus.setTextColor(Color.RED);

        imvFpPrint.setImageDrawable(getResources().getDrawable(R.drawable.logo_finger_print));

        tvFpPrintStatus.setText("Not captured yet!");
        tvFpPrintStatus.setTextColor(Color.RED);

        tvFpQuality.setText("0");

        btnFpCapture.setClickable(false);
        btnFpCapture.setBackgroundTintList(this.getResources().getColorStateList(R.color.colorAccent));

        btnFpVerify.setClickable(false);
        btnFpVerify.setBackgroundTintList(this.getResources().getColorStateList(R.color.colorAccent));

        btnFpSubmit.setClickable(false);
        btnFpSubmit.setBackgroundTintList(this.getResources().getColorStateList(R.color.colorAccent));

    }

    public void initData() {

        NBioBSPJNI.CURRENT_PRODUCT_ID = 0;
        if (bsp == null) {
            bsp = new NBioBSPJNI("010701-613E5C7F4CC7C4B0-72E340B47E034015", this, mCallback);
            String msg = null;
            if (bsp.IsErrorOccured())
                msg = "NBioBSP Error: " + bsp.GetErrorCode();
            else {
                msg = "SDK Version: " + bsp.GetVersion();
                exportEngine = bsp.new Export();
                indexSearch = bsp.new IndexSearch();
            }
            //tvDeviceStatus.setText(msg);
            fingerData = "";
            //sampleDialogFragment = new SampleDialogFragment();
        }
    }

    NBioBSPJNI.CAPTURE_CALLBACK mCallback = new NBioBSPJNI.CAPTURE_CALLBACK() {
        @Override
        public int OnCaptured(NBioBSPJNI.CAPTURED_DATA capturedData) {

            //tilUserId.getEditText().setText("IMAGE Quality: "+capturedData.getImageQuality());


            if (capturedData.getImage() != null) {
                imvFpPrint.setImageDrawable(null);
                imvFpPrint.setImageBitmap(null);
                imvFpPrint.setImageBitmap(capturedData.getImage());
                tvFpQuality.setText(String.valueOf(capturedData.getImageQuality()));
            }

            // quality : 40~100
            if (capturedData.getImageQuality() >= QUALITY_LIMIT) {
//                if (sampleDialogFragment != null && "DIALOG_TYPE_PROGRESS".equals(sampleDialogFragment.getTag()))
//                    sampleDialogFragment.dismiss();
                return NBioBSPJNI.ERROR.NBioAPIERROR_USER_CANCEL;
            } else if (capturedData.getDeviceError() != NBioBSPJNI.ERROR.NBioAPIERROR_NONE) {
//                if (sampleDialogFragment != null && "DIALOG_TYPE_PROGRESS".equals(sampleDialogFragment.getTag()))
//                    sampleDialogFragment.dismiss();
                return capturedData.getDeviceError();
            } else {
                return NBioBSPJNI.ERROR.NBioAPIERROR_NONE;
            }
        }

        @Override
        public void OnConnected() {

//            if (sampleDialogFragment != null)
//                sampleDialogFragment.dismiss();

            String message = "Connected";
            fingerData = "";

            ByteBuffer deviceId = ByteBuffer.allocate(StaticVals.wLength_GET_ID);
            deviceId.order(ByteOrder.BIG_ENDIAN);
            bsp.getDeviceID(deviceId.array());

            if (bsp.IsErrorOccured()) {
                msg = "NBioBSP GetDeviceID Error: " + bsp.GetErrorCode();
                tvFpDeviceStatus.setText(msg);
                return;
            }

            ByteBuffer setValue = ByteBuffer.allocate(StaticVals.wLength_SET_VALUE);
            setValue.order(ByteOrder.BIG_ENDIAN);

            byte[] src = new byte[StaticVals.wLength_SET_VALUE];
            for (int i = 0; i < src.length; i++) {
                src[i] = 1;
            }
            setValue.put(src);
            bsp.setValue(setValue.array());

            if (bsp.IsErrorOccured()) {
                msg = "NBioBSP SetValue Error: " + bsp.GetErrorCode();
                tvFpDeviceStatus.setText(msg);
                return;
            }

            ByteBuffer getvalue = ByteBuffer.allocate(StaticVals.wLength_GET_VALUE);
            getvalue.order(ByteOrder.BIG_ENDIAN);
            bsp.getValue(getvalue.array());

            if (bsp.IsErrorOccured()) {
                msg = "NBioBSP GetValue Error: " + bsp.GetErrorCode();
                tvFpDeviceStatus.setText(msg);
                return;
            }
            src = new byte[StaticVals.wLength_SET_VALUE];
            System.arraycopy(getvalue.array(), 0, src, 0, StaticVals.wLength_GET_VALUE);
//	        message += " \n";
//	        for(int i=0;i<src.length;i++){
//	        	message += src[i];
//	        }

            NBioBSPJNI.INIT_INFO_0 init_info_0 = bsp.new INIT_INFO_0();
            bsp.GetInitInfo(init_info_0);

            NBioBSPJNI.CAPTURE_QUALITY_INFO mCAPTURE_QUALITY_INFO = bsp.new CAPTURE_QUALITY_INFO();
            bsp.GetCaptureQualityInfo(mCAPTURE_QUALITY_INFO);

            mCAPTURE_QUALITY_INFO.EnrollCoreQuality = 70;
            mCAPTURE_QUALITY_INFO.EnrollFeatureQuality = 30;
            mCAPTURE_QUALITY_INFO.VerifyCoreQuality = 70;
            mCAPTURE_QUALITY_INFO.VerifyFeatureQuality = 30;
            bsp.SetCaptureQualityInfo(mCAPTURE_QUALITY_INFO);


//			message = message +":"+init_info_0.EnrollImageQuality;

            tvFpDeviceStatus.setText(message);
            //tvFpDeviceStatus.setTextColor(getResources().getColor(R.color.netConnectedColor));
            btnFpCapture.setClickable(true);
            btnFpCapture.setBackgroundTintList(MainActivity.this.getResources().getColorStateList(R.color.colorPrimary));

        }

        @Override
        public void OnDisConnected() {

            NBioBSPJNI.CURRENT_PRODUCT_ID = 0;

//            if (sampleDialogFragment != null)
//                sampleDialogFragment.dismiss();

            //ShowToast.showTastyWarningToast(FingerPrintActivity.this, "Device disconnected");
            Toast.makeText(MainActivity.this, "Device disconnected", Toast.LENGTH_SHORT).show();

            String message = "NBioBSP Disconnected: " + bsp.GetErrorCode();
            init();
        }
    };

    @OnClick(R.id.btn_fp_act_open_device)
    public void openDevice(){
//        sampleDialogFragment.show(getFragmentManager(), "DIALOG_TYPE_PROGRESS");
        bsp.OpenDevice();
    }

    @OnClick(R.id.btn_fp_act_capture)
    public void captureFingerPrint(){
        new Thread(new Runnable() {

            public void run() {
                OnCaptureSingleFinger();
            }
        }).start();
    }

    private void captureResultSuccess(String fingerData){

        //TastyToast.makeText(FingerPrintActivity.this,"Success!",TastyToast.LENGTH_LONG,TastyToast.SUCCESS).show();

        //tvFingerPrintData.setText(fingerData);

        tvFpPrintStatus.setText("Capture successful!");
//        tvFpPrintStatus.setTextColor(getResources().getColorStateList(R.color.netConnectedColor));

        btnFpVerify.setClickable(true);
        btnFpVerify.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimary));
    }

    private void captureResultFailed(String msg){
        //tvFingerPrintData.setText(msg);

//        TastyToast.makeText(FingerPrintActivity.this,"Failed to capture finger print, try again!",TastyToast.LENGTH_LONG,TastyToast.ERROR).show();

        Toast.makeText(this, "Failed to capture finger print, try again!", Toast.LENGTH_SHORT).show();

        tvFpPrintStatus.setText("Capture unsuccessful!");
        tvFpPrintStatus.setTextColor(Color.RED);

        tvFpQuality.setText("0");

        btnFpVerify.setClickable(false);
        btnFpVerify.setBackgroundTintList(getResources().getColorStateList(R.color.colorAccent));
    }

    public synchronized void OnCaptureSingleFinger(){

        NBioBSPJNI.FIR_HANDLE hSavedFIR;

        hSavedFIR = bsp.new FIR_HANDLE();

        bsp.Capture(hSavedFIR);

//        if(sampleDialogFragment!=null && "DIALOG_TYPE_PROGRESS".equals(sampleDialogFragment.getTag()))
//            sampleDialogFragment.dismiss();

        if (!bsp.IsErrorOccured())  {

            NBioBSPJNI.FIR_TEXTENCODE textSavedFIR;
            textSavedFIR = bsp.new FIR_TEXTENCODE();
            bsp.GetTextFIRFromHandle(hSavedFIR, textSavedFIR);
            //bsp.GetTextFIRFromHandle(hSavedFIR, textSavedFIR,NBioBSPJNI.FIR_FORMAT.STANDARD);

            fingerData = textSavedFIR.TextFIR;
        }
        else {
            msg = "NBioBSP Capture Error: " + bsp.GetErrorCode();
        }

        runOnUiThread(new Runnable() {

            public void run() {
                if (fingerData != null)  {
                    captureResultSuccess(fingerData);
                }else{
                    captureResultFailed("Capture error! Try again.");
                }
            }
        });
    }

    @Override
    public void onDestroy(){

        if (bsp != null) {
            bsp.dispose();
            bsp = null;
        }
        super.onDestroy();

    }


}