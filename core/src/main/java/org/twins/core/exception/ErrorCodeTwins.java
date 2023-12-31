package org.twins.core.exception;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

import java.util.Arrays;

@RequiredArgsConstructor
public enum ErrorCodeTwins implements ErrorCode {
    UUID_UNKNOWN(10000, "uuid is unknown"),
    UUID_ALREADY_EXIST(10001, "uuid is already exist"),

    ENTITY_INVALID(10002, "entity invalid"),
    USER_UNKNOWN(10001, "unknown user"),
    USER_GROUP_UNKNOWN(10901, "user group is unknown"),
    USER_GROUP_ENTER_ERROR(10902, "user group can not be entered"),
    USER_GROUP_IS_MANDATORY(10903, "user group is mandatory"),
    BUSINESS_ACCOUNT_UNKNOWN(10101, "unknown business_account"),
    BUSINESS_ACCOUNT_USER_ALREADY_EXISTS(10102, "business_account user already exists"),
    DOMAIN_UNKNOWN(10201, "unknown domain"),
    DOMAIN_USER_ALREADY_EXISTS(10202, "domain user already exists"),
    DOMAIN_USER_NOT_EXISTS(10203, "domain user is not registered"),
    DOMAIN_BUSINESS_ACCOUNT_ALREADY_EXISTS(10204, "domain business_account already exists"),
    DOMAIN_BUSINESS_ACCOUNT_NOT_EXISTS(10205, "domain business_account is not registered"),
    PERMISSION_SCHEMA_NOT_ALLOWED(10301, "permission schema is not allowed"),
    TWIN_CLASS_SCHEMA_NOT_ALLOWED(10401, "twin class schema is not allowed"),
    TWIN_CLASS_FIELD_KEY_UNKNOWN(10402, "twin class field key is unknown"),
    TWIN_CLASS_FIELD_VALUE_TYPE_INCORRECT(10403, "twin class field value type is incorrect"),
    TWIN_CLASS_FIELD_VALUE_MULTIPLY_OPTIONS_ARE_NOT_ALLOWED(10404, "twin class field value multiply options are not allowed"),
    TWIN_CLASS_FIELD_VALUE_REQUIRED(10405, "twin class field value required"),
    TWIN_CLASS_FIELD_VALUE_INCORRECT(10406, "twin class field value incorrect"),
    TWIN_CLASS_FIELD_INCORRECT_TYPE(10407, "twin class field type incorrect"),
    TWIN_CLASS_FIELD_VALUE_IS_ALREADY_IN_USE(10408, "twin class field value is already in use"),
    TWINFLOW_SCHEMA_NOT_ALLOWED(10501, "twinflow schema is not allowed"),
    TWINFLOW_SCHEMA_NOT_CONFIGURED(10502, "twinflow schema is not configured"),
    TWINFLOW_TRANSACTION_INCORRECT(10503, "twinflow transition can not be performed"),
    TWINFLOW_TRANSACTION_DENIED(10504, "twinflow transition is denied"),
    DATALIST_OPTION_IS_NOT_VALID_FOR_LIST(10601, "data list option is not valid for current data list"),
    DATALIST_LIST_UNKNOWN(10602, "data list is unknown"),
    SPACE_TWIN_ID_INCORRECT(10701, "given twin id is not a space twin"),
    HEAD_TWIN_ID_NOT_ALLOWED(10702, "given twin is not allowed for given class"),
    HEAD_TWIN_NOT_SPECIFIED(10703, "space twin must be specified"),
    TWIN_ALIAS_UNKNOWN(10801, "alias is unknown"),
    TWIN_LINK_INCORRECT(10901, "twins can not be linkend"),
    TWIN_FIELD_VALUE_INCORRECT(10902, "twins field value can not be converted"),
    FACTORY_INCORRECT(11001, "twin factory config is incorrect"),
    FACTORY_PIPELINE_STEP_ERROR(11002, "twin factory pipeline step error"),
    TWIN_STATUS_INCORRECT(11101, "twin status is incorrect");

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCodeTwins(int code, String message) {
        this(code, message, HttpStatus.BAD_REQUEST);
    }

    @Override
    public String getServiceCode() {
        return "TWINS";
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public static ErrorCode byCode(int code) {
        return Arrays.stream(values()).filter(e -> e.code == code).findAny().orElse(null);
    }
}
