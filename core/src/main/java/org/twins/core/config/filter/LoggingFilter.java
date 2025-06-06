package org.twins.core.config.filter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.cambium.common.util.JsonUtils;
import org.cambium.common.util.LoggerUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.twins.core.controller.rest.annotation.Loggable;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

import static org.twins.core.service.HttpRequestService.*;

//@RequiredArgsConstructor
public class LoggingFilter extends OncePerRequestFilter {
    @Autowired
    private List<HandlerMapping> handlerMappings;
    public static final String REQUEST_LOG_ID = "RequestLogId";

    public static final String CONTROLLER_METHOD = "ControllerMethod";
    public static final Random RANDOM = new Random();

    public static class LogInternalService {


        public void afterRequest(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response, Long time) {
            try {
                if ("OPTIONS".equals(request.getMethod())) {
                    return;
                }
                String id = request.getAttribute(REQUEST_LOG_ID) + "";
                logRequest(request, id);
                logResponse(request, response, id, time);
                response.copyBodyToResponse();
                LoggerUtils.cleanMDC();
            } catch (Throwable t) {
                log.error("RqRs error !", t);
            }

        }

        private void logRequest(ContentCachingRequestWrapper request, String rqId) {
            logHeaders(request, List.of(HEADER_DOMAIN_ID, HEADER_CHANNEL, HEADER_BUSINESS_ACCOUNT_ID, HEADER_LOCALE));
            Loggable loggable = LoggingFilter.getLoggableMethodAnnotation(request);
            byte[] content = request.getContentAsByteArray();
            logContent(content, request.getContentType(), request.getCharacterEncoding(), "RQ", rqId, loggable != null ? loggable.rqBodyThreshold() : 0);
        }

        private void logHeaders(ContentCachingRequestWrapper request, List<String> headerNameList) {
            List<String> logHeaders = new ArrayList<>();
            for (String headerName : headerNameList) {
                String headerValue = request.getHeader(headerName);
                if (headerValue != null)
                    logHeaders.add(headerName + ":[" + headerValue + "]");
            }
            if (!logHeaders.isEmpty())
                logInfoBoth("RQ_HEADERS: {}", String.join(", ", logHeaders));
        }


        private void logContent(byte[] content, String contentType, String contentEncoding, String prfx, String rqId, int logShortThreshold) {
            try {
                String message = new String(content, contentEncoding);
                message = JsonUtils.mask(new String[]{"fullName", "accessToken", "refreshToken", "username", "password", "email"}, message);
                log.info("{}_BODY: {}", prfx, message);
                if (message.length() > 2000 && message.indexOf("openapi") > 0) { // swagger output is too big
                    logShort.info("{}_BODY: <content> is longer then 2000 symbols. Please see other log file", prfx);
                } else if (logShortThreshold == 0 || logShortThreshold > message.length())
                    logShort.info("{}_BODY: {}", prfx, message);
                else
                    logShort.info("{}_BODY: <content> is longer then {} symbols. Please see other log file", prfx, logShortThreshold);
            } catch (Throwable t) {
                logErrorBoth("", t);
            }
        }

        private void logResponse(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response, String rqId, Long time) {
            String requestUri = StringUtils.defaultIfBlank(request.getRequestURI(), "");
            if (!requestUri.contains("/actuator/prometheus")) {
            int status = response.getStatus();
            String queryString = request.getQueryString() == null ? "" : "?" + request.getQueryString();
            logInfoBoth("RS_URL {}:{}{} STATUS:{} {} | {} ms", request.getMethod(), requestUri, queryString, status, HttpStatus.valueOf(status).getReasonPhrase(), System.currentTimeMillis() - time);
            response.getHeaderNames().forEach(headerName ->
                    log.debug("RS_HEADER {}: {}", headerName, response.getHeader(headerName)));
            byte[] content = response.getContentAsByteArray();
            Loggable loggable = LoggingFilter.getLoggableMethodAnnotation(request);

                logContent(content, response.getContentType(), response.getCharacterEncoding(), "RS", rqId, loggable != null ? loggable.rsBodyThreshold() : 0);
            }
        }
    }

