package com.vibal.utilities;

import org.junit.Test;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class UnitTest {
    @Test
    public void testHotCompletable() throws InterruptedException {
        Observable completable = Completable.create(emitter -> {
            System.out.println("Starting...");
            Thread.sleep(2000);
            System.out.println("First done");
            emitter.onComplete();
        }).doOnComplete(() -> System.out.println("On Complete first"))
                .andThen(Completable.create(emitter -> {
                    System.out.println("Starting second...");
                    Thread.sleep(2000);
                    System.out.println("Second done");
                    emitter.onComplete();
                }).doOnComplete(() -> System.out.println("On Complete second")))
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.trampoline())
                .toObservable()
                .publish()
                .autoConnect();
//                .subscribeOn(Schedulers.newThread())
//                .observeOn(Schedulers.trampoline())
//                .subscribe();

//        completable.doOnSubscribe(disposable -> {
//            completable.connect();
//            System.out.println("Connect");
//        });
//        completable.connect();

        // Subscribe
        Disposable disposable = completable.observeOn(Schedulers.trampoline())
                .doOnSubscribe(disposable1 -> System.out.println("Connect"))
                .subscribe();

        Thread.sleep(3000);
        System.out.println("Disposing...");
        disposable.dispose();
        Thread.sleep(1000);
        System.out.println("Disposed: " + disposable.isDisposed());
        Thread.sleep(2000);

        // Test
//        TestObserver disposable = completable.observeOn(Schedulers.trampoline())
//                .doOnSubscribe(disposable1 -> System.out.println("Connect"))
//                .test();
//
//        Thread.sleep(3000);
//        System.out.println("Disposing...");
//        disposable.dispose();
//        Thread.sleep(1000);
//        disposable.assertNotComplete();
//        Thread.sleep(2000);
    }

    @Test
    public void testHotSingle() throws InterruptedException {
        Observable<Integer> observable = Single.create((SingleOnSubscribe<Integer>) emitter -> {
            System.out.println("Starting...");
            Thread.sleep(2000);
            System.out.println("First done");
            emitter.onSuccess(1);
        }).map(integer -> {
            System.out.println("Starting second... " + integer);
            Thread.sleep(2000);
            System.out.println("Second done");
            return integer + 1;
        })
                .subscribeOn(Schedulers.newThread())
                .toObservable()
                .publish()
                .autoConnect();

        // Subscribe+
//        Disposable disposable = single.observeOn(Schedulers.trampoline())
//                .doOnSubscribe(disposable1 -> System.out.println("Connect"))
//                .subscribe(System.out::println);
//
//        Thread.sleep(3000);
//        System.out.println("Disposing...");
//        disposable.dispose();
//        Thread.sleep(1000);
//        System.out.println("Disposed: " + disposable.isDisposed());
//        Thread.sleep(2000);

        // Test
        TestObserver disposable = observable.observeOn(Schedulers.trampoline())
                .doOnSubscribe(disposable1 -> System.out.println("Connect"))
                .test();
        TestObserver disposable2 = observable
                .doOnSubscribe(disposable1 -> System.out.println("Connect"))
                .test();

        Thread.sleep(3000);
        System.out.println("Disposing...");
        disposable.dispose();
        Thread.sleep(1000);
        disposable.assertNotComplete();
        Thread.sleep(2000);
        disposable2.assertComplete()
                .assertValue(2);
    }
}