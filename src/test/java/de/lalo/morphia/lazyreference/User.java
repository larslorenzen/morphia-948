package de.lalo.morphia.lazyreference;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Reference;

import java.util.ArrayList;
import java.util.List;

/**
 * @author llorenzen
 * @since 10/01/2017
 */
@Entity("users")
public class User {

    @Id
    private ObjectId id;

    @Reference
    private ValueHolder<Account> account = new ValueHolder<>();

    @Reference(lazy = true)
    private List<Login> logins = new ArrayList<>();

    public Account getAccount() {
        if (account.isEmpty()) {
            account.set(new Account(this));
        }
        return account.get();
    }

    Login createLogin() {
        Login login = new Login(this);
        logins.add(login);
        return login;
    }

    public List<Login> getLogins() {
        return logins;
    }
}
