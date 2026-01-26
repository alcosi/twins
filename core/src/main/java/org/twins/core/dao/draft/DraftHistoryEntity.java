package org.twins.core.dao.draft;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.util.UuidUtils;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;
import org.twins.core.enums.history.HistoryType;
import org.twins.core.dao.history.HistoryTypeConverter;
import org.twins.core.dao.history.context.HistoryContext;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "draft_history")
@FieldNameConstants
@DynamicUpdate
public class DraftHistoryEntity implements EasyLoggable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "draft_id")
    private UUID draftId;

    @Column(name = "twin_id")
    private UUID twinId;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "actor_user_id")
    private UUID actorUserId;

    @Column(name = "history_type_id")
    @Convert(converter = HistoryTypeConverter.class)
    private HistoryType historyType;

    @Column(name = "twin_class_field_id")
    private UUID twinClassFieldId;

    @Type(JsonType.class)
    @Column(name = "context", columnDefinition = "jsonb")
    private HistoryContext context;

    @Column(name = "snapshot_message")
    private String snapshotMessage;
//
//    @ManyToOne
//    @JoinColumn(name = "twin_id", insertable = false, updatable = false, nullable = false)
//    private TwinEntity twin;

//    @ManyToOne
//    @JoinColumn(name = "actor_user_id", insertable = false, updatable = false, nullable = false)
//    private UserEntity actorUser;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "draftHistory[" + id + "]";
            default -> "draftHistory[id:" + id + ", twinId:" + twinId + "]";
        };
    }
}
