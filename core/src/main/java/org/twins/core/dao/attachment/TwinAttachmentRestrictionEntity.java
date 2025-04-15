package org.twins.core.dao.attachment;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import java.util.UUID;

@Entity
@Data
@FieldNameConstants
@Accessors(chain = true)
@Table(name = "twin_attachment_restriction")
public class TwinAttachmentRestrictionEntity {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            this.id = UUID.randomUUID();
        }
    }

    @Column(name = "min_count")
    private int minCount;

    @Column(name = "max_count")
    private int maxCount;

    @Column(name = "file_size_mb_limit")
    private int fileSizeMbLimit;

    @Column(name = "file_extension_list")
    private String fileExtensionLimit;

    @Column(name = "file_name_regexp")
    private String fileNameRegexp;
}
