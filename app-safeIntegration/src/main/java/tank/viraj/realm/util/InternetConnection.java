package tank.viraj.realm.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import rx.Observable;

/**
 * Created by Viraj Tank, 18-06-2016.
 */
public class InternetConnection {
    public Observable<Boolean> isInternetOn(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return Observable.just(netInfo != null && netInfo.isConnected());
    }
}