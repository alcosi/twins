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
    ENTITY_ALREADY_EXIST(10003, "entity is already exist in db. Please check unique keys"),
    USER_UNKNOWN(10101, "unknown user"),
    USER_LOCALE_UNKNOWN(10102, "unknown locale"),
    DOMAIN_UNKNOWN(10201, "unknown domain"),
    DOMAIN_TYPE_UNSUPPORTED(10202, "domain type unsupported"),
    DOMAIN_KEY_INCORRECT(10203, "domain key is incorrect"),
    DOMAIN_KEY_UNAVAILABLE(10204, "domain key is already in use"),
    DOMAIN_USER_ALREADY_EXISTS(10205, "domain user already exists"),
    DOMAIN_USER_NOT_EXISTS(10206, "domain user is not registered"),
    DOMAIN_BUSINESS_ACCOUNT_ALREADY_EXISTS(10207, "domain business_account already exists"),
    DOMAIN_BUSINESS_ACCOUNT_NOT_EXISTS(10208, "domain business_account is not registered"),
    DOMAIN_LOCALE_UNKNOWN(10209, "unknown locale"),
    DOMAIN_OR_BUSINESS_ACCOUNT_USER_NOT_EXISTS(10210, "domain or business_account user not exists"),
    PERMISSION_SCHEMA_NOT_ALLOWED(10301, "permission schema is not allowed"),
    PERMISSION_ID_UNKNOWN(10302, "permission id unknown"),
    TWIN_NOT_PROTECTED(10303, "Twin is not protected by permission"),
    PERMISSION_SCHEMA_NOT_SPECIFIED(10304, "permission schema is not specified"),
    TWIN_CLASS_SCHEMA_NOT_ALLOWED(10401, "twin class schema is not allowed"),
    TWIN_CLASS_FIELD_KEY_UNKNOWN(10402, "twin class field key is unknown"),
    TWIN_CLASS_FIELD_VALUE_TYPE_INCORRECT(10403, "twin class field value type is incorrect"),
    TWIN_CLASS_FIELD_VALUE_MULTIPLY_OPTIONS_ARE_NOT_ALLOWED(10404, "twin class field value multiply options are not allowed"),
    TWIN_CLASS_FIELD_VALUE_REQUIRED(10405, "twin class field value required"),
    TWIN_CLASS_FIELD_VALUE_INCORRECT(10406, "twin class field value incorrect"),
    TWIN_CLASS_FIELD_INCORRECT_TYPE(10407, "twin class field type incorrect"),
    TWIN_CLASS_FIELD_VALUE_IS_ALREADY_IN_USE(10408, "twin class field value is already in use"),
    TWIN_CLASS_TAGS_NOT_ALLOWED(10409, "tags are not allowed for given class"),
    TWIN_CLASS_HIERARCHY_ERROR(10410, "something wrong with twin class hierarchy"),
    TWIN_CLASS_ID_UNKNOWN(10411, "unknown twin class id"),
    TWIN_CLASS_KEY_UNKNOWN(10412, "unknown twin class key"),
    TWIN_CLASS_KEY_ALREADY_IN_USE(10413, "twin class key is already in use"),
    TWIN_CLASS_KEY_INCORRECT(10414, "twin class key incorrect"),
    TWIN_CLASS_FIELD_KEY_INCORRECT(10415, "twin class field key is incorrect"),
    TWIN_CLASS_UPDATE_RESTRICTED(10416, "twin class can not be updated"),
    TWIN_CLASS_FIELD_UPDATE_RESTRICTED(10417, "twin class field can not be updated"),
    TWINFLOW_SCHEMA_NOT_ALLOWED(10501, "twinflow schema is not allowed"),
    TWINFLOW_SCHEMA_NOT_CONFIGURED(10502, "twinflow schema is not configured"),
    TWINFLOW_TRANSACTION_INCORRECT(10503, "twinflow transition can not be performed"),
    TWINFLOW_TRANSACTION_DENIED(10504, "twinflow transition is denied"),
    TWINFLOW_INIT_STATUS_INCORRECT(10505, "can't set initial status for twinflow: status not allowed for twin class"),
    TRANSITION_STATUS_INCORRECT(10506, "can't set status for transition"),
    DATALIST_OPTION_IS_NOT_VALID_FOR_LIST(10601, "data list option is not valid for current data list"),
    DATALIST_OPTION_IS_NOT_VALID_FOR_BUSINESS_ACCOUNT(10602, "data list option is not valid for current business account"),
    DATALIST_LIST_UNKNOWN(10603, "data list is unknown"),
    SPACE_TWIN_ID_INCORRECT(10701, "given twin id is not a space twin"),
    HEAD_TWIN_ID_NOT_ALLOWED(10702, "given twin is not allowed for given class"),
    HEAD_TWIN_NOT_SPECIFIED(10703, "space twin must be specified"),
    TWIN_ALIAS_UNKNOWN(10801, "alias is unknown"),
    UNSUPPORTED_ALIAS_TYPE(10802, "unsupported alias type"),
    TWIN_LINK_INCORRECT(10901, "twins can not be linkend"),
    TWIN_FIELD_VALUE_INCORRECT(10902, "twins field value can not be converted"),
    TWIN_BASIC_FIELD_UNKNOWN(10903, "unknown twin basic field"),
    FACTORY_INCORRECT(11001, "twin factory config is incorrect"),
    FACTORY_PIPELINE_STEP_ERROR(11002, "twin factory pipeline step error"),
    FACTORY_MULTIPLIER_ERROR(11003, "twin factory multiplier error"),
    TWIN_STATUS_INCORRECT(11101, "twin status is incorrect"),
    PAGINATION_ERROR(11201, "the offset must be a multiple of the size"),
    TWIN_COMMENT_FIELD_TEXT_IS_NULL(11301, "twin comment field is null"),
    TWIN_COMMENT_EDIT_ACCESS_DENIED(11302, "Comment editing access denied"),
    TWIN_ATTACHMENT_INCORRECT_COMMENT(11402, "This attachment belongs to another comment"),
    TWIN_ATTACHMENT_DELETE_ACCESS_DENIED(11403, "This attachment does not belong to the commenter"),
    TWIN_SEARCH_NOT_UNIQ(11601, "twin search can not be selected by permission"),
    TWIN_SEARCH_PARAM_MISSED(11602, "twin search params count incorrect"),
    TWIN_SEARCH_ALIAS_UNKNOWN(11603, "twin search alias unknown"),
    TWIN_SEARCH_CONFIG_INCORRECT(11604, "twin search config incorrect"),
    TWIN_SEARCH_PARAM_INCORRECT(11605, "twin search params incorrect"),
    SHOW_MODE_ACCESS_DENIED(11701, "show mode access denied"),
    TWIN_CREATE_ACCESS_DENIED(11801, "Twin can't be created by current user"),
    USER_GROUP_UNKNOWN(12101, "user group is unknown"),
    USER_GROUP_ENTER_ERROR(12102, "user group can not be entered"),
    USER_GROUP_IS_MANDATORY(12103, "user group is mandatory"),
    BUSINESS_ACCOUNT_UNKNOWN(12201, "unknown business_account"),
    BUSINESS_ACCOUNT_USER_ALREADY_EXISTS(12202, "business_account user already exists"),
    BUSINESS_ACCOUNT_USER_NOT_EXISTS(12203, "business_account user not exists");


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
