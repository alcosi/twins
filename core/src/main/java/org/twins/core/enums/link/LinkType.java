package org.twins.core.enums.link;

import lombok.Getter;

@Getter
public enum LinkType {
    ManyToOne(true, true, false),
    ManyToMany(true, false, false),
    OneToOne(false, true, true);

    private final boolean many;
    private final boolean uniqForSrcTwin;
    private final boolean uniqForDstTwin;

    LinkType(boolean many, boolean uniqForSrcTwin, boolean uniqForDstTwin) {
        this.many = many;
        this.uniqForSrcTwin = uniqForSrcTwin;
        this.uniqForDstTwin = uniqForDstTwin;
    }
}
