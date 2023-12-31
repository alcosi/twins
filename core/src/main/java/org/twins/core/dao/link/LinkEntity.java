package org.twins.core.dao.link;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.cambium.common.EasyLoggable;
import org.cambium.common.EasyLoggableImpl;
import org.cambium.i18n.dao.I18nEntity;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "link")
public class LinkEntity extends EasyLoggableImpl {
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

    @Column(name = "mandatory")
    private boolean mandatory;

    @Column(name = "created_by_user_id")
    private UUID createdByUserId;

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

//    @ManyToOne
//    @JoinColumn(name = "forward_name_i18n_id", insertable = false, updatable = false, nullable = false)
//    private I18nEntity forwardNameI18n;

//    @ManyToOne
//    @JoinColumn(name = "backward_name_i18n_id", insertable = false, updatable = false, nullable = false)
//    private I18nEntity backwardNameI18n;

    public String easyLog(Level level) {
        switch (level) {
            case SHORT:
                return "link[" + id + "]";
            default:
                return "link[id:" + id + ", srcTwinClassId:" + srcTwinClassId + "], dstTwinClassId:" + dstTwinClassId + "]";
        }

    }

    @Getter
    public enum TwinlinkType {
        ManyToOne(true, true),
        ManyToMany(true, false),
        OneToOne(false, true);

        private final boolean many;
        private final boolean uniqForSrcTwin;

        TwinlinkType(boolean many, boolean uniqForSrcTwin) {
            this.many = many;
            this.uniqForSrcTwin = uniqForSrcTwin;
        }
    }
}
