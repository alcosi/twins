package org.twins.core.featurer.fieldtyper.descriptor;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
public class FieldDescriptorAttachment extends FieldDescriptor {
    private Integer minCount;
    private Integer maxCount;
    private String extensions;
    private String filenameRegExp;
    private Integer fileSizeMbLimit;
}

