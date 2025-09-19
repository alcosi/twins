package org.twins.core.enums.twinclass;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum OwnerType {
    SYSTEM("system", false, false, false),
    USER("user", false, false, true),
    BUSINESS_ACCOUNT("businessAccount", true, false, false),
    DOMAIN("domain", false, true, false),
    DOMAIN_BUSINESS_ACCOUNT("domainBusinessAccount", true, true, false),
    DOMAIN_USER("domainUser", false, true, true),
    DOMAIN_BUSINESS_ACCOUNT_USER("domainBusinessAccountUser", true, true, true);

    private final String id;
    private final boolean businessAccountLevel;
    private final boolean domainLevel;
    private final boolean userLevel;

    OwnerType(String id, boolean businessAccountLevel, boolean domainLevel, boolean userLevel) {
        this.id = id;
        this.businessAccountLevel = businessAccountLevel;
        this.domainLevel = domainLevel;
        this.userLevel = userLevel;
    }

    public static OwnerType valueOd(String type) {
        return Arrays.stream(OwnerType.values()).filter(t -> t.id.equals(type)).findAny().orElse(DOMAIN_BUSINESS_ACCOUNT);
    }

    public boolean isSystemLevel() {
        return this == SYSTEM;
    }
}
