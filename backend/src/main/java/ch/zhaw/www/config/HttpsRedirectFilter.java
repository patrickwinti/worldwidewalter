package ch.zhaw.www.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * Redirects insecure HTTP requests to their HTTPS equivalent.
 * <p>
 * TLS is terminated by the reverse proxy in front of the application, so the effective
 * scheme is taken from the forwarded headers (enabled via
 * {@code server.forward-headers-strategy=framework}). When the effective request is not
 * secure, the client is redirected to the same URL over HTTPS.
 * <p>
 * The filter is disabled by default and enabled via {@code app.https-redirect.enabled=true}
 * so that local development over plain HTTP keeps working.
 */
@Component
@ConditionalOnProperty(name = "app.https-redirect.enabled", havingValue = "true")
public class HttpsRedirectFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        if (request.isSecure()) {
            filterChain.doFilter(request, response);
            return;
        }

        String httpsUrl = UriComponentsBuilder.fromUriString(request.getRequestURL().toString())
                .scheme("https")
                .replaceQuery(request.getQueryString())
                .build(true)
                .toUriString();

        response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
        response.setHeader("Location", httpsUrl);
    }
}
