package org.cambium.featurer;

public class FeaturerProperties extends java.util.Properties {
    public FeaturerProperties() {
    }

    public boolean isTrue(String key) {
        String val = this.getProperty(key, "false").trim();
        return val.equalsIgnoreCase("on") || val.equalsIgnoreCase("yes") || val.equalsIgnoreCase("true");
    }

    public boolean isFalse(String key) {
        String val = this.getProperty(key, "true").trim();
        return val.equalsIgnoreCase("off") || val.equalsIgnoreCase("no") || val.equalsIgnoreCase("false");
    }

    public boolean isOn(String key) {
        return this.isTrue(key);
    }

    public boolean isOff(String key) {
        return this.isFalse(key);
    }

    public int getPropertyInt(String key, int ddefault) {
        try {
            return Integer.parseInt(this.getProperty(key));
        } catch (Exception var4) {
            return ddefault;
        }
    }
}