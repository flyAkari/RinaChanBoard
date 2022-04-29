package top.flyakari.rinachanboardcontroller;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements FacePartAdapter.ItemOnClickCallback {
    private Button mBtnSend;
    private static int leftEyeType = 0, rightEyeType = 0, cheekType = 0, mouthType = 0;
    public static String str_ip;

    private ImageView ivLeftEye, ivRightEye, ivLeftCheek, ivRightCheek, ivLeftMouth, ivRightMouth;
    private CheckBox mCbEyeSync, mCbSend;

    UdpClientConnecter udpClientConnecter;
    private List<FacePart> leftEyeList = new ArrayList<>();
    private List<FacePart> rightEyeList = new ArrayList<>();
    private List<FacePart> cheekList = new ArrayList<>();
    private List<FacePart> mouthList = new ArrayList<>();

    public static void setLeftEyeType(int type){
        leftEyeType = type;
    }
    public static void setRightEyeType(int type){
        rightEyeType = type;
    }
    public static void setCheekType(int type){
        cheekType = type;
    }
    public static void setMouthType(int type) {
        mouthType = type;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == -1) {
                // 没有Write权限，动态获取
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                        Manifest.permission.INTERNET}, 1);
                Log.d("TAG", "onCreate: 申请获得权限！");
            } else {
                Log.d("TAG", "onCreate: 已获得权限！");
            }
        }
        //Toast.makeText(this, str_ip, Toast.LENGTH_SHORT).show();
        setContentView(R.layout.activity_main);
        ivLeftEye = findViewById(R.id.iv_left_eye);
        ivRightEye = findViewById(R.id.iv_right_eye);
        ivLeftCheek = findViewById(R.id.iv_left_cheek);
        ivRightCheek = findViewById(R.id.iv_right_cheek);
        ivLeftMouth = findViewById(R.id.iv_left_mouth);
        ivRightMouth = findViewById(R.id.iv_right_mouth);

        mCbEyeSync = findViewById(R.id.cb_eye_sync);
        mCbSend = findViewById(R.id.cb_send);

        initFaceParts();
        FacePartAdapter.setItemOnClickCallback(this);
        RecyclerView recyclerViewLeftEye = findViewById(R.id.recycler_view_left_eye);
        LinearLayoutManager layoutManagerLeftEye = new LinearLayoutManager(this);
        layoutManagerLeftEye.setOrientation(LinearLayoutManager.HORIZONTAL);
        layoutManagerLeftEye.setStackFromEnd(false);
        recyclerViewLeftEye.setLayoutManager(layoutManagerLeftEye);
        FacePartAdapter adapterLeftEye = new FacePartAdapter(leftEyeList);
        recyclerViewLeftEye.setAdapter(adapterLeftEye);

        RecyclerView recyclerViewRightEye = findViewById(R.id.recycler_view_right_eye);
        LinearLayoutManager layoutManagerRightEye = new LinearLayoutManager(this);
        layoutManagerRightEye.setOrientation(LinearLayoutManager.HORIZONTAL);
        layoutManagerRightEye.setStackFromEnd(false);
        recyclerViewRightEye.setLayoutManager(layoutManagerRightEye);
        FacePartAdapter adapterRightEye = new FacePartAdapter(rightEyeList);
        recyclerViewRightEye.setAdapter(adapterRightEye);

        RecyclerView recyclerViewCheek = findViewById(R.id.recycler_view_cheek);
        LinearLayoutManager layoutManagerCheek = new LinearLayoutManager(this);
        layoutManagerCheek.setOrientation(LinearLayoutManager.HORIZONTAL);
        layoutManagerCheek.setStackFromEnd(false);
        recyclerViewCheek.setLayoutManager(layoutManagerCheek);
        FacePartAdapter adapterCheek = new FacePartAdapter(cheekList);
        recyclerViewCheek.setAdapter(adapterCheek);

        RecyclerView recyclerViewMouth = findViewById(R.id.recycler_view_mouth);
        LinearLayoutManager layoutManagerMouth = new LinearLayoutManager(this);
        layoutManagerMouth.setOrientation(LinearLayoutManager.HORIZONTAL);
        layoutManagerMouth.setStackFromEnd(false);
        recyclerViewMouth.setLayoutManager(layoutManagerMouth);
        FacePartAdapter adapterMouth = new FacePartAdapter(mouthList);
        recyclerViewMouth.setAdapter(adapterMouth);

        mBtnSend = findViewById(R.id.btn_send);
        udpClientConnecter = UdpClientConnecter.getInstance();
        mBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                udpClientConnecter.createConnector(str_ip,8888,5000);
                udpClientConnecter.sendStr(leftEyeType+","+rightEyeType+","+mouthType+","+cheekType+",");
            }
        });
        udpClientConnecter.setOnConnectListener(new UdpClientConnecter.ConnectListener() {
            @Override
            public void onReceiveData(String data) {
                Log.i("MainActivity", data);
            }
        });
    }

    private void initFaceParts(){
        for(int i = 0; i < 21; i++){
            Resources resources = this.getResources();
            int imageIdentify = resources.getIdentifier("eye"+i, "drawable", "top.flyakari.rinachanboardcontroller" );
            FacePart leftEye = new FacePart(FacePartName.LeftEye, imageIdentify, i);
            FacePart rightEye = new FacePart(FacePartName.RightEye, imageIdentify, i);
            leftEyeList.add(leftEye);
            rightEyeList.add(rightEye);
        }
        for(int i = 0; i < 5; i++){
            Resources resources = this.getResources();
            int imageIdentify = resources.getIdentifier("cheek"+i, "drawable", "top.flyakari.rinachanboardcontroller" );
            FacePart cheek = new FacePart(FacePartName.Cheek, imageIdentify, i);
            cheekList.add(cheek);
        }
        for(int i = 0; i < 13; i++){
            Resources resources = this.getResources();
            int imageIdentify = resources.getIdentifier("mouth"+i, "drawable", "top.flyakari.rinachanboardcontroller" );
            FacePart mouth = new FacePart(FacePartName.Mouth, imageIdentify, i);
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
                ivLeftEye.setImageResource(imageIdentify_leftEye);
                setLeftEyeType(facePartId);
                if(mCbEyeSync.isChecked()){
                    ivRightEye.setImageResource(imageIdentify_leftEye);
                    setRightEyeType(facePartId);
                }
                break;
            case RightEye:
                Resources resources_rightEye = this.getResources();
                int imageIdentify_rightEye = resources_rightEye.getIdentifier("eye"+facePartId, "drawable", "top.flyakari.rinachanboardcontroller" );
                ivRightEye.setImageResource(imageIdentify_rightEye);
                ivRightEye.setScaleX(-1);
                setRightEyeType(facePartId);
                if(mCbEyeSync.isChecked()){
                    ivLeftEye.setImageResource(imageIdentify_rightEye);
                    setLeftEyeType(facePartId);
                }
                break;
            case Cheek:
                Resources resources_cheek = this.getResources();
                int imageIdentify_cheek = resources_cheek.getIdentifier("cheek"+facePartId, "drawable", "top.flyakari.rinachanboardcontroller" );
                ivLeftCheek.setImageResource(imageIdentify_cheek);
                ivRightCheek.setImageResource(imageIdentify_cheek);
                ivRightCheek.setScaleX(-1);
                setCheekType(facePartId);
                break;
            case Mouth:
                Resources resources_mouth = this.getResources();
                int imageIdentify_mouth = resources_mouth.getIdentifier("mouth"+facePartId, "drawable", "top.flyakari.rinachanboardcontroller" );
                ivLeftMouth.setImageResource(imageIdentify_mouth);
                ivRightMouth.setImageResource(imageIdentify_mouth);
                ivRightMouth.setScaleX(-1);
                setMouthType(facePartId);
                break;
            default:
                break;
        }
        if(mCbSend.isChecked()){
            udpClientConnecter.createConnector(str_ip,8888,5000);
            udpClientConnecter.sendStr(leftEyeType+","+rightEyeType+","+mouthType+","+cheekType+",");
        }
    }


    
}