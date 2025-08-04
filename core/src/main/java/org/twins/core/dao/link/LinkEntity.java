package org.twins.core.dao.link;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.featurer.annotations.FeaturerList;
import org.cambium.featurer.dao.FeaturerEntity;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.featurer.linker.Linker;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.UUID;

@Data
@FieldNameConstants
@Accessors(chain = true)
@Entity
@Table(name = "link")
public class LinkEntity implements EasyLoggable {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "src_twin_class_id")
    private UUID srcTwinClassId;

    @Column(name = "dst_twin_class_id")
    private UUID dstTwinClassId;

    @Column(name = "forward_name_i18n_id")
    private UUID forwardNameI18NId;

    @Column(name = "backward_name_i18n_id")
    private UUID backwardNameI18NId;

    @Column(name = "link_type_id")
    @Enumerated(EnumType.STRING)
    private TwinlinkType type;

    @Column(name = "link_strength_id")
    @Enumerated(EnumType.STRING)
    private LinkStrength linkStrengthId;

    @Column(name = "created_by_user_id")
    private UUID createdByUserId;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Timestamp createdAt;

//    @ManyToOne
//    @JoinColumn(name = "domain_id", insertable = false, updatable = false)
//    private DomainEntity domain;

    @ManyToOne
    @JoinColumn(name = "src_twin_class_id", insertable = false, updatable = false, nullable = false)
    private TwinClassEntity srcTwinClass;

    @ManyToOne
    @JoinColumn(name = "dst_twin_class_id", insertable = false, updatable = false, nullable = false)
    private TwinClassEntity dstTwinClass;

    @Transient
    @EqualsAndHashCode.Exclude
    private UserEntity createdByUser;

    @Deprecated //for specification only
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "forward_name_i18n_id", insertable = false, updatable = false)
    private I18nEntity forwardNameI18n;

    @Deprecated //for specification only
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "backward_name_i18n_id", insertable = false, updatable = false)
    private I18nEntity backwardNameI18n;

    @Column(name = "linker_featurer_id")
    private Integer linkerFeaturerId;

    @FeaturerList(type = Linker.class)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "linker_featurer_id", insertable = false, updatable = false)
    private FeaturerEntity linkerFeaturer;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "linker_params", columnDefinition = "hstore")
    private HashMap<String, String> linkerParams;

    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "link[" + id + "]";
            default -> "link[id:" + id + ", srcTwinClassId:" + srcTwinClassId + "], dstTwinClassId:" + dstTwinClassId + "]";
        };

    }

    @Getter
    public enum TwinlinkType {
        ManyToOne(true, true, false),
        ManyToMany(true, false, false),
        OneToOne(false, true, true);

        private final boolean many;
        private final boolean uniqForSrcTwin;
        private final boolean uniqForDstTwin;

        TwinlinkType(boolean many, boolean uniqForSrcTwin, boolean uniqForDstTwin) {
            this.many = many;
            this.uniqForSrcTwin = uniqForSrcTwin;
            this.uniqForDstTwin = uniqForDstTwin;
        }
    }
}
