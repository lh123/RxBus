package com.lh.rxbus;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.lh.rxbuslibrary.RxBus;
import com.lh.rxbuslibrary.annotation.Subscribe;
import com.lh.rxbuslibrary.event.EventThread;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        RxBus.getInstance().post(1,"Hello RxBus!");
                    }
                }).start();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        RxBus.getInstance().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        RxBus.getInstance().unRegister(this);
    }

    @Subscribe(tag = 1,scheduler = EventThread.UI)
    public void Test(String msg){
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
    }
}
