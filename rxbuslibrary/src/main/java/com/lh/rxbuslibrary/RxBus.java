package com.lh.rxbuslibrary;

import android.support.annotation.NonNull;

import com.lh.rxbuslibrary.annotation.Subscribe;
import com.lh.rxbuslibrary.event.DefaultEvent;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

/**
 * Created by home on 2017/1/9.
 * RxBus主类
 */

public class RxBus {
    private static volatile RxBus INSTANCE;

    private Subject<Object> mBusSubject;
    private Map<String, CompositeDisposable> mDisposableMap;

    private RxBus() {
        mBusSubject = PublishSubject.create().toSerialized();
        mDisposableMap = new HashMap<>();
    }

    public static RxBus getInstance() {
        if (INSTANCE == null) {
            synchronized (RxBus.class) {
                if (INSTANCE == null) {
                    INSTANCE = new RxBus();
                }
            }

        }
        return INSTANCE;
    }

    public void post(int tag) {
        mBusSubject.onNext(new DefaultEvent(tag, null));
    }

    public void post(int tag, @NonNull Object object) {
        mBusSubject.onNext(new DefaultEvent(tag, object));
    }

    public void post(@NonNull Object object) {
        mBusSubject.onNext(object);
    }

    public void register(@NonNull final Object subscriber) {
        Observable.just(subscriber)
                .filter(new Predicate<Object>() {
                    @Override
                    public boolean test(Object o) throws Exception {
                        return !mDisposableMap.containsKey(subscriber.getClass().getName());
                    }
                })
                .flatMap(new Function<Object, ObservableSource<Method>>() {
                    @Override
                    public ObservableSource<Method> apply(Object o) throws Exception {
                        return Observable.fromArray(o.getClass().getDeclaredMethods());
                    }
                })
                .filter(new Predicate<Method>() {
                    @Override
                    public boolean test(Method method) throws Exception {
                        return method.isAnnotationPresent(Subscribe.class);
                    }
                })
                .subscribe(new Consumer<Method>() {
                    @Override
                    public void accept(Method method) throws Exception {
                        subscribe(method, subscriber);
                    }
                });
    }

    private void subscribe(final Method method, final Object subscriber) {
        final Subscribe subscribe = method.getAnnotation(Subscribe.class);
        Disposable disposable = mBusSubject.filter(new Predicate<Object>() {
            @Override
            public boolean test(Object object) throws Exception {
                Class[] paramTypes = method.getParameterTypes();
                if (object instanceof DefaultEvent) {
                    DefaultEvent event = (DefaultEvent) object;
                    if (event.getData() != null && paramTypes.length > 0) {
                        return event.getTag() == subscribe.tag() && paramTypes[0].isInstance(event.getData());
                    } else {
                        return event.getData() == null && paramTypes.length == 0 && event.getTag() == subscribe.tag();
                    }
                } else {
                    return paramTypes.length > 0 && subscribe.tag() == 0 && paramTypes[0].isInstance(object);
                }
            }
        }).map(new Function<Object, Object>() {
            @Override
            public Object apply(Object object) throws Exception {
                if (object instanceof DefaultEvent) {
                    DefaultEvent event = (DefaultEvent) object;
                    if (event.getData() == null) {
                        return new Object();
                    } else {
                        return event.getData();
                    }
                } else {
                    return object;
                }
            }
        }).compose(subscribe.scheduler().toTransformer()).subscribe(new Consumer<Object>() {
            @Override
            public void accept(Object object) throws Exception {
                method.setAccessible(true);
                if (method.getParameterTypes().length > 0) {
                    method.invoke(subscriber, object);
                } else {
                    method.invoke(subscriber);
                }
            }
        });
        CompositeDisposable compositeDisposable;
        String key = subscriber.getClass().getName();
        if (mDisposableMap.containsKey(key)) {
            compositeDisposable = mDisposableMap.get(key);
        } else {
            compositeDisposable = new CompositeDisposable();
            mDisposableMap.put(key, compositeDisposable);
        }
        compositeDisposable.add(disposable);
    }

    public void unRegister(@NonNull Object subscriber) {
        Observable.just(subscriber).flatMap(new Function<Object, ObservableSource<CompositeDisposable>>() {
            @Override
            public ObservableSource<CompositeDisposable> apply(Object o) throws Exception {
                String key = o.getClass().getName();
                if (mDisposableMap.containsKey(key)) {
                    CompositeDisposable disposables = mDisposableMap.get(key);
                    mDisposableMap.remove(key);
                    return Observable.just(disposables);
                } else {
                    return Observable.empty();
                }
            }
        }).subscribe(new Consumer<CompositeDisposable>() {
            @Override
            public void accept(CompositeDisposable disposable) throws Exception {
                if (!disposable.isDisposed()) {
                    disposable.dispose();
                }
            }
        });
    }
}
