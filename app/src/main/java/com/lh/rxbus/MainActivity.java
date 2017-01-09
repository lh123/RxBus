package com.lh.rxbus;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.lh.rxbuslibrary.BusSchedulers;
import com.lh.rxbuslibrary.RxBus;
import com.lh.rxbuslibrary.Subscribe;

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
                        RxBus.getInstance().post("Hello RxBus!");
                    }
                }).start();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        RxBus.getInstance().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        RxBus.getInstance().unRegister(this);
    }


    @Subscribe(scheduler = BusSchedulers.UI)
    public void Test(String str){
        Toast.makeText(getApplicationContext(),str,Toast.LENGTH_SHORT).show();
    }
}
