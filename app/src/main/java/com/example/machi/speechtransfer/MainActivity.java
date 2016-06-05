package com.example.machi.speechtransfer;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends Activity implements View.OnClickListener {
    private final static int ICode = 0;
    private final static String SPEECH_SERVER = "192.168.0.254";
    private final static int SPEECH_SERVER_PORT = 10080;
    private final static String LOG_TAG = "SpeechTransfer";
    public String m_msg;

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.speechButton) {
            try {
                Intent it = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                it.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                it.putExtra(RecognizerIntent.EXTRA_PROMPT, "しゃべったことをRaspberryPiに送信するよ！");
                it.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
                startActivityForResult(it, ICode);
            } catch (ActivityNotFoundException e) {
                Log.e(LOG_TAG, "Speech recognize app doesn't exist!");
            }
        }
        else if (view.getId() == R.id.sendButton) {
            EditText myEditText = (EditText)findViewById(R.id.speechText);
            m_msg = myEditText.getText().toString().trim();
            if (!m_msg.isEmpty()) {
                sendPostRequest();
                myEditText.setText("");
            }
        }
        else {
            EditText myEditText = (EditText)findViewById(R.id.speechText);
            Button pushedButton = (Button)view;
            String buttonLabel = pushedButton.getText().toString();
            myEditText.setText(buttonLabel);
            m_msg = buttonLabel;
            sendPostRequest();
        }
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode==ICode && resultCode==RESULT_OK) {
            String msg="";
            ArrayList<String> results=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            for (int i=0;i<results.size();i++) {
                msg+=results.get(i);
            }
            //TODO: Send this text to Raspberry pi (run some server)
            EditText myEditText = (EditText)findViewById(R.id.speechText);
            myEditText.setText(msg);
            m_msg = msg;
            sendPostRequest();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.speechButton).setOnClickListener(this);
        findViewById(R.id.sendButton).setOnClickListener(this);
        findViewById(R.id.goodmorning).setOnClickListener(this);
        findViewById(R.id.hello).setOnClickListener(this);
        findViewById(R.id.goodnight).setOnClickListener(this);
    }

    private void sendPostRequest() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = SPEECH_SERVER;
                int port = SPEECH_SERVER_PORT;
                HttpURLConnection conn = null;
                try {
                    conn = (HttpURLConnection) new URL("http", url, port, "").openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setRequestProperty("Content-Type", "text/plain; charset=UTF-8");
                    conn.connect();
                    Log.i(LOG_TAG, "connected: server=" + url + ", port=" + String.valueOf(port));

                    m_msg = "あああああ、" + m_msg;  //Somehow aplay didn't play first sentence, so add dummy sentence
                    DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                    os.write(m_msg.getBytes("UTF-8"));
                    os.flush();
                    os.close();

                    if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                        Log.e(LOG_TAG, "Server returned error code " + String.valueOf(conn.getResponseCode()));
                    }
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Got exception during sendPostRequest(): " + e.getMessage());
                } finally {
                    if (conn != null) {
                        conn.disconnect();
                    }
                }
            }
        }).start();
    }
}
