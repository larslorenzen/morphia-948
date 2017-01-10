package de.lalo.morphia.lazyreference;

import com.mongodb.DBObject;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.DatastoreImpl;
import org.mongodb.morphia.mapping.Mapper;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.QueryImpl;

/**
 * @author Lars Lorenzen
 * @since 13.05.11
 */
public class MorphiaUtil {

    public static DBObject getQueryDBObject(Query query) {
        if (query instanceof QueryImpl) {
            QueryImpl impl = (QueryImpl) query;
            return impl.getQueryObject();
        }
        throw new IllegalStateException("Query " + query + " cannot be cast to " + QueryImpl.class);
    }

    public static void setQueryDBObject(Query query, DBObject dbObject) {
        if (query instanceof QueryImpl) {
            QueryImpl impl = (QueryImpl) query;
            impl.setQueryObject(dbObject);
        } else {
            for (String key : dbObject.keySet()) {
                query.field(key).equal(dbObject.get(key));
            }
        }
    }

    // there are some places in the code where i need the Mapper
    public static Mapper getMapper(Datastore datastore) {
        if (datastore instanceof DatastoreImpl) {
            DatastoreImpl advancedDatastore = (DatastoreImpl) datastore;
            return advancedDatastore.getMapper();
        }

        throw new IllegalStateException("Datastore " + datastore + " cannot be cast to " + DatastoreImpl.class);
    }

}
