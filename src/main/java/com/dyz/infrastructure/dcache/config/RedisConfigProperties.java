package com.dyz.infrastructure.dcache.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "dcache.redis")
public class RedisConfigProperties {

    private String host;

    private String password;

    private Integer port;

    private Integer minIdle;

    private Integer maxIdle;

    private Integer maxTotal;

    private Integer maxWaitMillis;

    private Integer timeout;

    private Integer dbIndex;

    private Boolean testOnBorrow;
}
