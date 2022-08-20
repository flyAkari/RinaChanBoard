package top.flyakari.rinachanboardcontroller;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import top.flyakari.rinachanboardcontroller.face.FacePartRecyclerViewItem;
import top.flyakari.rinachanboardcontroller.face.FacePartRecyclerViewAdapter;
import top.flyakari.rinachanboardcontroller.face.FacePartName;
import top.flyakari.rinachanboardcontroller.udp.UdpClientConnector;

public class ManualAssemblyActivity extends AppCompatActivity implements FacePartRecyclerViewAdapter.ItemOnClickCallback {
    private Button mBtnSend;
    private static int leftEyeType = 0, rightEyeType = 0, cheekType = 0, mouthType = 0;

    private ImageView mIvLeftEye, mIvRightEye, mIvLeftCheek, mIvRightCheek, mIvMouthLeft, mIvMouthRight;
    private CheckBox mCbEyeSync, mCbAutoSend;

    UdpClientConnector mUdpClientConnector;
    private List<FacePartRecyclerViewItem> leftEyeList = new ArrayList<>();
    private List<FacePartRecyclerViewItem> rightEyeList = new ArrayList<>();
    private List<FacePartRecyclerViewItem> cheekList = new ArrayList<>();
    private List<FacePartRecyclerViewItem> mouthList = new ArrayList<>();

    private static void setLeftEyeType(int type){
        leftEyeType = type;
    }
    private static void setRightEyeType(int type){
        rightEyeType = type;
    }
    private static void setCheekType(int type){
        cheekType = type;
    }
    private static void setMouthType(int type) {
        mouthType = type;
    }

