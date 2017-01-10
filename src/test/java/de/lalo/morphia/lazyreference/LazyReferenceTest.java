package de.lalo.morphia.lazyreference;

import org.junit.Test;
import org.mongodb.morphia.AdvancedDatastore;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author llorenzen
 * @since 10/01/2017
 */
public class LazyReferenceTest {

    @Test
    public void testObjectGraphForValueHolder() {
        AdvancedDatastore datastore = MongoConnection.getInstance().getDatastore();
        User user = new User();
        datastore.save(user);
        datastore.save(user.getAccount());
        // to save the reference to the account
        datastore.save(user);


        User fetchedUser = datastore.get(user);
        assertThat(fetchedUser.getAccount().getUser(), is(fetchedUser));
    }

    @Test
    public void testObjectGraphForList() {
        AdvancedDatastore datastore = MongoConnection.getInstance().getDatastore();
        User user = new User();
        datastore.save(user);
        datastore.save(user.createLogin());
        // to save the reference to the logins
        datastore.save(user);

        User fetchedUser = datastore.get(user);
        assertThat(fetchedUser.getLogins(), hasSize(1));
        assertThat(fetchedUser.getLogins().get(0).getUser(), is(fetchedUser));
    }
}
