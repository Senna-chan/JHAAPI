package com.github.thibstars.jhaapi.internal.interceptors;

import okhttp3.Interceptor;
import okhttp3.Response;

import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.net.ConnectException;

public class RetryInterceptor implements Interceptor {
    private final int maxRetries;
    private final long backoffMs;

    public RetryInterceptor(int maxRetries, long backoffMs) {
        this.maxRetries = maxRetries;
        this.backoffMs = backoffMs;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        IOException last = null;
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                return chain.proceed(chain.request());
            } catch (SSLHandshakeException | ConnectException e) {
                last = e;
                try {
                    Thread.sleep(backoffMs * (attempt + 1));
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw e;
                }
            }
        }
        throw last;
    }
}
