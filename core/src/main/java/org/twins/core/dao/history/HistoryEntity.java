package org.twins.core.dao.history;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;
import org.twins.core.dao.history.context.HistoryContext;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.user.UserEntity;

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
    @GeneratedValue(generator = "uuid")
    private UUID id;

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

    @ManyToOne
    @JoinColumn(name = "twin_id", insertable = false, updatable = false, nullable = false)
    private TwinEntity twin;

    @ManyToOne
    @JoinColumn(name = "twin_class_field_id", insertable = false, updatable = false, nullable = true)
    private TwinClassFieldEntity twinClassField;

    @ManyToOne
    @JoinColumn(name = "actor_user_id", insertable = false, updatable = false, nullable = false)
    private UserEntity actorUser;

    @Transient
    private String freshMessage;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "history[" + id + "]";
            default -> "history[id:" + id + ", twinId:" + twinId + "]";
        };
    }
}
