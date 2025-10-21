package dataaccess;

import dataaccess.MemoryDataAccess;
import datamodel.UserData;
import org.junit.jupiter.api.*;

public class DataAccessTest {

    @Test
    void clear() {
        var user = new UserData("Joe", "john", "someone");
        DataAccess da = new MemoryDataAccess();
        assertNull(da.getUser(user.username()));
        da.createUser(user);
        assertNotNull(da.getUser(user.username()));

    }

    @Test
    void createUser() {

    }

    @Test
    void getUser() {

    }
}
