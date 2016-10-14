package tank.viraj.realm;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import tank.viraj.realm.injections.ApplicationComponent;
import tank.viraj.realm.injections.ApplicationModule;
import tank.viraj.realm.injections.DaggerApplicationComponent;

/**
 * Created by Viraj Tank, 18-06-2016.
 */
public class MainApplication extends Application {

    private ApplicationComponent applicationComponent;

    public MainApplication() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        /* Leak canary */
        LeakCanary.install(this);

        initializeRealm();

        applicationComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();
    }

    private void initializeRealm() {
        /* securityKey */
        char[] chars = "16CharacterLongPasswordKey4Realm".toCharArray();
        byte[] key = new byte[chars.length * 2];
        for (int i = 0; i < chars.length; i++) {
            key[i * 2] = (byte) (chars[i] >> 8);
            key[i * 2 + 1] = (byte) chars[i];
        }

        /* realmConfiguration */
        Realm.init(this);
        Realm.setDefaultConfiguration(new RealmConfiguration.Builder()
                .encryptionKey(key)
                .deleteRealmIfMigrationNeeded()
                .build());
    }

    public ApplicationComponent getApplicationComponent() {
        return applicationComponent;
    }
}