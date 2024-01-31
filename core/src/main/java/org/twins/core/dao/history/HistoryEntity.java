package org.twins.core.dao.history;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggableImpl;
import org.hibernate.annotations.Type;
import org.twins.core.dao.history.context.HistoryContext;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.user.UserEntity;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Table(name = "history")
@FieldNameConstants
public class HistoryEntity extends EasyLoggableImpl {
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

    @Type(JsonType.class)
    @Column(name = "context", columnDefinition = "jsonb")
    private HistoryContext context;

    @Column(name = "snapshot_message")
    private String snapshotMessage;

    @ManyToOne
    @JoinColumn(name = "twin_id", insertable = false, updatable = false, nullable = false)
    private TwinEntity twin;

    @ManyToOne
    @JoinColumn(name = "actor_user_id", insertable = false, updatable = false, nullable = false)
    private UserEntity actorUser;

    @Override
    public String easyLog(Level level) {
        switch (level) {
            case SHORT:
                return "history[" + id + "]";
            default:
                return "history[id:" + id + ", twinId:" + twinId + "]";
        }
    }
}