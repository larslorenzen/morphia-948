package de.lalo.morphia.lazyreference;

import com.mongodb.DBObject;
import com.mongodb.DBRef;
import org.mongodb.morphia.AdvancedDatastore;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.mapping.CustomMapper;
import org.mongodb.morphia.mapping.MappedField;
import org.mongodb.morphia.mapping.Mapper;
import org.mongodb.morphia.mapping.cache.EntityCache;
import org.mongodb.morphia.mapping.lazy.proxy.ProxiedEntityReferenceList;
import org.mongodb.morphia.query.Query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * User: llorenzen
 * Date: 19.07.11
 * Time: 09:52
 */
public abstract class IndirectEntityReferenceList implements ProxiedEntityReferenceList {

    private List<Key<?>> keys;
    private Class referenceObjectClass;

    protected CustomMapper mapper;
    protected MappedField mf;
    // we need to keep a reference to the original cache to keep the object graph consistent
    protected EntityCache cache;
    protected Mapper mapr;

    protected IndirectEntityReferenceList(CustomMapper mapper, DBObject dbObject, MappedField mf, Object entity, EntityCache cache, Mapper mapr) {
        this.mapper = mapper;
        this.mf = mf;
        this.cache = cache;
        this.mapr = mapr;

        referenceObjectClass = mf.getSubClass();

        Object dbVal = mf.getDbObjectValue(dbObject);
        keys = new ArrayList<Key<?>>();
        if (dbVal != null) {
            if (dbVal instanceof List) {
                List<DBRef> refList = (List<DBRef>) dbVal;
                if (refList.size() > 15) {
                    keys = new ArrayList<Key<?>>(refList.size());
                }
                for (DBRef dbRef : refList) {
                    keys.add(mapr.<Object>refToKey(dbRef));
                }
            } else {
                DBRef dbRef = (DBRef) dbVal;
                keys.add(mapr.refToKey(dbRef));
            }
        }
    }

    @Override
    public void __add(Key<?> key) {
        keys.add(key);
    }

    @Override
    public void __addAll(Collection<? extends Key<?>> keys) {
        this.keys.addAll(keys);
    }

    @Override
    public List<Key<?>> __getKeysAsList() {
        return keys;
    }

    @Override
    public Class __getReferenceObjClass() {
        return referenceObjectClass;
    }

    protected void postFetch() {
        keys = null;
        referenceObjectClass = null;
        mapper = null;
        mf = null;
        cache = null;
        mapr = null;
    }

    protected <T extends Collection> T fetchReferences() {
        List<Key<?>> keyList = __getKeysAsList();
        T retrievedEntities = createCollection();
        if (keyList == null || keyList.isEmpty()) {
            return retrievedEntities;
        }
        List<Object> ids = new ArrayList<>(keyList.size());
        for (Key<?> key : keyList) {
            ids.add(key.getId());
        }
        AdvancedDatastore datastore = MongoConnection.getInstance().getDatastore();
        Query query = datastore.find(__getReferenceObjClass());
        query.field(Mapper.ID_KEY).in(ids);
        if (query instanceof QueryWithCacheGetter) {
            QueryWithCacheGetter myQuery = (QueryWithCacheGetter) query;
            ValueHolder.mergeCaches(this.cache, myQuery.getCache());
        }
        if (ids.isEmpty()) {
            return retrievedEntities;
        }
        for (Object entity : query) {
            retrievedEntities.add(entity);
        }
        return retrievedEntities;
    }

    protected abstract <T extends Collection> T createCollection();
}
