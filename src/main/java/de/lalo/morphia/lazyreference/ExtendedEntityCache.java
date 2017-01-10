package de.lalo.morphia.lazyreference;

import org.mongodb.morphia.Key;
import org.mongodb.morphia.mapping.cache.DefaultEntityCache;
import org.mongodb.morphia.mapping.cache.EntityCache;
import org.mongodb.morphia.mapping.cache.EntityCacheFactory;

/**
 * User: llorenzen
 * Date: 02.10.11
 * Time: 21:27
 */
public class ExtendedEntityCache extends DefaultEntityCache {

    private EntityCache parent;

    @Override
    public Boolean exists(Key<?> k) {
        Boolean exists = super.exists(k);
        if (Boolean.TRUE.equals(exists)) {
            return true;
        }
        if (parent != null) {
            exists = parent.exists(k);
            if (Boolean.TRUE.equals(exists)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public <T> T getEntity(Key<T> k) {
        T entity = null;
        if (parent != null) {
            entity = parent.getEntity(k);
        }
        if (entity == null) {
            entity = super.getEntity(k);
        }
        return entity;
    }

    @Override
    public <T> T getProxy(Key<T> k) {
        T entity = null;
        if (parent != null) {
            entity = parent.getProxy(k);
        }
        if (entity == null) {
            entity = super.getProxy(k);
        }
        return entity;
    }

    void setParent(EntityCache parent) {
        this.parent = parent;
    }

    public static class Factory implements EntityCacheFactory {

        @Override
        public EntityCache createCache() {
            return new ExtendedEntityCache();
        }

    }
}
