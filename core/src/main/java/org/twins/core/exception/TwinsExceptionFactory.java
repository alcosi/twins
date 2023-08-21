package org.twins.core.exception;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.exception.ServiceExceptionFactory;
import org.springframework.stereotype.Component;

@Component
public class TwinsExceptionFactory extends ServiceExceptionFactory {
    public static final String SERVICE_CODE_TWINS = "twins";

    public ServiceException incorrectConfiguration(String message) {
        return new ServiceException(SERVICE_CODE_TWINS, 1000, message);
    }
}
