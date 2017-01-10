package de.lalo.morphia.lazyreference;

import com.mongodb.DBObject;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.mapping.CustomMapper;
import org.mongodb.morphia.mapping.MappedField;
import org.mongodb.morphia.mapping.Mapper;
import org.mongodb.morphia.mapping.cache.EntityCache;
import org.mongodb.morphia.mapping.lazy.proxy.ProxiedEntityReferenceList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * User: llorenzen
 * Date: 18.07.11
 * Time: 11:42
 */
public class IndirectList extends IndirectEntityReferenceList implements List, ProxiedEntityReferenceList {

    private volatile List values;

    public IndirectList(CustomMapper mapper, DBObject dbObject, MappedField mf, Object entity, EntityCache cache, Mapper mapr) {
        super(mapper, dbObject, mf, entity, cache, mapr);
    }

    @Override
    public int size() {
        if (__isFetched()) {
            return values.size();
        }
        return __getKeysAsList().size();
    }

    @Override
    public boolean isEmpty() {
        if (__isFetched()) {
            return values.isEmpty();
        }
        return __getKeysAsList().isEmpty();
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

    public Object[] toArray(Object[] a) {
        fetch();
        return values.toArray(a);
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
    public boolean addAll(int index, Collection c) {
        fetch();
        return values.addAll(index, c);
    }

    public boolean removeAll(Collection c) {
        fetch();
        return values.removeAll(c);
    }

    public boolean retainAll(Collection c) {
        fetch();
        return values.retainAll(c);
    }

    @Override
    public void clear() {
        fetch();
        values.clear();
    }

    @Override
    public boolean equals(Object o) {
        fetch();
        return values.equals(o);
    }

    @Override
    public int hashCode() {
        fetch();
        return values.hashCode();
    }

    @Override
    public Object get(int index) {
        fetch();
        return values.get(index);
    }

    @Override
    public Object set(int index, Object element) {
        fetch();
        return values.set(index, element);
    }

    @Override
    public void add(int index, Object element) {
        fetch();
        values.add(index, element);
    }

    @Override
    public Object remove(int index) {
        fetch();
        return values.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        fetch();
        return values.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        fetch();
        return values.lastIndexOf(o);
    }

    @Override
    public ListIterator listIterator() {
        fetch();
        return values.listIterator();
    }

    @Override
    public ListIterator listIterator(int index) {
        fetch();
        return values.listIterator(index);
    }

    @Override
    public List subList(int fromIndex, int toIndex) {
        fetch();
        return values.subList(fromIndex, toIndex);
    }

    private void fetch() {
        if (values == null) {
            load();
            postFetch();
        }
    }

    private synchronized void load() {
        if (values == null) {
            // a simple getByKeys(...) won't retain order

            List retrievedEntities = fetchReferences();
            List tmpList = new ArrayList(retrievedEntities);
            for (Object entity : tmpList) {
                Key<Object> key = new Key<>(entity.getClass(), mapr.getCollectionName(entity), mapr.getId(entity));
                int index = __getKeysAsList().indexOf(key);
                if (index == -1) {
                    key = mapr.getKey(entity);
                    index = __getKeysAsList().indexOf(key);
                }
                retrievedEntities.set(index, entity);
            }

            values = retrievedEntities;
        }
    }

    protected <T extends Collection> T createCollection() {
        return (T) mapr.getOptions().getObjectFactory().createList(mf);
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
