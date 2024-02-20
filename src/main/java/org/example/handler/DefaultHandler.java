package org.example.handler;

import org.example.client.Client;
import org.example.client.DefaultClient;
import org.example.response.ApplicationStatusResponse;
import org.example.response.Response;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

public class DefaultHandler implements Handler {

    private final Client client;

    private final ExecutorService executorService = Executors.newFixedThreadPool(2);
    ;

    private ApplicationStatusResponse.Failure LastCacheFailure;

    private static final Map<String, Integer> AttemptsCount = new HashMap<>();

    public DefaultHandler(Client client) {
        this.client = client;
    }

    @Override
    public ApplicationStatusResponse performOperation(String id) {
        var executor = Executors.newSingleThreadExecutor();
        var result = executor.submit(() -> this.request(id));

        try {
            return result.get(15, TimeUnit.SECONDS);
        } catch (Exception exception) {
            var attempt = Optional.ofNullable(AttemptsCount.get(id)).orElse(0);

            return new ApplicationStatusResponse.Failure(null, attempt);
        }
    }

    private ApplicationStatusResponse request(String id) {
        var start = Instant.now();
        Callable<Response> callable = () -> this.client.getApplicationStatus1(id);
        Callable<Response> callable1 = () -> this.client.getApplicationStatus2(id);

        try {
            var result = this.executorService.invokeAny(List.of(callable, callable1), 15, TimeUnit.SECONDS);

            if (result instanceof Response.Success success) {
                return new ApplicationStatusResponse.Success(success.applicationId(), success.applicationStatus());
            } else if (result instanceof Response.RetryAfter retry) {
                Thread.sleep(retry.delay().toMillis());
                this.request(id);
            }

            throw ((Response.Failure) result).ex();
        } catch (Throwable exception) {
            var attempt = Optional.ofNullable(AttemptsCount.get(id)).orElse(0);
            var lastFailure = Optional.ofNullable(this.LastCacheFailure)
                    .map(ApplicationStatusResponse.Failure::lastRequestTime)
                    .orElseGet(() -> Duration.between(start, Instant.now()));

            return new ApplicationStatusResponse.Failure(lastFailure, attempt);
        }
    }
}
