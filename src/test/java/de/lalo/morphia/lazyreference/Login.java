package de.lalo.morphia.lazyreference;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Reference;

/**
 * @author llorenzen
 * @since 10/01/2017
 */
@Entity("logins")
public class Login {

    @Id
    private ObjectId id;

    @Reference
    private ValueHolder<User> user = new ValueHolder<>();

    private Login() {
        // for morphia
    }

    Login(User user) {
        this.user.set(user);
    }

    public User getUser() {
        return user.get();
    }
}
