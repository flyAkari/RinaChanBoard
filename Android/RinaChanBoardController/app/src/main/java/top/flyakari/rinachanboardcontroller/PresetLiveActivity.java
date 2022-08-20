package top.flyakari.rinachanboardcontroller;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import top.flyakari.rinachanboardcontroller.face.FacePartName;
import top.flyakari.rinachanboardcontroller.udp.UdpClientConnector;

public class PresetLiveActivity extends AppCompatActivity {
    MediaPlayer mMediaPlayer;
    InputStream mInputStream;
    InputStreamReader mInputStreamReader;
    BufferedReader mReader;
    Button mBtnSong1, mBtnStop;
    ProgressBar mProgressBar;
    UdpClientConnector mUdpClientConnector;
    int next_ms = 0;
    private Boolean mIsPlaying = false;
    private final static String TAG = "MusicPlayerActivity";
    private final static int fps = 10;
    private final Handler mHandler = new Handler();
    private final Runnable mRunnable = new Runnable() {
        String line;
        boolean isReadTime = true;
        String str1 = "", str2 = "";

        @Override
        public void run() {
            int ts_ms = mMediaPlayer.getCurrentPosition();
            if (isReadTime) {
                try {
                    line = mReader.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (null != line) {
                    String[] str = line.split(String.valueOf('!'));
                    str1 = str[0];
                    str2 = str[1];
                    int frame = Integer.parseInt(str1);
                    next_ms = frame * 1000 / fps;
                }
            }
            Log.i(TAG, "currentPos:"+mMediaPlayer.getCurrentPosition()+" duration:"+mMediaPlayer.getDuration());
            mProgressBar.setProgress((int)((double)mMediaPlayer.getCurrentPosition()/(double)mMediaPlayer.getDuration()*100));
            if (ts_ms >= next_ms) {
                sendCurrentCode(str2);
                Log.i(TAG, "ms:"+ts_ms+" frame:" + str1 + " code:" + str2);
                isReadTime = true;
            } else {
                isReadTime = false;
            }
            if(mIsPlaying) {
                mHandler.postDelayed(mRunnable, 1);
            }
        }
    };

    private void sendCurrentCode(String str) {
        if(null == str) {
            return;
        }
        String[] temp = str.split(String.valueOf(','));
        String str_eyel = temp[0];
        String str_eyer = temp[1];
        String str_mouth = temp[2];
        String str_cheek = temp[3];
        int eyelid = Integer.parseInt(str_eyel);
        int eyerid = Integer.parseInt(str_eyer);
        int cheekid = Integer.parseInt(str_cheek);
        int mouthid = Integer.parseInt(str_mouth);
        setPreview(FacePartName.LeftEye, eyelid);
        setPreview(FacePartName.RightEye, eyerid);
        setPreview(FacePartName.Cheek, cheekid);
        setPreview(FacePartName.Mouth, mouthid);
        mUdpClientConnector.sendStr(str+",");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preset_live);
        mBtnSong1 = findViewById(R.id.btn_poppinup);
        mBtnStop = findViewById(R.id.btn_stop);
        mProgressBar = findViewById(R.id.progressBar);
        mUdpClientConnector = UdpClientConnector.getInstance();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == -1) {
                // 没有Write权限，动态获取
                ActivityCompat.requestPermissions(PresetLiveActivity.this, new String[]{
                        Manifest.permission.INTERNET}, 1);
                Log.d("TAG", "onCreate: 申请获得权限！");
            } else {
                Log.d("TAG", "onCreate: 已获得权限！");
            }
        }

        ivLeftEye = findViewById(R.id.iv_left_eye_live);
        ivRightEye = findViewById(R.id.iv_right_eye_live);
        ivLeftCheek = findViewById(R.id.iv_left_cheek_live);
        ivRightCheek = findViewById(R.id.iv_right_cheek_live);
        ivLeftMouth = findViewById(R.id.iv_left_mouth_live);
        ivRightMouth = findViewById(R.id.iv_right_mouth_live);


        mBtnSong1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(null != mMediaPlayer) {
                    mMediaPlayer.stop();
                    mIsPlaying = false;
                    next_ms = 0;
                }
                mInputStream = getResources().openRawResource(R.raw.poppin_up_code);
                mInputStreamReader = new InputStreamReader(mInputStream, StandardCharsets.UTF_8);
                mReader = new BufferedReader(mInputStreamReader);
                mIsPlaying = true;
                mHandler.post(mRunnable);
                mMediaPlayer = MediaPlayer.create(PresetLiveActivity.this, R.raw.poppin_up);
                mMediaPlayer.start();
                mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        Toast.makeText(PresetLiveActivity.this, "Complete!", Toast.LENGTH_LONG).show();
                        mIsPlaying = false;
                        next_ms = 0;
                    }
                });
            }
        });

        mBtnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMediaPlayer.stop();
                mIsPlaying = false;
                next_ms = 0;
            }
        });
    }

    private ImageView ivLeftEye, ivRightEye, ivLeftCheek, ivRightCheek, ivLeftMouth, ivRightMouth;

    public void setPreview(FacePartName facePartName, int facePartId) {
        switch (facePartName) {
            case LeftEye:
                Resources resources_leftEye = this.getResources();
                int imageIdentify_leftEye = resources_leftEye.getIdentifier("eye" + facePartId, "drawable", "top.flyakari.rinachanboardcontroller");
                ivLeftEye.setImageResource(imageIdentify_leftEye);
                break;
            case RightEye:
                Resources resources_rightEye = this.getResources();
                int imageIdentify_rightEye = resources_rightEye.getIdentifier("eye" + facePartId, "drawable", "top.flyakari.rinachanboardcontroller");
                ivRightEye.setImageResource(imageIdentify_rightEye);
                ivRightEye.setScaleX(-1);
                break;
            case Cheek:
                Resources resources_cheek = this.getResources();
                int imageIdentify_cheek = resources_cheek.getIdentifier("cheek" + facePartId, "drawable", "top.flyakari.rinachanboardcontroller");
                ivLeftCheek.setImageResource(imageIdentify_cheek);
                ivRightCheek.setImageResource(imageIdentify_cheek);
                ivRightCheek.setScaleX(-1);
                break;
            case Mouth:
                Resources resources_mouth = this.getResources();
                int imageIdentify_mouth = resources_mouth.getIdentifier("mouth" + facePartId, "drawable", "top.flyakari.rinachanboardcontroller");
                ivLeftMouth.setImageResource(imageIdentify_mouth);
                ivRightMouth.setImageResource(imageIdentify_mouth);
                ivRightMouth.setScaleX(-1);
                break;
            default:
                break;
        }
    }
}