package com.lh.rxbuslibrary;

/**
 * Created by home on 2017/1/9.
 * Bus线程切换类
 */

public enum BusSchedulers {
    CURRENT,//发送事件的线程
    UI,//UI线程
    COMPUTATION,//计算线程
    IO,//IO线程
    NEW_THREAD//新线程
}
