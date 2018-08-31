package wxl.com.rxjavademo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    private Subscription mSubscribe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initEvent();
        //rxjava();
        //rxjava1();
        //rxjava2();
        //rxjava3();
        // rxjava4();
        //rxjava5();
        //rxjava6();
        //rxjava7();
        //rxjava8();
        //rxjava9();
        //rxjava10();
        rxjava11();

    }



    private void initEvent() {
        findViewById(R.id.request).setOnClickListener(view -> {
            if (mSubscribe != null) {
                //控制订阅者一次接收一个数据
//                mSubscribe.request(1);
                //mSubscribe.request(128);

                mSubscribe.request(96);

            }
        });
    }


    private void rxjava() {
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> e) throws Exception {
                Log.e("===", "subscribe thread name= " + Thread.currentThread().getName());
                e.onNext(1);
                e.onNext(2);
                e.onNext(3);
                e.onComplete();
                e.onNext(4);
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Object>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.e("===", "onSubscribe thread name= " + Thread.currentThread().getName());
                    }

                    @Override
                    public void onNext(Object o) {
                        Log.e("===", "onNext thread name= " + Thread.currentThread().getName());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("===", "onError thread name= " + Thread.currentThread().getName());
                    }

                    @Override
                    public void onComplete() {
                        Log.e("===", "onComplete thread name= " + Thread.currentThread().getName());
                    }
                });
            /*
            总结 ： Rxjava 如果不加以指定线程都在main 线程执行， 方法：.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            指定操作线程 e.onComplete();结束完成操作 再发送消息是无法再接受到了，

            发射事件，需要满足一定的规则：
            上游可以发送无限个onNext, 下游也可以接收无限个onNext.
            当上游发送了一个onComplete后, 上游onComplete之后的事件将会继续发送, 而下游收到onComplete事件之后将不再继续接收事件.
            当上游发送了一个onError后, 上游onNext之后的事件将继续发送, 而下游收到onError事件之后将不再继续接收事件.
            上游可以不发送onComplete或onError.
            最为关键的是onComplete和onError必须唯一并且互斥, 即不能发多个onComplete, 也不能发多个onError, 也不能先发一个onComplete, 然后再发一个onError, 反之亦然

            注: 关于onComplete和onError唯一并且互斥这一点, 是需要自行在代码中进行控制, 如果你的代码逻辑中违背了这个规则,
            **并不一定会导致程序崩溃. ** 比如发送多个onComplete是可以正常运行的, 依然是收到第一个onComplete就不再接收了,
            * 但若是发送多个onError, 则收到第二个onError事件会导致程序会崩溃.

             */

    }

    private void rxjava1() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                //ObservableEmitter 可以理解为发射器
                e.onNext(1);
                e.onNext(2);
                e.onNext(3);
                e.onNext(4);
                e.onComplete();//调用此方法后， 上有会继续发送事件，下游收到onComplete事件之后将不再继续接收事件
                e.onNext(5);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    private Disposable mDisposable;
                    private int i;

                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.e("===", "onSubscribe = " + d.isDisposed());
                        mDisposable = d;
                    }

                    @Override
                    public void onNext(Integer o) {
                        Log.e("===", "onNext = " + o);
                        i++;
                        if (i == 2) {
                            //切断水管 : 上游如果有事件会继续发送事件， 下游不会再接受到事件了
                            mDisposable.dispose();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("===", "onError");
                    }

                    @Override
                    public void onComplete() {
                        Log.e("===", "onComplete");
                    }
                });

    }

    private void rxjava2() {
        //线程灵活切换
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> e) throws Exception {
                Log.e("===", "1 subscribe thread = " + Thread.currentThread().getName());
                e.onNext(1);
                e.onNext(2);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnNext(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) throws Exception {
                        Log.e("===", "1 value = " + o);
                        Log.e("===", "1 accept thread = " + Thread.currentThread().getName());
                    }
                }).observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) throws Exception {
                        Log.e("===", "2 value = " + o);
                        Log.e("===", "2 accept thread = " + Thread.currentThread().getName());

                    }
                }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) throws Exception {
                        Log.e("===", "3 value = " + o);
                        Log.e("===", "3 accept thread = " + Thread.currentThread().getName());
                    }
                });

        CompositeDisposable disposable = new CompositeDisposable();


    }


    private void rxjava3() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                e.onNext(1);
                e.onNext(2);
                e.onNext(3);
            }

        }).flatMap(new Function<Integer, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(Integer integer) throws Exception {
                ArrayList<String> list = new ArrayList<>();
                for (int i = 0; i < 3; i++) {
                    list.add("result " + integer);
                }
                return Observable.fromIterable(list).delay(1000, TimeUnit.MILLISECONDS);
            }
        }).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                Log.e("===", s);

            }
        });

        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                e.onNext(1);
                e.onNext(2);
                e.onNext(3);
            }

        }).concatMap(new Function<Integer, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(Integer integer) throws Exception {
                ArrayList<String> list = new ArrayList<>();
                for (int i = 0; i < 3; i++) {
                    list.add("result1 " + integer);
                }
                return Observable.fromIterable(list).delay(1000, TimeUnit.MILLISECONDS);
            }
        }).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                Log.e("===", s);
            }
        });
        /*
         * flatMap,concatMap 两个操作符一样的作用， flatMap不保证事件发送和接收的顺序相同，上面的结果一次发送；
         * concatMap严格按照上游发送的顺序来发送的事件.上面的结果按时间间隔发送；
         */

    }

    private void rxjava4() {
        //zip操作符
        Observable<Integer> observable1 = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                Log.d("===", "emit 1");
                emitter.onNext(1);
                Log.d("===", "emit 2");
                emitter.onNext(2);
                Log.d("===", "emit 3");
                emitter.onNext(3);
                Log.d("===", "emit 4");
                emitter.onNext(4);
                Log.d("===", "emit complete1");
                emitter.onComplete();

            }
        }).subscribeOn(Schedulers.io());

        Observable<String> observable2 = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                Log.d("===", "emit A");
                emitter.onNext("A");
                Log.d("===", "emit B");
                emitter.onNext("B");
                Log.d("===", "emit C");
                emitter.onNext("C");
                Log.d("===", "emit complete2");
                emitter.onComplete();

            }
        }).subscribeOn(Schedulers.io());

        Observable.zip(observable1, observable2, new BiFunction<Integer, String, String>() {
            @Override
            public String apply(Integer integer, String s) throws Exception {
                return integer + s;
            }
        }).subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(String s) {
                Log.d("===", "s === " + s);
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {
                Log.d("===", "onComplete3");
            }
        });

        /*
         * Zip的基本用法, 那么它在Android有什么用呢, 其实很多场景都可以用到Zip
         *   比如一个界面需要展示用户的一些信息, 而这些信息分别要从两个服务器接口中获取,
         *  而只有当两个都获取到了之后才能进行展示, 这个时候就可以用Zip了
         *
         * */

    }

    private void rxjava5() {
        Observable<Integer> observable1 = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                for (int i = 0; ; i++) {
                    emitter.onNext(i);
                }
            }
        }).subscribeOn(Schedulers.io()).sample(2, TimeUnit.SECONDS); //进行sample采样

        Observable<String> observable2 = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                emitter.onNext("A");
            }
        }).subscribeOn(Schedulers.io());

        Observable.zip(observable1, observable2, new BiFunction<Integer, String, String>() {
            @Override
            public String apply(Integer integer, String s) throws Exception {
                Log.e("===", integer.toString());
                return integer + s;
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                Log.e("===", s);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                Log.w("===", throwable);
            }
        });

        /*
         * sample 这个操作符每隔指定的时间就从上游中取出一个事件发送给下游. 这里我们让它每隔2秒取一个事件给下游
         */

    }


    private void rxjava6() {
        Flowable<Integer> flowable = Flowable.create(new FlowableOnSubscribe<Integer>() {
            @Override
            public void subscribe(FlowableEmitter<Integer> emitter) throws Exception {
                Log.e("===", "emit 1");
                emitter.onNext(1);
                Log.e("===", "emit 2");
                emitter.onNext(2);
                Log.e("===", "emit 3");
                emitter.onNext(3);
                Log.e("===", "emit complete");
                emitter.onComplete();


            }
        }, BackpressureStrategy.ERROR);

        //订阅者
        Subscriber<Integer> subscriber = new Subscriber<Integer>() {


            @Override
            public void onSubscribe(Subscription s) {

                Log.e("===", "onSubscribe");
                //Subscriber接受多少个事件
                s.request(Long.MAX_VALUE);  //注意这句代码


            }

            @Override
            public void onNext(Integer integer) {
                Log.e("===", "onNext  " + integer);

            }

            @Override
            public void onError(Throwable t) {
                Log.e("===", "onError  " + t);

            }

            @Override
            public void onComplete() {
                Log.e("===", "onComplete");

            }
        };
        //订阅
        flowable.subscribe(subscriber);
    }

    private void rxjava7() {

        Flowable.create(new FlowableOnSubscribe<Integer>() {
            @Override
            public void subscribe(FlowableEmitter<Integer> emitter) throws Exception {
//                Log.e("===", "emit 1");
//                emitter.onNext(1);
//                Log.e("===", "emit 2");
//                emitter.onNext(2);
//                Log.e("===", "emit 3");
//                emitter.onNext(3);

                //Flowable 的bufferSize容量是128个事件， 超过128会报MissingBackpressureException异常
                for (int i = 0; i < 128; i++) {
                    Log.e("===", "emit " + i);
                    emitter.onNext(i);
                }
                Log.e("===", "emit complete");
                emitter.onComplete();
            }
        }, BackpressureStrategy.ERROR)//当事件超出时报异常
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        Log.e("===", "onSubscribe");
                        //设置成员变量,由外部按钮控制事件的接收,(必须是上下游异步线程操作)
                        mSubscribe = s;
                    }

                    @Override
                    public void onNext(Integer integer) {
                        Log.e("===", "onNext  " + integer);
                    }

                    @Override
                    public void onError(Throwable t) {
                        Log.e("===", "onError  " + t);
                    }

                    @Override
                    public void onComplete() {
                        Log.e("===", "onComplete");
                    }
                });
    }


    private void rxjava8() {
        //四种背压策略
        // BackpressureStrategy.ERROR //Flowable 的默认容量大小是128 超出时会报异常。
        // BackpressureStrategy.BUFFER //Flowable 的容量大小无限大和Observable一样，同样需要注意OOM的问题。
        // BackpressureStrategy.DROP //Flowable 的容量超出128时，就直接把存不下的事件丢弃。
        // BackpressureStrategy.LATEST //Flowable 的容量超出128时，就只保留最新的事件。


        //BackpressureStrategy.BUFFER 案例
    /*    Flowable.create(new FlowableOnSubscribe<Integer>() {
            @Override
            public void subscribe(FlowableEmitter<Integer> e) throws Exception {
                //无限发送事件,下游不接收事件，会报OOM异常,
                for (int i=0;;i++){
                    Log.e("===", "emit " + i);
                    e.onNext(i);
                }
            }
        },BackpressureStrategy.BUFFER)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onSubscribe(Subscription s) {

                    }
                    @Override
                    public void onNext(Integer integer) {

                    }

                    @Override
                    public void onError(Throwable t) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });*/

        //BackpressureStrategy.DROP 案例
    /*    Flowable.create(new FlowableOnSubscribe<Integer>() {
            @Override
            public void subscribe(FlowableEmitter<Integer> e) throws Exception {
                //无限发送事件,下游不接收事件，会报OOM异常,
                for (int i=0;;i++){
                   // Log.e("===", "emit " + i);
                    e.onNext(i);
                }
            }
        },BackpressureStrategy.DROP)//Flowable会保存开始的128个事件，后面的事件舍弃
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        Log.e("===", "onSubscribe");
                        //设置成员变量,由外部按钮控制事件的接收,(必须是上下游异步线程操作)
                        mSubscribe = s;
                    }
                    @Override
                    public void onNext(Integer integer) {
                        Log.e("===", "onNext  "+integer);
                    }

                    @Override
                    public void onError(Throwable t) {
                        Log.e("===", "onError");
                    }

                    @Override
                    public void onComplete() {
                        Log.e("===", "onComplete");
                    }
                });*/


        //BackpressureStrategy.LATEST 案例
        Flowable.create(new FlowableOnSubscribe<Integer>() {
            @Override
            public void subscribe(FlowableEmitter<Integer> e) throws Exception {
                //无限发送事件,下游不接收事件，会报OOM异常,
                for (int i = 0;i<1000 ; i++) {
                    // Log.e("===", "emit " + i);
                    e.onNext(i);
                }
            }
        }, BackpressureStrategy.LATEST)//Flowable会保存前面的128个事件；总是能获取到最后最新的事件,这里的最新事件是999
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        Log.e("===", "onSubscribe");
                        //设置成员变量,由外部按钮控制事件的接收,(必须是上下游异步线程操作)
                        mSubscribe = s;
                        mSubscribe.request(128);
                    }

                    @Override
                    public void onNext(Integer integer) {
                        Log.e("===", "onNext  " + integer);
                    }

                    @Override
                    public void onError(Throwable t) {
                        Log.e("===", "onError");
                    }

                    @Override
                    public void onComplete() {
                        Log.e("===", "onComplete");
                    }
                });

    }


    private void rxjava9() {
        //有些FLowable并不是我自己创建的, 该怎么办呢? 比如RxJava中的interval操作符，不是我们自己创建的, 但是RxJava给我们提供了其他的方法：
        //onBackpressureBuffer()
       // onBackpressureDrop()
       // onBackpressureLatest()
        Flowable.interval(1,TimeUnit.MICROSECONDS)
                .onBackpressureDrop()  //加上背压策略， 这里不加会报异常
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Long>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        Log.e("===", "onSubscribe");
                        mSubscribe = s;
                        s.request(Long.MAX_VALUE);
                    }

                    @Override
                    public void onNext(Long aLong) {
                        Log.e("===", "onNext: " + aLong);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        Log.e("===", "onError: "+t);
                    }

                    @Override
                    public void onComplete() {
                        Log.e("===", "onComplete");
                    }
                });


    }

    private void rxjava10(){
        Flowable.create(new FlowableOnSubscribe<Integer>() {
            @Override
            public void subscribe(FlowableEmitter<Integer> e) throws Exception {
                Log.e("===","下游请求的处理事件数量： "+e.requested());
                Log.e("===","subscribe "+Thread.currentThread().getName());
//                e.onNext(1);
//                e.onNext(2);
//                e.onNext(3);
                //e.onComplete();

                boolean flag;
                for (int i = 0; ; i++) {
                    flag = false;
                    while (e.requested() == 0) {
                        if (!flag) {
                            Log.e("===", "Oh no! I can't emit value!");
                            flag = true;
                        }
                    }
                    e.onNext(i);
                    Log.e("===", "emit " + i + " , requested = " + e.requested());
                }


            }
        },BackpressureStrategy.ERROR)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Integer>() {
            @Override
            public void onSubscribe(Subscription s) {
                Log.e("===","onSubscribe "+Thread.currentThread().getName());
                mSubscribe=s;
                //上下游同步时
                //s.request(10);//打印 下游请求的处理事件数量： 10

                //多次请求
                //s.request(20);
                //s.request(50);
                //打印一次   下游请求的处理事件数量： 70
                //s.request(2);

                //上下游异步时  上有默认的缓存事件容量大小是 128
                //s.request(1000);//打印   下游请求的处理事件数量： 128
                //s.request(2);////打印   下游请求的处理事件数量： 128


            }

            @Override
            public void onNext(Integer s) {
                Log.e("===","onNext "+Thread.currentThread().getName());
            }

            @Override
            public void onError(Throwable t) {
                Log.e("===","onError "+t);
                //上下游同步时（上下游在一个线程中），当上游发送的事件 大于 下游接收的事件报异常： MissingBackpressureException
                //异步不会报异常；
            }

            @Override
            public void onComplete() {
                Log.e("===","onComplete "+Thread.currentThread().getName());
            }
        });

    }

    private void rxjava11(){
        Maybe.just(1)
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Log.e("===","--- "+integer);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e("===","--- "+throwable);
                    }
                });

    }

}
