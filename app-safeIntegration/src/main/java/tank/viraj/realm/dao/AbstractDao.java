package tank.viraj.realm.dao;

/**
 * Created by Viraj Tank, 18-06-2016.
 */
abstract class AbstractDao {
    /* Clearing right realm with right DAO is important,
     * test this method to avoid any misplaced clear */
    abstract public void clearDatabase();
}