    static final Logger log = ((Logger) LoggerFactory.getLogger("RqRsLogger"));
    static final Logger logShort = ((Logger) LoggerFactory.getLogger("RqRsLoggerShort"));

    private static void logInfoBoth(String format, Object... argArray) {
        log.info(format, argArray);
        logShort.info(format, argArray);
    }

    private static void logErrorBoth(String msg, Throwable t) {
        log.error(msg, t);
        logShort.error(msg, t);
    }


    static {
        log.setLevel(Level.TRACE);
    }


    @Autowired
    LogInternalService logInternalService;

    private HandlerExecutionChain handles(HttpServletRequest request, HandlerMapping h) {
        try {
            return h.getHandler(request);
        } catch (Exception e) {
            logErrorBoth("", e);
            return null;
        }
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        logController(request);
        if (isLoggable(request)) {
            logSessionId(request);
            doFilterWrapped(wrapRequest(request), wrapResponse(response), filterChain);
        } else {
            filterChain.doFilter(request, response);
            log.trace("Ignoring request logging {}", request.getRequestURI());
            logShort.trace("Ignoring request logging {}", request.getRequestURI());
        }
    }

    private Boolean isLoggable(HttpServletRequest request) {
        Loggable loggingController = getLoggableMethodAnnotation(request);
        if (loggingController == null) {
            return true;
        }
        return loggingController.value();
    }

    protected static Loggable getLoggableMethodAnnotation(HttpServletRequest request) {
        Method method = (Method) request.getAttribute(CONTROLLER_METHOD);
        if (method == null) {
            return null;
        }
        return method.getDeclaredAnnotation(Loggable.class);
    }

    private void logController(HttpServletRequest request) {
        Optional<Method> method = handlerMappings.stream()
                .map(h -> handles(request, h))
                .filter(Objects::nonNull)
                .map(HandlerExecutionChain::getHandler)
                .filter(h -> h instanceof HandlerMethod)
                .map(h -> (HandlerMethod) h)
                .map(HandlerMethod::getMethod)
                .findFirst();
        method.ifPresent(m -> request.setAttribute(CONTROLLER_METHOD, m));
        String controller = method
                .map(Method::getName)
                .orElse("");
        LoggerUtils.logController(controller);
    }

    private void logSessionId(HttpServletRequest request) {
        String sessionid = Optional.ofNullable(request.getHeader("Authorization")).map(String::toUpperCase).orElseGet(() -> UUID.randomUUID().toString().replace("-", "").toUpperCase());
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            attrs.setAttribute("SESSION_ID", sessionid, RequestAttributes.SCOPE_REQUEST);
        }
        LoggerUtils.logSession(sessionid);
    }

    private ContentCachingRequestWrapper wrapRequest(HttpServletRequest request) {
        if (request instanceof ContentCachingRequestWrapper) {
            return (ContentCachingRequestWrapper) request;
        } else {
            return new ContentCachingRequestWrapper(request);
        }
    }

    protected void doFilterWrapped(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response, FilterChain filterChain) throws ServletException, IOException {
        Long time = System.currentTimeMillis();
        try {
            String id = getIdString();
            String queryString = request.getQueryString() == null ? "" : "?" + request.getQueryString();
            logInfoBoth("RQ_URL {}:{}{}", request.getMethod(), request.getRequestURI(), queryString);
            request.setAttribute(REQUEST_LOG_ID, id);
            filterChain.doFilter(request, response);
        } finally {
            logInternalService.afterRequest(request, response, time);
        }
    }

    private String getIdString() {
        Integer integer = RANDOM.nextInt(100000000);
        String leftPad = StringUtils.leftPad(integer + "", 8, '0');
        return leftPad.substring(0, 4) + '-' + leftPad.substring(5);
    }

    private ContentCachingResponseWrapper wrapResponse(HttpServletResponse response) {
        if (response instanceof ContentCachingResponseWrapper) {
            return (ContentCachingResponseWrapper) response;
        } else {
            return new ContentCachingResponseWrapper(response);
        }
    }
}
