package org.cambium.common.util;

import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;

public class CollectionUtils {
    public static List<String> singletonListOrNull(String string) {
        if (StringUtils.isNotBlank(string))
            return Collections.singletonList(string);
        else
            return null;
    }
}
