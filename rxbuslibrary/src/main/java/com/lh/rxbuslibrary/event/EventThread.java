package com.lh.rxbuslibrary.event;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by home on 2017/1/9.
 * Bus线程切换类
 */

public enum EventThread {

    CURRENT,//发送事件的线程
    UI,//UI线程
    COMPUTATION,//计算线程
    IO,//IO线程
    NEW_THREAD;//新线程

    public ObservableTransformer<Object,Object> toTransformer(){
        return new ObservableTransformer<Object, Object>() {
            @Override
            public ObservableSource<Object> apply(Observable<Object> upstream) {
                switch (EventThread.this){
                    case CURRENT:
                        return upstream;
                    case UI:
                        return upstream.observeOn(AndroidSchedulers.mainThread());
                    case COMPUTATION:
                        return upstream.observeOn(Schedulers.computation());
                    case IO:
                        return upstream.observeOn(Schedulers.io());
                    case NEW_THREAD:
                        return upstream.observeOn(Schedulers.newThread());
                }
                return upstream;
            }
        };
    }
}
