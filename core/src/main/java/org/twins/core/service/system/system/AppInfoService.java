package org.twins.core.service.system.system;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class AppInfoService {

    public static final String BUILD_REVISION = "Build-Revision";
    public static final String BUILD_JDK = "Build-Jdk";
    public static final String BUILD_TIMESTAMP = "Build-Timestamp";
    public static final String IMPLEMENTATION_VERSION = "Implementation-Version";
    public static final String BUILD_DISPLAY = "Build-Display";
    public static final String BUILD_TAG = "Build-Tag";

    private static final List<String> infoKeys = new ArrayList<>(){{
        add(BUILD_REVISION);
        add(BUILD_JDK);
        add(BUILD_TIMESTAMP);
        add(BUILD_DISPLAY);
        add(BUILD_TAG);
        add(IMPLEMENTATION_VERSION);
    }};

    public static Map<String, String> ATTRIBUTES = new HashMap<>();

    public Map<String, String> getManifestAttributes() throws Exception {
        if(!ATTRIBUTES.isEmpty() && !ATTRIBUTES.containsKey("error")) return ATTRIBUTES;
        else {
            InputStream manifestStream = getClass().getClassLoader().getResourceAsStream("META-INF/MANIFEST.MF");
            if (manifestStream != null) {
                Manifest manifest = new Manifest(manifestStream);
                Attributes attrs = manifest.getMainAttributes();
                for(String key : infoKeys) ATTRIBUTES.put(key, attrs.getValue(key));
            } else {
                ATTRIBUTES.put("error", "MANIFEST.MF is not available");
            }
            return ATTRIBUTES;
        }
    }
}
