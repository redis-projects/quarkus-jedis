package org.acme.redis.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.runtime.Startup;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.UnifiedJedis;


import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;

import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

@Slf4j
@Startup
@ApplicationScoped
public class JedisConfig {

    @ConfigProperty(name = "redis.db.host")
    String host;

    @ConfigProperty(name = "redis.db.port")
    int port;

    private volatile JedisPool jedisPool;

    @Dependent
    @Produces
    public UnifiedJedis jedis() {
        log.info("Bootstrapping Jedis with DB Host: '{}' and port: '{}'", host, port);
        UnifiedJedis jedis = new JedisPooled(host, port);
        return jedis;
    }

    public void destroy(@Disposes UnifiedJedis jedis) {
        jedis.close();
    }
}
