package de.lalo.morphia.lazyreference;

import com.mongodb.MongoClient;
import org.mongodb.morphia.AdvancedDatastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.mapping.Mapper;

/**
 * @author llorenzen
 * @since 10/01/2017
 */
public class MongoConnection {

    private static final MongoConnection instance = new MongoConnection();

    private Morphia morphia;

    private MongoClient mongoClient;

    MongoConnection() {
        this.morphia = new Morphia();
        this.mongoClient = new MongoClient("localhost");
        Mapper mapper = morphia.getMapper();
        mapper.getOptions().setCacheFactory(new ExtendedEntityCache.Factory());

        mapper.getOptions().setReferenceMapper(new IndirectReferenceMapper(mapper.getOptions().getReferenceMapper()));
        mapper.getOptions().setDefaultMapper(mapper.getOptions().getEmbeddedMapper());
    }

    AdvancedDatastore getDatastore() {
        AdvancedDatastore datastore = (AdvancedDatastore) morphia.createDatastore(mongoClient, "test");
        datastore.setQueryFactory(new QueryWithCacheGetter.Factory());
        return datastore;
    }

    static MongoConnection getInstance() {
        return instance;
    }

}
