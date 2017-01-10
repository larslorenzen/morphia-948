package de.lalo.morphia.lazyreference;

import com.mongodb.DBObject;
import com.mongodb.DBRef;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.annotations.Reference;
import org.mongodb.morphia.mapping.CustomMapper;
import org.mongodb.morphia.mapping.MappedClass;
import org.mongodb.morphia.mapping.MappedField;
import org.mongodb.morphia.mapping.Mapper;
import org.mongodb.morphia.mapping.MappingException;
import org.mongodb.morphia.mapping.cache.EntityCache;

import java.util.Map;

/**
 * User: llorenzen
 * Date: 18.07.11
 * Time: 11:03
 */
public class IndirectReferenceMapper implements CustomMapper {

    private CustomMapper wrapped;

    public IndirectReferenceMapper(CustomMapper wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void toDBObject(Object entity, MappedField mf, DBObject dbObject, Map<Object, DBObject> involvedObjects, Mapper mapr) {
        if (ValueHolder.class.equals(mf.getType())) {
            ValueHolder valueHolder = (ValueHolder) mf.getFieldValue(entity);
            String name = mf.getNameToStore();
            if (valueHolder == null || valueHolder.isEmpty()) {
                if (!mapr.getOptions().isStoreNulls()) {
                    return;
                }
                dbObject.put(name, null);
                return;
            }
            if (valueHolder.isFetched()) {
                Object fieldValue = valueHolder.get();
                DBRef dbrefFromKey = mapr.keyToDBRef(getKey(fieldValue, mapr));
                valueHolder.setRef(dbrefFromKey);
                dbObject.put(name, dbrefFromKey);
            } else {
                DBRef ref = valueHolder.getRef();
                dbObject.put(name, ref);
            }
        } else {
            wrapped.toDBObject(entity, mf, dbObject, involvedObjects, mapr);
        }
    }

    @Override
    public void fromDBObject(Datastore datastore, DBObject dbObject, MappedField mf, Object entity, EntityCache cache, Mapper mapr) {
        Reference annotation = mf.getAnnotation(Reference.class);
        if (annotation.lazy() && mf.isMultipleValues()) {
            Object value = mf.isSet() ? new IndirectSet(wrapped, dbObject, mf, entity, cache, mapr) : new IndirectList(wrapped, dbObject, mf, entity, cache, mapr);
            mf.setFieldValue(entity, value);
        } else if (ValueHolder.class.equals(mf.getType())) {
            ValueHolder value = (ValueHolder) mf.getFieldValue(entity);
            DBRef dbObjectValue = (DBRef) mf.getDbObjectValue(dbObject);
            if (value == null) {
                value = new ValueHolder(mapr, mf, dbObjectValue, cache);
            } else {
                value.init(mapr, mf, dbObjectValue, cache);
            }
            Object referencedEntity = cache.getEntity(mapr.refToKey(dbObjectValue));
            if (referencedEntity != null) {
                value.set(referencedEntity);
            }
            mf.setFieldValue(entity, value);
        } else {
            wrapped.fromDBObject(datastore, dbObject, mf, entity, cache, mapr);
        }
    }


    private Key<?> getKey(Object entity, Mapper mapr) {
        try {
            MappedClass mappedClass = mapr.getMappedClass(entity);
            Object id = mappedClass.getIdField().get(entity);
            if (id == null) {
                throw new MappingException("@Id field cannot be null! for entity " + entity);
            }
            return new Key<>(mappedClass.getClazz(), mappedClass.getCollectionName(), id);
        } catch (IllegalAccessException iae) {
            throw new RuntimeException(iae);
        }
    }


}
