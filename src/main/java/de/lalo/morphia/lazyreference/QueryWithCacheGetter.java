package de.lalo.morphia.lazyreference;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.ReadPreference;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.mapping.cache.EntityCache;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.QueryFactory;
import org.mongodb.morphia.query.QueryImpl;

import java.lang.reflect.Field;

/**
 * ATTENTION: extends QueryImpl
 * <p/>
 * User: llorenzen
 * Date: 25.01.13
 * Time: 17:20
 */
class QueryWithCacheGetter<T> extends QueryImpl<T> {

    // the cache field was protected in earlier versions but now reflection-magic is needed
    private static final Field cacheField;

    static {
        try {
            cacheField = QueryImpl.class.getDeclaredField("cache");
            cacheField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private QueryWithCacheGetter(Class<T> clazz, DBCollection coll, Datastore ds) {
        super(clazz, coll, ds);
    }

    public EntityCache getCache() {
        try {
            return (EntityCache) cacheField.get(this);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    static class Factory implements QueryFactory {

        @Override
        public <T> Query<T> createQuery(Datastore datastore, DBCollection collection, Class<T> type) {
            return new QueryWithCacheGetter<T>(type, collection, datastore);
        }

        @Override
        public <T> Query<T> createQuery(Datastore datastore, DBCollection collection, Class<T> type, DBObject query) {
            QueryWithCacheGetter<T> queryWithCacheGetter = new QueryWithCacheGetter<>(type, collection, datastore);
            if (query != null) {
                queryWithCacheGetter.setQueryObject(query);
            }
            return queryWithCacheGetter;
        }

        @Override
        public <T> Query<T> createQuery(Datastore datastore) {
            return null;
        }
    }
}
