package com.lh.rxbuslibrary.event;

/**
 * Created by home on 2017/1/9.
 * 带Tag的事件
 */

public class DefaultEvent {
    private int tag;
    private Object data;

    public DefaultEvent(int tag, Object data) {
        this.tag = tag;
        this.data = data;
    }

    public int getTag() {
        return tag;
    }

    public Object getData() {
        return data;
    }
}
