package com.lh.rxbuslibrary;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

/**
 * Created by home on 2017/1/9.
 * RxBus主类
 */

public class RxBus {
    private static volatile RxBus INSTANCE;

    private Subject<Object> mSubject;
    private Map<String, CompositeDisposable> mDisposableMap;

    private RxBus() {
        mSubject = PublishSubject.create().toSerialized();
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

    public void post(Object object) {
        mSubject.onNext(object);
    }

    public void register(final Object object) {
        Observable.just(object)
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
                .map(new Function<Method, Disposable>() {
                    @Override
                    public Disposable apply(final Method method) throws Exception {
                        Subscribe subscribe = method.getAnnotation(Subscribe.class);
                        Class<?> paramType = method.getParameterTypes()[0];
                        return Observable.just(subscribe.scheduler())
                                .flatMap(new Function<BusSchedulers, ObservableSource<?>>() {
                                    @Override
                                    public ObservableSource<?> apply(BusSchedulers scheduler) throws Exception {
                                        switch (scheduler){
                                            case CURRENT:
                                                return mSubject;
                                            case UI:
                                                return mSubject.observeOn(AndroidSchedulers.mainThread());
                                            case COMPUTATION:
                                                return mSubject.observeOn(Schedulers.computation());
                                            case IO:
                                                return mSubject.observeOn(Schedulers.io());
                                            case NEW_THREAD:
                                                return mSubject.observeOn(Schedulers.newThread());
                                        }
                                        return mSubject;
                                    }
                                })
                                .ofType(paramType)
                                .subscribe(new Consumer<Object>() {
                                    @Override
                                    public void accept(Object o) throws Exception {
                                        method.setAccessible(true);
                                        method.invoke(object, o);
                                    }
                                });
                    }
                })
                .subscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        String key = object.getClass().getName();
                        CompositeDisposable disposables;
                        if (mDisposableMap.containsKey(key)) {
                            disposables = mDisposableMap.get(key);
                        } else {
                            disposables = new CompositeDisposable();
                            mDisposableMap.put(key, disposables);
                        }
                        disposables.add(disposable);
                    }
                });
    }

    public void unRegister(Object object) {
        Observable.just(object).flatMap(new Function<Object, ObservableSource<CompositeDisposable>>() {
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
