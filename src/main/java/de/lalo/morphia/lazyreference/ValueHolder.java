package de.lalo.morphia.lazyreference;

import com.mongodb.DBRef;
import org.bson.types.ObjectId;
import org.mongodb.morphia.AdvancedDatastore;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Transient;
import org.mongodb.morphia.mapping.MappedField;
import org.mongodb.morphia.mapping.Mapper;
import org.mongodb.morphia.mapping.MappingException;
import org.mongodb.morphia.mapping.cache.EntityCache;
import org.mongodb.morphia.query.Query;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * User: llorenzen
 * Date: 28.10.11
 * Time: 13:05
 */
public class ValueHolder<T> {

    private static final Logger logger = Logger.getLogger(ValueHolder.class.getName());

    @Id
    private long id;

    @Transient
    private Class referenceObjClass;

    @Transient
    private DBRef ref;

    @Transient
    private T object;

    @Transient
    private Mapper mapper;

    @Transient
    private MappedField mappedField;

    @Transient
    private Object rootObject;

    // We need to hold a reference to the cache to keep the object graph consistent in case of back references
    @Transient
    private EntityCache cache;

    public ValueHolder() {
    }

    /**
     * Only pass entities!
     *
     * @param rootObject The entity that holds the valueHolder
     */
    public ValueHolder(Object rootObject) {
        this.rootObject = rootObject;
    }

    public ValueHolder(Mapper mapr, MappedField mf, DBRef ref, EntityCache cache) {
        init(mapr, mf, ref, cache);
    }

    protected void init(Mapper mapr, MappedField mf, DBRef dbRef, EntityCache cache) {
        this.mapper = mapr;
        this.mappedField = mf;
        this.cache = cache;
        this.referenceObjClass = getFieldType(mappedField);
        if (!Objects.equals(ref, dbRef)) {
            object = null;
        }
        this.ref = dbRef;
    }

    private void setRootObject(Object rootObject) {
        this.rootObject = rootObject;
    }

    private Mapper getMapper() {
        if (mapper == null) {
            mapper = MorphiaUtil.getMapper(MongoConnection.getInstance().getDatastore());
        }
        return mapper;
    }

    public ObjectId getId() {
        Object id = ref != null ? ref.getId() : null;
        if (id == null && object != null) {
            id = getMapper().getId(object);
        }
        return (ObjectId) id;
    }

    public T get() {
        fetch();
        return object;
    }

    private void setObject(T object) {
        if (object != null) {
            referenceObjClass = object.getClass();
        }
        this.object = object;
    }

    public void set(T object) {
        if (this.object != null || object == null) {
            ref = null;
        }
        setObject(object);
    }

    public T refresh() {
        if (ref != null) {
            object = null;
        }
        return get();
    }

    // for setting the value without the need to fetch the entity
    public void setValue(ValueHolder<T> value) {
        this.object = value.object;
        this.ref = value.ref;
        this.referenceObjClass = value.referenceObjClass;
        this.mappedField = value.mappedField;
        this.mapper = value.mapper;
    }

    public Class getObjectClass() {
        return object != null ? object.getClass() : null;
    }

    public boolean isFetched() {
        return object != null;
    }

    public boolean isEmpty() {
        return ref == null && object == null;
    }

    public DBRef getRef() {
        return ref;
    }

    private void fetch() {
        if (object == null && ref != null) {
            if (referenceObjClass == null) {
                throw new IllegalStateException("You must specify ValueHolder as generic type. Do NOT!!! omit actual type parameters! field: " + mappedField.getJavaFieldName());
            }
            object = cache.getEntity(new Key<T>(referenceObjClass, ref.getCollectionName(), ref.getId()));
            if (object != null) {
                return;
            }

            AdvancedDatastore datastore = MongoConnection.getInstance().getDatastore();
            Query query = datastore.find(ref.getCollectionName(), referenceObjClass);
            query.filter(Mapper.ID_KEY, ref.getId());
            if (query instanceof QueryWithCacheGetter) {
                QueryWithCacheGetter myQuery = (QueryWithCacheGetter) query;
                EntityCache cache = myQuery.getCache();
                mergeCaches(this.cache, cache);
                if (rootObject != null) {
                    try {
                        Key<Object> key = getMapper().getKey(rootObject);
                        key.setCollection(getMapper().getCollectionName(rootObject));
                        cache.putEntity(key, rootObject);
                    } catch (MappingException e) {
                        logger.log(Level.SEVERE, "Can not get key for rootObject " + rootObject + " of ValueHolder " + this, e);
                    }
                }
            }

            object = (T) query.get();
        }
    }

    public void invalidate() {
        if (ref != null) {
            object = null;
        }
    }

    private Class getFieldType(MappedField mappedField) {
        Field field = mappedField.getField();
        ParameterizedType genericType = (ParameterizedType) field.getGenericType();
        Type argument = genericType.getActualTypeArguments()[0];
        return (Class) argument;
    }

    protected void setRef(DBRef ref) {
        this.ref = ref;
    }

    public void setRef(DBRef ref, Class referenceObjClass) {
        this.ref = ref;
        this.referenceObjClass = referenceObjClass;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ValueHolder{");
        if (object != null) {
            sb.append("object=").append(object);
        } else {
            sb.append("referenceObjClass=").append(referenceObjClass).append(", ref=").append(ref);
        }
        sb.append("}");
        return sb.toString();
    }

    static void mergeCaches(EntityCache source, EntityCache destination) {
        if (destination instanceof ExtendedEntityCache) {
            ExtendedEntityCache destinationExtended = (ExtendedEntityCache) destination;
            destinationExtended.setParent(source);
        }
    }
}
