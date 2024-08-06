package org.twins.core.domain;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@RequiredArgsConstructor
@Accessors(chain = true)
public class TwinBasicFields {

    private UUID assigneeUserId;
    private UUID createdByUserId;
    private String name;
    private String description;

    public enum Basics {
        assigneeUserId,
        createdByUserId,
        name,
        description
    }

}
