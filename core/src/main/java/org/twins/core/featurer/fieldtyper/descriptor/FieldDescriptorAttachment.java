package org.twins.core.featurer.fieldtyper.descriptor;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
public class FieldDescriptorAttachment extends FieldDescriptor {
    private Integer minCount;
    private Integer maxCount;
    private List<String> extensions;
    private String filenameRegExp;
    private Integer fileSizeMbLimit;
}
