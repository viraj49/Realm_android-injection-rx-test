package tank.viraj.realm.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

import java.io.IOException;

import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by Viraj Tank, 18-06-2016.
 */
public class InternetConnection {
    private Context context;
    private BroadcastReceiver broadcastReceiver;
    private PublishSubject<Boolean> internetStatusHotObservable;

    public InternetConnection(Context context) {
        this.context = context;
    }

    public Observable<Boolean> isInternetOnObservable() {
        return Observable.just(isInternetOn());
    }

    private boolean isInternetOn() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return false;
    }

    public Observable<Boolean> getInternetStatusHotObservable() {
        internetStatusHotObservable = PublishSubject.create();
        return internetStatusHotObservable.asObservable();
    }

    /* Register for Internet connection change broadcast receiver */
    public void registerBroadCastReceiver() {
        final IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                internetStatusHotObservable.onNext(isInternetOn());
            }
        };

        context.registerReceiver(broadcastReceiver, filter);
    }

    /* unRegister for Internet connection change broadcast receiver */
    public void unRegisterBroadCastReceiver() {
        context.unregisterReceiver(broadcastReceiver);
    }
}