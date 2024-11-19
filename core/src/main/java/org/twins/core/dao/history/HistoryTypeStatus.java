package org.twins.core.dao.history;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum HistoryTypeStatus {
    hardDisabled("hardDisabled", true, true),
    hardEnabled("hardEnabled", true, false),
    softDisabled("softDisabled", false, true),
    softEnabled("softEnabled", false, false);

    final String id;
    final boolean blocker; // if true, then low level configs will be ignored
    final boolean disabled; // if true, then history won't write to db and won't read from it


    public static HistoryTypeStatus valueOd(String status) {
        return Arrays.stream(HistoryTypeStatus.values()).filter(t -> t.id.equals(status)).findAny().orElse(hardDisabled);
    }
}
