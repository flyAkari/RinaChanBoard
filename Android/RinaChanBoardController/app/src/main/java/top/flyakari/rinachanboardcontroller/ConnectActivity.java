package top.flyakari.rinachanboardcontroller;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Pattern;

public class ConnectActivity extends AppCompatActivity {
    private Button mBtnConnect;
    private TextView mTvReply;
    private EditText mEtIP;
    private UdpClientConnecter udpClientConnecter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        mTvReply = findViewById(R.id.tv_reply1);
        mBtnConnect = findViewById(R.id.btn_connect);
        mEtIP = findViewById(R.id.et_ip);
    }

    @Override
    protected void onResume() {
        super.onResume();
        udpClientConnecter = UdpClientConnecter.getInstance();

        mBtnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String server_ip = new String(mEtIP.getText().toString());
                if(!checkIP(server_ip)){
                    Toast.makeText(ConnectActivity.this, "IP format wrong!", Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.i("ConnectActivity", "IP:"+server_ip);
                udpClientConnecter.createConnector(server_ip,8888,5000);
                udpClientConnecter.sendStr("RinaBoardUdpTest");
                MainActivity.str_ip = server_ip;
            }
        });
        udpClientConnecter.setOnConnectListener(new UdpClientConnecter.ConnectListener() {
            @Override
            public void onReceiveData(String data) {
                Log.i("ConnectActivity","onReceiveData");
                mTvReply.setText(data);
                if(data.startsWith("RinaboardIsOn")) {
                    Intent intent = new Intent(ConnectActivity.this, MainActivity.class);
                    startActivity(intent);
                }else{
                    Toast.makeText(ConnectActivity.this, "Wrong Reply", Toast.LENGTH_SHORT).show();
                }
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