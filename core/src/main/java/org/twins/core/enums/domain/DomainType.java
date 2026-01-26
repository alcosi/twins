package org.twins.core.enums.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum DomainType {
    basic("basic"),
    b2b("b2b");

    final String id;

    public static DomainType valueOd(String type) {
        return Arrays.stream(DomainType.values()).filter(t -> t.id.equals(type)).findAny().orElse(basic);
    }
}
