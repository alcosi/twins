package org.twins.core.dao.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.util.UuidUtils;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Table(name = "domain_version")
@Data
@FieldNameConstants
@Accessors(chain = true)
public class DomainVersionEntity implements EasyLoggable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "version")
    private String version;

    @Column(name = "name")
    private String name;

    @Column(name = "hash")
    private String hash;

    @Column(name = "json_file")
    private String jsonFile;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "released_at")
    private Timestamp releasedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private DomainEntity domain;

    public String easyLog(Level level) {
        return "domainVersion[id:" + id + ", version:" + version + "]";
    }
}
