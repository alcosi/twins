package org.cambium.common.util;

public class InformationVolumeUtils {
    public static Double BYTE_IN_GB = 1073741824.0;

    public static String convertToGb(long bytes) {
        double gigabytes = bytes / BYTE_IN_GB;
        String result = String.format("%.3f", gigabytes).replace(",", ".");
        return result.replaceAll("0*$", "").replaceAll("\\.$", "");
    }
}
