package com.nuvi.nuvi.common.api;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;

@Component
public class RequestMetaFactory {

    private final Clock clock;

    public RequestMetaFactory() {
        this(Clock.systemUTC());
    }

    RequestMetaFactory(Clock clock) {
        this.clock = clock;
    }

    public Meta current() {
        return new Meta(RequestId.current().orElseGet(RequestId::newRequestId), Instant.now(clock));
    }
}
