package org.twins.core.dao.history;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.util.UuidUtils;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;
import org.twins.core.dao.history.context.HistoryContext;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.enums.history.HistoryType;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "history")
@FieldNameConstants
@DynamicUpdate
public class HistoryEntity implements EasyLoggable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "twin_id")
    private UUID twinId;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "actor_user_id")
    private UUID actorUserId;

    @Column(name = "history_type_id")
    @Convert(converter = HistoryTypeConverter.class)
    private HistoryType historyType;

    @Column(name = "history_batch_id")
    private UUID historyBatchId;

    @Column(name = "twin_class_field_id")
    private UUID twinClassFieldId;

    @Type(JsonType.class)
    @Column(name = "context", columnDefinition = "jsonb")
    private HistoryContext context;

    @Column(name = "snapshot_message")
    private String snapshotMessage;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "twin_id", insertable = false, updatable = false, nullable = false)
    private TwinEntity twin;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "actor_user_id", insertable = false, updatable = false, nullable = false)
    private UserEntity actorUser;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "history[" + id + "]";
            default -> "history[id:" + id + ", twinId:" + twinId + "]";
        };
    }
}
