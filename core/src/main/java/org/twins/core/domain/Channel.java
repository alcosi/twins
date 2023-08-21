package org.twins.core.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;

import java.util.Arrays;

@RequiredArgsConstructor
public enum Channel {
    WEB(0, "WEB"),
    ANDROID_GOOGLE_MOBILE(1, "ANDROID"),
    ANDROID_HUAWEI_MOBILE(3, "HUAWEI ANDROID"),
    IOS_MOBILE(2, "iOS"),
    UNKNOWN(-1, "UNKNOWN"),
    ANY(100,  "UNKNOWN");

    public final Integer id;
    public final String description;
    private static final StringToChannelEnumConverter stringConverter = new StringToChannelEnumConverter();

    public static Channel resolve(String s) {
        return stringConverter.convert(s);
    }

    public static Channel resolve(Integer s) {
        return stringConverter.convert(s + "");
    }

    public boolean isWeb() {
        switch (this) {
            case WEB:
            case ANY:
                return true;
            default:
                return false;
        }
    }

    public boolean isMobile() {
        switch (this) {
            case ANDROID_GOOGLE_MOBILE:
            case ANDROID_HUAWEI_MOBILE:
            case IOS_MOBILE:
            case ANY:
                return true;
            default:
                return false;
        }
    }

    public boolean isMobileAndroid() {
        switch (this) {
            case ANDROID_GOOGLE_MOBILE:
            case ANDROID_HUAWEI_MOBILE:
            case ANY:
                return true;
            default:
                return false;
        }
    }

    public boolean isMobileIOS() {
        switch (this) {
            case IOS_MOBILE:
            case ANY:
                return true;
            default:
                return false;
        }
    }

    public static class StringToChannelEnumConverter implements Converter<String, Channel> {
        @Override
        public Channel convert(String source) {
            if (source == null) {
                return UNKNOWN;
            } else if ("WEB".equalsIgnoreCase(source)) {
                return WEB;
            } else if ("MOBILE".equalsIgnoreCase(source)) {
                return ANDROID_GOOGLE_MOBILE;
            } else {
                boolean isNumeric = source.chars().allMatch(Character::isDigit);
                if (isNumeric) {
                    return Arrays.stream(Channel.values()).filter(c -> c.id.equals(Integer.parseInt(source))).findFirst().orElse(UNKNOWN);
                } else {
                    return Arrays.stream(Channel.values()).filter(c -> c.name().equalsIgnoreCase(source)).findFirst().orElse(UNKNOWN);
                }
            }
        }
    }
}
