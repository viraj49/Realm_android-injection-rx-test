package tank.viraj.realm;

import android.app.Application;

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

        applicationComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();
    }

    public ApplicationComponent getApplicationComponent() {
        return applicationComponent;
    }
}