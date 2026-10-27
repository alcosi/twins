package org.twins.core.dao.link;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.util.UuidUtils;
import org.cambium.featurer.dao.FeaturerEntity;
import org.hibernate.annotations.Type;
import org.twins.core.domain.Identifiable;

import java.util.HashMap;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "link_validator")
@Accessors(chain = true)
@FieldNameConstants
public class LinkValidatorEntity implements EasyLoggable, Identifiable<UUID> {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "link_id", nullable = false)
    private UUID linkId;

    @Column(name = "`order`", nullable = false)
    private Integer order;

    @Column(name = "linker_featurer_id", nullable = false)
    private Integer linkerFeaturerId;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "linker_params", columnDefinition = "hstore")
    private HashMap<String, String> linkerParams;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "link_id", insertable = false, updatable = false)
    private LinkEntity linkSpecOnly;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "linker_featurer_id", insertable = false, updatable = false)
    private FeaturerEntity linkerFeaturerSpecOnly;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private LinkEntity link;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "linkValidator[" + id + "]";
            case NORMAL -> "linkValidator[id:" + id + ", linkId:" + linkId + "]";
            default -> "linkValidator[id:" + id
                    + ", linkId:" + linkId
                    + ", linkerFeaturerId:" + linkerFeaturerId
                    + ", linkerParams:" + linkerParams + "]";
        };
    }
}
