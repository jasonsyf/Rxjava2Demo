package cn.syf.rxjava2demo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.util.TimeUtils;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.observers.SafeObserver;
import io.reactivex.schedulers.Schedulers;

public class TestActivity extends AppCompatActivity {
    private static final String TAG = "rxjava";
    private TextView mTextView;
    StringBuilder mOperateStr = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        mTextView = findViewById(R.id.text);
//        testCreateOption();
//        testMapOption();
//        testZipOption();
//        testConcatOption();
//        testFlatmapOption();
//        testConcatmapOption();
//        testDistintOption();
//        testFilterOption();
//        testBufferOption();
//        testTimerOption();
        testintervalOption();
    }

    /**
     * interval 操作符用于间隔时间执行某个操作，其接受三个参数，分别是第一次发送延迟，间隔时间，时间单位。
     */
    private void testintervalOption() {
        mOperateStr.append("interval start:" + DateUtil.getNowTime() + "\n");
        Log.e(TAG, "interval start : " + DateUtil.getNowTime() + "\n");
        Observable.interval(2, 5, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        mOperateStr.append("interval time:" + aLong + " at " + DateUtil.getNowTime() + "\n");
                        Log.e(TAG, "interval time:" + aLong + " at " + DateUtil.getNowTime() + "\n");
                        mTextView.setText(mOperateStr);
                    }
                });
    }

    /**
     * 一个定时任务，延迟多长时间后进行任务
     */
    private void testTimerOption() {
        mOperateStr.append("timer start:" + DateUtil.getNowTime() + "\n");
        Log.e(TAG, "timer start : " + DateUtil.getNowTime() + "\n");
        Observable.timer(2, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        mOperateStr.append("timer :" + aLong + " at " + DateUtil.getNowTime() + "\n");
                        Log.e(TAG, "timer :" + aLong + " at " + DateUtil.getNowTime() + "\n");
                        mTextView.setText(mOperateStr);
                    }
                });

    }


    /**
     * buffer 操作符接受两个参数，buffer(count,skip)，作用是将 Observable 中的数据按 skip (步长) 分成
     * 最大不超过 count 的 buffer ，然后生成一个  Observable
     */
    private void testBufferOption() {
        Observable.just(1, 1, 1, 2, 2, 3, 5, 9, 10, 10)
                .buffer(3, 3)
                .subscribe(new Consumer<List<Integer>>() {
                    @Override
                    public void accept(List<Integer> integers) throws Exception {
                        mOperateStr.append("Buffer : " + integers.toString() + "\n");
                        mTextView.setText(mOperateStr.toString());
                    }
                });
    }

    /**
     * Filter:过滤器。可以接受一个参数，让其过滤掉不符合我们条件的值
     */
    private void testFilterOption() {
        Observable.just(1, 1, 1, 2, 2, 3, 5, 9, 10, 10)
                .filter(new Predicate<Integer>() {
                    @Override
                    public boolean test(Integer integer) throws Exception {
                        return integer > 5;
                    }
                })
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Log.i(TAG, "filter: " + integer);
                        mOperateStr.append("filter : " + integer + "\n");
                        mTextView.setText(mOperateStr.toString());
                    }
                });
    }

    /**
     * 去除重复数据
     */
    private void testDistintOption() {
        Observable.just(1, 1, 1, 2, 2, 3, 5, 9, 10, 10)
                .distinct()
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Log.i(TAG, "distint: " + integer);
                        mOperateStr.append("distint : " + integer + "\n");
                        mTextView.setText(mOperateStr.toString());
                    }
                });
    }

    /**
     * concatMap 与 FlatMap 的唯一区别就是 concatMap 保证了顺序
     */
    private void testConcatmapOption() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                emitter.onNext(1);
                emitter.onNext(2);
                emitter.onNext(3);

            }
        }).concatMap(new Function<Integer, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(Integer integer) throws Exception {
                List<String> list = new ArrayList<>();
                for (int i = 0; i < 3; i++) {
                    list.add("我是一个值" + integer);
                }
                int delayTime = (int) (1 + Math.random() * 10);
                return Observable.fromIterable(list).delay(delayTime, TimeUnit.MILLISECONDS);
            }
        }).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                mOperateStr.append("flatmap : " + s + "\n");
                mTextView.setText(mOperateStr.toString());
            }
        });
    }

    /**
     * flatMap:可以把一个发射器 Observable 通过某种方法转换为多个 Observables，然后再把这些分散的 Observables
     * 装进一个单一的发射器 Observable。但有个需要注意的是，flatMap 并不能保证事件的顺序，如果需要保证，
     * 需要用到我们下面要讲的 ConcatMap。
     */
    private void testFlatmapOption() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                emitter.onNext(1);
                emitter.onNext(2);
                emitter.onNext(3);

            }
        }).flatMap(new Function<Integer, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(Integer integer) throws Exception {
                List<String> list = new ArrayList<>();
                for (int i = 0; i < 3; i++) {
                    list.add("我是一个值" + integer);
                }
                int delayTime = (int) (1 + Math.random() * 10);
                return Observable.fromIterable(list).delay(delayTime, TimeUnit.MILLISECONDS);
            }
        }).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                mOperateStr.append("flatmap : " + s + "\n");
                mTextView.setText(mOperateStr.toString());
            }
        });
    }

    /**
     * Concat:对于单一的把两个发射器连接成一个发射器
     */
    private void testConcatOption() {
        Observable.concat(Observable.just("A", "b", "c"), Observable.just("1", "2", "3"))
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        mOperateStr.append("concat : " + s + "\n");
                        mTextView.setText(mOperateStr.toString());
                    }
                });
    }

    /**
     * zip 组合事件的过程就是分别从发射器 A 和发射器 B 各取出一个事件来组合，并且一个事件只能被使用一次，
     * 组合的顺序是严格按照事件发送的顺序来进行的,最终接收器收到的事件数量是和发送器发送事件最少的那个发送
     * 器的发送事件数目相同
     */
    private void testZipOption() {
        Observable<String> observable1 = Observable.just("A", "B", "C", "D");
        Observable<Integer> observable2 = Observable.just(1, 2, 3, 4, 5);
        Observable.zip(observable1, observable2, new BiFunction<String, Integer, String>() {
            @Override
            public String apply(String s, Integer integer) throws Exception {
                return s + integer;
            }
        }).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                Log.i(TAG, "accept: " + s);
                mTextView.setText(s);
            }
        }).dispose();
    }

    /**
     * map 是对发射时间发送的每一个事件应用一个函数，是的每一个事件都按照指定的函数去变化
     */
    private void testMapOption() {
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                StringBuilder s = new StringBuilder("我是create的value");
                for (int i = 0; i < 5; i++) {
                    s.append(i);
                    emitter.onNext(s.toString() + i);
                }
            }
        })
                .map(new Function<String, String>() {
                    @Override
                    public String apply(String s) throws Exception {
                        return s + "(经过map转换后)" + "\n";
                    }
                })
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        mTextView.setText(s);
                    }
                }).dispose();
    }

    /**
     * create 操作符应该是最常见的操作符了，主要用于产生一个 Obserable 被观察者对象
     */
    private void testCreateOption() {
        Observable.create((ObservableOnSubscribe<Integer>) emitter -> {
            mOperateStr.append("Observable emit 1" + "\n");
            emitter.onNext(1);
            mOperateStr.append("Observable emit 2" + "\n");
            Log.e(TAG, "Observable emit 2" + "\n");
            emitter.onNext(2);
            mOperateStr.append("Observable emit 3" + "\n");
            Log.e(TAG, "Observable emit 3" + "\n");
            emitter.onNext(3);
            emitter.onComplete();
            mOperateStr.append("Observable emit 4" + "\n");
            Log.e(TAG, "Observable emit 4" + "\n");
            emitter.onNext(4);
        }).subscribe(new Observer<Integer>() {
            private int i;
            private Disposable mDisposable;

            @Override
            public void onSubscribe(Disposable d) {
                mOperateStr.append("onSubscribe : " + d.isDisposed() + "\n");
                Log.e(TAG, "onSubscribe : " + d.isDisposed() + "\n");
                mDisposable = d;

            }

            @Override
            public void onNext(Integer o) {
                mOperateStr.append("onNext : value : " + o + "\n");
                Log.e(TAG, "onNext : value : " + o + "\n");
                i++;
                if (i == 2) {
                    // 在RxJava 2.x 中，新增的Disposable可以做到切断的操作，让Observer观察者不再接收上游事件
                    mDisposable.dispose();
                    mOperateStr.append("onNext : isDisposable : " + mDisposable.isDisposed() + "\n");
                    Log.e(TAG, "onNext : isDisposable : " + mDisposable.isDisposed() + "\n");
                }


            }

            @Override
            public void onError(Throwable e) {
                mOperateStr.append("onError : value : " + e.getMessage() + "\n");
                Log.e(TAG, "onError : value : " + e.getMessage() + "\n");


            }

            @Override
            public void onComplete() {
                mOperateStr.append("onComplete" + "\n");
                Log.e(TAG, "onComplete" + "\n");
            }
        });
    }
}
