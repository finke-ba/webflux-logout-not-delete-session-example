package com.example.webfluxlogoutnotdeletesessionexample;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.Document;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;
import org.springframework.lang.Nullable;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.session.data.mongo.JacksonMongoSessionConverter;
import org.springframework.session.data.mongo.MongoSession;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

public class CustomJacksonMongoSessionConverter extends JacksonMongoSessionConverter {

    private static final Log LOG = LogFactory.getLog(CustomJacksonMongoSessionConverter.class);

    private final ObjectMapper objectMapper = buildObjectMapper();

    private ObjectMapper buildObjectMapper() {

        ObjectMapper objectMapper = new ObjectMapper();

        // serialize fields instead of properties
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        // ignore unresolved fields (mostly 'principal')
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        objectMapper.setPropertyNamingStrategy(new MongoIdNamingStrategy());

        objectMapper.registerModules(SecurityJackson2Modules.getModules(getClass().getClassLoader()));
        objectMapper.addMixIn(MongoSession.class, MongoSessionMixin.class);
        objectMapper.addMixIn(HashMap.class, HashMapMixin.class);

        return objectMapper;
    }


    @Override
    @Nullable
    protected MongoSession convert(Document source) {

        Date expireAt = (Date) source.remove("expireAt");

        String json = source.toJson(JsonWriterSettings.builder().outputMode(JsonMode.RELAXED).build());

        try {
            // TODO - looks like this line contains problem - in JdkMongoSessionConverter slight different behaviour -
            //  MongoSession session = new MongoSession(sessionWrapper.getString(ID), maxIntervalDuration.getSeconds())
            MongoSession mongoSession = this.objectMapper.readValue(json, MongoSession.class);
            // I'll do new Session instead of it - like in JdkMongoSessionConverter
            mongoSession = new MongoSession(mongoSession.getId(), mongoSession.getMaxInactiveInterval().getSeconds());

            mongoSession.setExpireAt(expireAt);
            return mongoSession;
        } catch (IOException e) {
            LOG.error("Error during Mongo Session deserialization", e);
            return null;
        }
    }

    /**
     * Used to whitelist {@link MongoSession} for {@link SecurityJackson2Modules}.
     */
    private static class MongoSessionMixin {
        // Nothing special
    }

    /**
     * Used to whitelist {@link HashMap} for {@link SecurityJackson2Modules}.
     */
    private static class HashMapMixin {
        // Nothing special
    }

    private static class MongoIdNamingStrategy extends PropertyNamingStrategy.PropertyNamingStrategyBase {

        @Override
        public String translate(String propertyName) {

            switch (propertyName) {
                case "id":
                    return "_id";
                case "_id":
                    return "id";
                default:
                    return propertyName;
            }
        }
    }
}