    int started_ms = 0;
    boolean waiting = false;
    private final Handler mHandler = new Handler();
    private final Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if(waiting) {
                started_ms++;
                mHandler.postDelayed(mRunnable, 1);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Toast.makeText(this, str_ip, Toast.LENGTH_SHORT).show();
        setContentView(R.layout.activity_manual_assembly);
        mIvLeftEye = findViewById(R.id.iv_left_eye);
        mIvRightEye = findViewById(R.id.iv_right_eye);
        mIvLeftCheek = findViewById(R.id.iv_left_cheek);
        mIvRightCheek = findViewById(R.id.iv_right_cheek);
        mIvMouthLeft = findViewById(R.id.iv_left_mouth);
        mIvMouthRight = findViewById(R.id.iv_right_mouth);

        mCbEyeSync = findViewById(R.id.cb_eye_sync);
        mCbAutoSend = findViewById(R.id.cb_send);

        initFaceParts();
        FacePartRecyclerViewAdapter.setItemOnClickCallback(this);
        RecyclerView recyclerViewLeftEye = findViewById(R.id.recycler_view_left_eye);
        LinearLayoutManager layoutManagerLeftEye = new LinearLayoutManager(this);
        layoutManagerLeftEye.setOrientation(LinearLayoutManager.HORIZONTAL);
        layoutManagerLeftEye.setStackFromEnd(false);
        recyclerViewLeftEye.setLayoutManager(layoutManagerLeftEye);
        FacePartRecyclerViewAdapter adapterLeftEye = new FacePartRecyclerViewAdapter(leftEyeList);
        recyclerViewLeftEye.setAdapter(adapterLeftEye);

        RecyclerView recyclerViewRightEye = findViewById(R.id.recycler_view_right_eye);
        LinearLayoutManager layoutManagerRightEye = new LinearLayoutManager(this);
        layoutManagerRightEye.setOrientation(LinearLayoutManager.HORIZONTAL);
        layoutManagerRightEye.setStackFromEnd(false);
        recyclerViewRightEye.setLayoutManager(layoutManagerRightEye);
        FacePartRecyclerViewAdapter adapterRightEye = new FacePartRecyclerViewAdapter(rightEyeList);
        recyclerViewRightEye.setAdapter(adapterRightEye);

        RecyclerView recyclerViewCheek = findViewById(R.id.recycler_view_cheek);
        LinearLayoutManager layoutManagerCheek = new LinearLayoutManager(this);
        layoutManagerCheek.setOrientation(LinearLayoutManager.HORIZONTAL);
        layoutManagerCheek.setStackFromEnd(false);
        recyclerViewCheek.setLayoutManager(layoutManagerCheek);
        FacePartRecyclerViewAdapter adapterCheek = new FacePartRecyclerViewAdapter(cheekList);
        recyclerViewCheek.setAdapter(adapterCheek);

        RecyclerView recyclerViewMouth = findViewById(R.id.recycler_view_mouth);
        LinearLayoutManager layoutManagerMouth = new LinearLayoutManager(this);
        layoutManagerMouth.setOrientation(LinearLayoutManager.HORIZONTAL);
        layoutManagerMouth.setStackFromEnd(false);
        recyclerViewMouth.setLayoutManager(layoutManagerMouth);
        FacePartRecyclerViewAdapter adapterMouth = new FacePartRecyclerViewAdapter(mouthList);
        recyclerViewMouth.setAdapter(adapterMouth);

        mBtnSend = findViewById(R.id.btn_send);
        mUdpClientConnector = UdpClientConnector.getInstance();
        mBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //mUdpClientConnector.createConnector(MainActivity.getServerIp(),8888,5000);
                mUdpClientConnector.sendStr(leftEyeType+","+rightEyeType+","+mouthType+","+cheekType+",");
                started_ms = 0;
                waiting = true;
                mHandler.post(mRunnable);
            }
        });
        mUdpClientConnector.setOnConnectListener(new UdpClientConnector.ConnectListener() {
            @Override
            public void onReceiveData(String data) {
                waiting = false;
                Log.i("MainActivity", "delay:"+started_ms/2);
            }
        });
    }

    private void initFaceParts(){
        for(int i = 0; i < 21; i++){
            Resources resources = this.getResources();
            int imageIdentify = resources.getIdentifier("eye"+i, "drawable", "top.flyakari.rinachanboardcontroller" );
            FacePartRecyclerViewItem leftEye = new FacePartRecyclerViewItem(FacePartName.LeftEye, imageIdentify, i);
            FacePartRecyclerViewItem rightEye = new FacePartRecyclerViewItem(FacePartName.RightEye, imageIdentify, i);
            leftEyeList.add(leftEye);
            rightEyeList.add(rightEye);
        }
        for(int i = 0; i < 5; i++){
            Resources resources = this.getResources();
            int imageIdentify = resources.getIdentifier("cheek"+i, "drawable", "top.flyakari.rinachanboardcontroller" );
            FacePartRecyclerViewItem cheek = new FacePartRecyclerViewItem(FacePartName.Cheek, imageIdentify, i);
            cheekList.add(cheek);
        }
        for(int i = 0; i < 15; i++){
            Resources resources = this.getResources();
            int imageIdentify = resources.getIdentifier("mouth"+i, "drawable", "top.flyakari.rinachanboardcontroller" );
            FacePartRecyclerViewItem mouth = new FacePartRecyclerViewItem(FacePartName.Mouth, imageIdentify, i);
            mouthList.add(mouth);
        }
    }

    @Override
    public void onItemClick(FacePartName facePartName, int facePartId) {
        //Toast.makeText(MainActivity.this, "setPreview", Toast.LENGTH_SHORT).show();
        setPreview(facePartName, facePartId);
    }

    public void setPreview(FacePartName facePartName, int facePartId){
        switch(facePartName){
            case LeftEye:
                Resources resources_leftEye = this.getResources();
                int imageIdentify_leftEye = resources_leftEye.getIdentifier("eye"+facePartId, "drawable", "top.flyakari.rinachanboardcontroller" );
                mIvLeftEye.setImageResource(imageIdentify_leftEye);
                setLeftEyeType(facePartId);
                if(mCbEyeSync.isChecked()){
                    mIvRightEye.setImageResource(imageIdentify_leftEye);
                    setRightEyeType(facePartId);
                }
                break;
            case RightEye:
                Resources resources_rightEye = this.getResources();
                int imageIdentify_rightEye = resources_rightEye.getIdentifier("eye"+facePartId, "drawable", "top.flyakari.rinachanboardcontroller" );
                mIvRightEye.setImageResource(imageIdentify_rightEye);
                mIvRightEye.setScaleX(-1);
                setRightEyeType(facePartId);
                if(mCbEyeSync.isChecked()){
                    mIvLeftEye.setImageResource(imageIdentify_rightEye);
                    setLeftEyeType(facePartId);
                }
                break;
            case Cheek:
                Resources resources_cheek = this.getResources();
                int imageIdentify_cheek = resources_cheek.getIdentifier("cheek"+facePartId, "drawable", "top.flyakari.rinachanboardcontroller" );
                mIvLeftCheek.setImageResource(imageIdentify_cheek);
                mIvRightCheek.setImageResource(imageIdentify_cheek);
                mIvRightCheek.setScaleX(-1);
                setCheekType(facePartId);
                break;
            case Mouth:
                Resources resources_mouth = this.getResources();
                int imageIdentify_mouth = resources_mouth.getIdentifier("mouth"+facePartId, "drawable", "top.flyakari.rinachanboardcontroller" );
                mIvMouthLeft.setImageResource(imageIdentify_mouth);
                mIvMouthRight.setImageResource(imageIdentify_mouth);
                mIvMouthRight.setScaleX(-1);
                setMouthType(facePartId);
                break;
            default:
                break;
        }
        if(mCbAutoSend.isChecked()){
            mUdpClientConnector.createConnector(MainActivity.getServerIp(),8888,5000);
            mUdpClientConnector.sendStr(leftEyeType+","+rightEyeType+","+mouthType+","+cheekType+",");
        }
    }
}