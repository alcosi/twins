/*
 * Copyright (c)
 * created:2021 - 5 - 13
 * by Yan Tayanouski
 * ESAS Ltd. La propriété, c'est le vol!
 */

package org.cambium.common.exception;

import org.springframework.http.HttpStatus;

public interface ErrorCode {
    public String getServiceCode();

    public int getCode();

    public String getMessage();

    public HttpStatus getHttpStatus();
}
