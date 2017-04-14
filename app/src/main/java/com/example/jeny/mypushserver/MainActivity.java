package com.example.jeny.mypushserver;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Sender;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";

    EditText messageInput;

    /**
     * 서버 : Sender 객체 선언
     */
    Sender sender;

    Handler handler = new Handler();

    /**
     * collapseKey 설정을 위한 Random 객체
     */
    private Random random ;

    /**
     * 구글 서버에 메시지 보관하는 기간(초단위로 4주까지 가능)
     */
    private int TTLTime = 60;

    /**
     * 단말기에 메시지 전송 재시도 횟수
     */
    private	int RETRY = 3;

    /*
     * 등록된 ID 저장
     */
    ArrayList<String> idList = new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 서버 : GOOGLE_API_KEY를 이용해 Sender 초기화
        sender = new Sender(GCMInfo.GOOGLE_API_KEY);

        // 서버 : 전송할 메시지 입력 박스
        messageInput = (EditText) findViewById(R.id.messageInput);

        // 서버 : 전송하기 버튼
        Button sendButton = (Button) findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String data = messageInput.getText().toString();

                sendToDevice(data);
            }
        });

    }


    /**
     * 푸시 메시지 전송
     */
    private void sendToDevice(String data) {

        SendThread thread = new SendThread(data);
        thread.start();

    }



    private void println(String msg) {
        final String output = msg;
        handler.post(new Runnable() {
            public void run() {
                Log.d(TAG, output);
                Toast.makeText(getApplicationContext(), output, Toast.LENGTH_LONG).show();
            }
        });
    }


    /**
     * GCM 메시지 전송을 위한 스레드
     */
    class SendThread extends Thread {
        String data;
        String regId;

        public SendThread(String inData) {
            data = inData;
        }

        public void run() {

            try {

                // 단말의 ID 를 DB 에서 가져와서 idList 에 저장하기 by Mulder

                idList.clear();

                // S2 Black 01053054262
                regId = "APA91bEVxxZatJ-Tdut9RcZbJ0ffoyVzO4fnf8920S3fGkBj2PIV4itXhHNdalhCVzZ_8_-zh7_b08I1Jyt6cO2rzJZNxQ1dSYS__VwD9GP-GyasGIUgJsV1fWvJolaE6FBpwk3gbP2m";
                idList.add(regId);

                // S2 White 01094043483
                regId = "APA91bGXGEy9f6BrmpFA2s-VD9d3EGo5t0L3apW24yDlGGNEbuowJTUiRlP8s1skEYWLmGjuxv6naGAGd0mLzqqxv3KvU_Q-vyMOr6R_jcNIzutWwCvb8d3p1UdL4wKUYR1B0rPJRVT2";
                idList.add(regId);

                // G Flex  01031287657
                regId = "APA91bHcKY8UxC2bDcKoEIPfkJryjPgzXwLwpXT1NodcGNb4MQUcnRn3D88tNIfogs2mLm2lUfQ6quEAyUsn-dfKfruAH-aSuZrkOEc756teD_NUIq7SsIknAcudOpp6zSfo_QHY0jsE";
                idList.add(regId);
                
                sendText(data);
            } catch(Exception ex) {
                ex.printStackTrace();
            }

        }

        public void sendText(String msg)
                throws Exception
        {

            if( random == null){
                random = new Random(System.currentTimeMillis());
            }

            String messageCollapseKey = String.valueOf(Math.abs(random.nextInt()));

            try {
                // 푸시 메시지 전송을 위한 메시지 객체 생성 및 환경 설정
                Message.Builder gcmMessageBuilder = new Message.Builder();
                gcmMessageBuilder.collapseKey(messageCollapseKey).delayWhileIdle(true).timeToLive(TTLTime);
                gcmMessageBuilder.addData("type","text");
                gcmMessageBuilder.addData("command", "show");
                gcmMessageBuilder.addData("data", URLEncoder.encode(data, "UTF-8"));

                Message gcmMessage = gcmMessageBuilder.build();

                // 여러 단말에 메시지 전송 후 결과 확인
                MulticastResult resultMessage = sender.send(gcmMessage, idList, RETRY);
                String output = "GCM 전송 메시지 결과 => " + resultMessage.getMulticastId()
                        + "," + resultMessage.getRetryMulticastIds() + "," + resultMessage.getSuccess();

                println(output);

            } catch(Exception ex) {
                ex.printStackTrace();

                String output = "GCM 메시지 전송 과정에서 에러 발생 : " + ex.toString();
                println(output);

            }

        }
    }



}
