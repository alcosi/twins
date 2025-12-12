package org.twins.core.dao.resource;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.hibernate.annotations.CreationTimestamp;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.user.UserEntity;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@FieldNameConstants
@Table(name = "resource")
public class ResourceEntity implements EasyLoggable {
    @Id
    private UUID id;

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "uploaded_by_user_id")
    private UUID uploadedByUserId;

    @Column(name = "original_file_name")
    private String originalFileName;

    @Column(name = "size_in_bytes")
    private Long sizeInBytes;

    @Column(name = "storage_file_key")
    private String storageFileKey;

    @Column(name = "storage_id")
    private UUID storageId;

    @CreationTimestamp
    @Column(name = "created_at")
    private Timestamp createdAt;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_id", insertable = false, updatable = false)
    private DomainEntity domain;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "storage_id", insertable = false, updatable = false)
    private StorageEntity storage;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_user_id", insertable = false, updatable = false)
    private UserEntity uploadedByUser;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "resource[id:" + id + ", domainId:" + domainId + "]";
            case NORMAL, DETAILED ->
                    "resource[id:" + id + ", domainId:" + domainId + ", originalFileName:" + originalFileName + "]";
        };
    }


}
