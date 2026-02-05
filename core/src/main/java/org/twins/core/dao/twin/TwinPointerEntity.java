package org.twins.core.dao.twin;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;
import org.hibernate.annotations.Type;
import org.twins.core.dao.domain.DomainVersionEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.versioning.DomainSetting;

import java.util.HashMap;
import java.util.UUID;

@Data
@Entity
@Accessors(chain = true)
@DomainSetting
@Table(name = "twin_pointer")
public class TwinPointerEntity implements EasyLoggable {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "twin_class_id")
    private UUID twinClassId;

    @Column(name = "domain_version_id")
    private UUID domainVersionId;

    @Column(name = "pointer_featurer_id")
    private Integer pointerFeaturerId;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "pointer_params")
    private HashMap<String, String> pointerParams;

    @Column(name = "name")
    private String name;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_class_id", insertable = false, updatable = false)
    private TwinClassEntity twinClass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_version_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private DomainVersionEntity domainVersion;

    @Override
    public String easyLog(Level level) {
        switch (level) {
            case SHORT:
                return "twinPointer[" + id + "]";
            default:
                return "twinPointer[id:" + id + ", twinClassId:" + twinClassId + "]";
        }
    }
}
