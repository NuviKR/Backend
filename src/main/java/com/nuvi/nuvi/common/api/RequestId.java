package com.nuvi.nuvi.common.api;

import java.util.Optional;
import java.util.UUID;

public final class RequestId {

    public static final String HEADER_NAME = "X-Request-Id";
    public static final String ATTRIBUTE_NAME = RequestId.class.getName() + ".value";

    private static final ThreadLocal<String> CURRENT = new ThreadLocal<>();

    private RequestId() {
    }

    public static String newRequestId() {
        return "req_" + UUID.randomUUID().toString().replace("-", "");
    }

    public static Optional<String> current() {
        return Optional.ofNullable(CURRENT.get());
    }

    static void set(String requestId) {
        CURRENT.set(requestId);
    }

    static void clear() {
        CURRENT.remove();
    }
}
