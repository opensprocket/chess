package dataaccess;

import datamodel.User;

public interface DataAccess {
    void saveUser(User user);
    void getUser(String user);
}
