package cn.syf.rxjava2demo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.observers.SafeObserver;

public class TestActivity extends AppCompatActivity {
    private static final String TAG = "rxjava";
    private TextView mTextView;
    StringBuilder mOperateStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        mTextView = findViewById(R.id.text);
//        Flowable.just("Hello world").subscribe(mTextView::setText).dispose();
//        testCreateOption();
//        testMapOption();
        testZipOption();
    }

    private void testZipOption() {
        List<String> stringList = new ArrayList<>();
        stringList.add("A");
        stringList.add("B");
        stringList.add("C");
        stringList.add("D");
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        Observable<String> observable1 = Observable.just("A","B","C","D");
        Observable<Integer> observable2 = Observable.just(1,2,3,4,5);
        Observable.zip(observable1, observable2, new BiFunction<String, Integer, String>() {
            @Override
            public String apply(String s, Integer integer) throws Exception {
                return s+integer;
            }
        }).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                Log.i(TAG, "accept: "+s);
                mTextView.setText(s);
            }
        }).dispose();
    }

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
