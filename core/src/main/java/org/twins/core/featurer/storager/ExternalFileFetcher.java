package org.twins.core.featurer.storager;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.UnknownHostException;
import java.time.Duration;

/**
 * SSRF-safe fetcher for external file uris. Guards every request:
 * only http/https schemes are allowed, the host must not resolve to a local or private
 * network address, and redirects are followed manually with each hop re-validated
 * (OkHttp automatic redirect following is disabled).
 * DNS-rebinding between the validation resolve and the connect remains a known limitation.
 * Java address checks do not cover IPv6 ULA (fc00::/7) and CGNAT (100.64.0.0/10).
 */
@Slf4j
public final class ExternalFileFetcher {
    private static final int MAX_REDIRECTS = 5;
    private static OkHttpClient httpClient;

    private ExternalFileFetcher() {
    }

    /**
     * Sends an HTTP GET request to the given uri following redirects manually (each hop is
     * re-validated against the SSRF guard, max {@value #MAX_REDIRECTS} hops).
     *
     * @param uri     the target uri
     * @param timeout connect/read timeout; applied when the shared client is built
     * @return the successful response with a non-null body; the caller must close it
     * @throws ServiceException if the uri is not allowed or the file cannot be retrieved
     * @throws IOException      if an I/O error occurs while sending or receiving the request
     */
    public static Response fetch(URI uri, Duration timeout) throws ServiceException, IOException {
        if (httpClient == null) {
            // built once with the first caller's timeout; later calls reuse that client
            httpClient = new OkHttpClient.Builder()
                    .proxySelector(ProxySelector.getDefault())
                    .connectTimeout(timeout)
                    .readTimeout(timeout)
                    // redirects are followed manually to re-validate each hop against the SSRF guard
                    .followRedirects(false)
                    .followSslRedirects(false)
                    .build();
        }

        URI currentUri = uri;
        for (int redirectCount = 0; ; redirectCount++) {
            validateExternalFileUri(currentUri);
            Response response = executeWithRetries(currentUri);
            if (!response.isRedirect()) {
                if (!response.isSuccessful()) {
                    response.close();
                    throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID, "Failed to retrieve the file: HTTP Status " + response.code());
                }
                if (response.body() == null) {
                    response.close();
                    throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID, "Failed to retrieve the file, response body is NULL");
                }
                return response;
            }
            String location = response.header("Location");
            response.close();
            if (location == null)
                throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID, "Failed to retrieve the file: redirect without Location header");
            if (redirectCount >= MAX_REDIRECTS)
                throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID, "Failed to retrieve the file: too many redirects");
            try {
                currentUri = currentUri.resolve(location);
            } catch (IllegalArgumentException e) {
                throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID, "Failed to retrieve the file: bad redirect location " + location);
            }
        }
    }

    /**
     * SSRF guard for external file uris: allows only http/https schemes and rejects hosts resolving
     * to any local, loopback, link-local, site-local (private network) or multicast address.
     */
    public static void validateExternalFileUri(URI uri) throws ServiceException {
        String scheme = uri.getScheme();
        if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https")))
            throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID, "External file uri scheme is not allowed: " + scheme);
        String host = uri.getHost();
        if (host == null || host.isBlank())
            throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID, "External file uri host is empty");
        final InetAddress[] addresses;
        try {
            addresses = InetAddress.getAllByName(host);
        } catch (UnknownHostException e) {
            throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID, "External file uri host can not be resolved: " + host);
        }
        for (InetAddress address : addresses)
            if (address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isLinkLocalAddress()
                    || address.isSiteLocalAddress() || address.isMulticastAddress())
                throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID, "External file uri host resolves to a local or private network address: " + host);
    }

    private static Response executeWithRetries(URI uri) throws IOException {
        Request request = new Request.Builder()
                .url(uri.toURL())
                .get()
                .build();

        int maxRetries = 3;
        IOException lastException = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                return httpClient.newCall(request).execute();
            } catch (IOException e) {
                lastException = e;
                log.warn("Attempt {}/{} failed for URI {}: {}", attempt, maxRetries, uri, e.getMessage());
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(500L * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Retry interrupted", ie);
                    }
                }
            }
        }

        throw lastException;
    }
}
