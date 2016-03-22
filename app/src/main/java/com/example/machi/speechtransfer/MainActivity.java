package com.example.machi.speechtransfer;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private final static int ICode = 0;

    @Override
    public void onClick(View view) {
        try {
            Intent it=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            it.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            it.putExtra(RecognizerIntent.EXTRA_PROMPT,"しゃべったことをRaspberryPiに送信するよ！");
            it.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,1);
            startActivityForResult(it,ICode);
        }  catch (ActivityNotFoundException e) {
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
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.speechButton).setOnClickListener(this);
    }
}
