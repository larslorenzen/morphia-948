package de.lalo.morphia.lazyreference;

import com.mongodb.DBObject;
import org.mongodb.morphia.mapping.CustomMapper;
import org.mongodb.morphia.mapping.MappedField;
import org.mongodb.morphia.mapping.Mapper;
import org.mongodb.morphia.mapping.cache.EntityCache;
import org.mongodb.morphia.mapping.lazy.proxy.ProxiedEntityReferenceList;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * User: llorenzen
 * Date: 18.07.11
 * Time: 11:27
 */
public class IndirectSet extends IndirectEntityReferenceList implements Set, ProxiedEntityReferenceList {

    private Set values;

    public IndirectSet(CustomMapper mapper, DBObject dbObject, MappedField mf, Object entity, EntityCache cache, Mapper mapr) {
        super(mapper, dbObject, mf, entity, cache, mapr);
    }

    @Override
    public int size() {
        fetch();
        return values.size();
    }

    @Override
    public boolean isEmpty() {
        fetch();
        return values.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        fetch();
        return values.contains(o);
    }

    @Override
    public Iterator iterator() {
        fetch();
        return values.iterator();
    }

    @Override
    public Object[] toArray() {
        fetch();
        return values.toArray();
    }

    @Override
    public boolean add(Object o) {
        fetch();
        return values.add(o);
    }

    @Override
    public boolean remove(Object o) {
        fetch();
        return values.remove(o);
    }

    @Override
    public boolean containsAll(Collection c) {
        fetch();
        return values.containsAll(c);
    }

    @Override
    public boolean addAll(Collection c) {
        fetch();
        return values.addAll(c);
    }

    @Override
    public boolean retainAll(Collection c) {
        fetch();
        return values.retainAll(c);
    }

    @Override
    public boolean removeAll(Collection c) {
        fetch();
        return values.removeAll(c);
    }

    @Override
    public void clear() {
        if (values != null) {
            values.clear();
        }
    }

    @Override
    public Object[] toArray(Object[] a) {
        fetch();
        return values.toArray(a);
    }

    private void fetch() {
        if (values == null) {
            load();
            postFetch();
        }
    }

    private synchronized void load() {
        if (values == null) {
            values = fetchReferences();
        }
    }

    @Override
    protected <T extends Collection> T createCollection() {
        return (T) mapr.getOptions().getObjectFactory().createSet(mf);
    }

    @Override
    public boolean __isFetched() {
        return values != null;
    }

    @Override
    public Object __unwrap() {
        return values;
    }

}
