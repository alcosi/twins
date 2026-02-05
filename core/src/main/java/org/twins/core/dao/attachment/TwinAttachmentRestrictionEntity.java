package org.twins.core.dao.attachment;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.util.UuidUtils;
import org.twins.core.dao.domain.DomainVersionEntity;
import org.twins.core.domain.versioning.DomainSetting;

import java.util.UUID;

@Entity
@Data
@FieldNameConstants
@Accessors(chain = true)
@DomainSetting
@Table(name = "twin_attachment_restriction")
public class TwinAttachmentRestrictionEntity implements EasyLoggable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "domain_version_id")
    private UUID domainVersionId;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_version_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private DomainVersionEntity domainVersion;

    @Override
    public String easyLog(Level level) {
        return "twinAttachmentRestriction[" + id + "]";
    }
}
