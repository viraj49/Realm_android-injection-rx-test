package tank.viraj.realm.dao;

import io.realm.RealmConfiguration;

/**
 * Created by Viraj Tank, 18-06-2016.
 */
public abstract class AbstractDao {
    /* RealmConfiguration needed to create Realm instance,
     * we are injecting realmConfiguration with Dagger2 */
    protected RealmConfiguration realmConfiguration;

    public AbstractDao(RealmConfiguration realmConfiguration) {
        this.realmConfiguration = realmConfiguration;
    }

    /* Clearing right realm with right DAO is important,
     * test this method to avoid any misplaced clear */
    abstract public void clearDatabase();
}