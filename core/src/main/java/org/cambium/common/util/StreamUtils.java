package org.cambium.common.util;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

@Slf4j
public class StreamUtils {

    public static <T> Predicate<T> andLogFilteredOutValues(Predicate<T> predicate, String logMessage) {
        return value -> {
            if (predicate.test(value)) {
                return true;
            } else {
                log.warn(logMessage);
                return false;
            }
        };
    }

    public static boolean andLogFilteredOutValues(boolean test, String logMessage) {
        if (test) {
            return true;
        } else {
            log.warn(logMessage);
            return false;
        }
    }


    public static <T> Predicate<T> distinctByKey(
            Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }
}
