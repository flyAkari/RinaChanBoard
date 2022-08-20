package top.flyakari.rinachanboardcontroller;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Pattern;

import top.flyakari.rinachanboardcontroller.udp.UdpClientConnector;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Button mBtnManual, mBtnLive, mBtnCheck;
    private TextView mTvReply;
    private EditText mEtIP;
    private UdpClientConnector mUdpClientConnector;
    private static String server_ip;

    public static String getServerIp(){
        return server_ip;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == -1) {
//                ActivityCompat.requestPermissions(MainActivity.this, new String[]{
//                        Manifest.permission.INTERNET}, 1);
//                Log.d("TAG", "onCreate: 申请获得权限！");
//            } else {
//                Log.d("TAG", "onCreate: 已获得权限！");
//            }
//        }
        setContentView(R.layout.activity_main);
        mTvReply = findViewById(R.id.tv_reply1);
        mBtnManual = findViewById(R.id.btn_manual_activity);
        mBtnLive = findViewById(R.id.btn_live_activity);
        mBtnCheck = findViewById(R.id.btn_check);
        mEtIP = findViewById(R.id.et_ip);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mUdpClientConnector = UdpClientConnector.getInstance();

        mBtnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                server_ip = mEtIP.getText().toString();
                mUdpClientConnector.createConnector(server_ip,8888,1);
                mUdpClientConnector.sendStr("RinaBoardUdpTest");
                if(!checkIP(server_ip)){
                    Toast.makeText(MainActivity.this, R.string.ip_format_wrong, Toast.LENGTH_SHORT).show();
                }else {
                    Log.i(TAG, "IP:" + server_ip);
                }
            }
        });

        mBtnManual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ManualAssemblyActivity.class);
                startActivity(intent);
            }
        });

        mBtnLive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, PresetLiveActivity.class);
                startActivity(intent);
            }
        });

        mUdpClientConnector.setOnConnectListener(new UdpClientConnector.ConnectListener() {
            @Override
            public void onReceiveData(String data) {
                Log.i("ConnectActivity","onReceiveData"+data);
                mTvReply.setText(data);
            }
        });
    }

    private boolean checkIP(String str) {
        Pattern pattern = Pattern
                .compile("^((\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]"
                        + "|[*])\\.){3}(\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]|[*])$");
        return pattern.matcher(str).matches();
    }
}