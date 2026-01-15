package org.twins.core.dao.eraseflow;

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
@Data
@Accessors(chain = true)
@Table(name = "eraseflow_link_cascade")
@FieldNameConstants
public class EraseflowLinkCascadeEntity implements EasyLoggable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "eraseflow_id")
    private UUID eraseflowId;

    @Column(name = "link_id")
    private UUID linkId;

    @Column(name = "created_by_user_id")
    private UUID createdByUserId;

    @Column(name = "cascade_deletion_factory_id")
    private UUID cascadeDeletionFactoryId;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "description")
    private String description;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "eraseflow_id", insertable = false, updatable = false, nullable = false)
    private EraseflowEntity eraseflow;

    @Override
    public String easyLog(Level level) {
        return "eraseflowLinkCasace[id:" + id + "]";
    }
}
