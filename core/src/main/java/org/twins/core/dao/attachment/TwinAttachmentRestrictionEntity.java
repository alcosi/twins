package org.twins.core.dao.attachment;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;

import java.util.UUID;

@Entity
@Data
@FieldNameConstants
@Accessors(chain = true)
@Table(name = "twin_attachment_restriction")
public class TwinAttachmentRestrictionEntity implements EasyLoggable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            this.id = UUID.randomUUID();
        }
    }

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "min_count")
    private Integer minCount;

    @Column(name = "max_count")
    private Integer maxCount;

    @Column(name = "file_size_mb_limit")
    private Integer fileSizeMbLimit;

    @Column(name = "file_extension_list")
    private String fileExtensionLimit;

    @Column(name = "file_name_regexp")
    private String fileNameRegexp;

    @Override
    public String easyLog(Level level) {
        return "twinAttachmentRestriction[" + id + "]";
    }
}
