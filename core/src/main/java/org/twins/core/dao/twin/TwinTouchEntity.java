package org.twins.core.dao.twin;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.hibernate.annotations.CreationTimestamp;
import org.twins.core.dao.user.UserEntity;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.UUID;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Table(name = "twin_touch")
@FieldNameConstants
public class TwinTouchEntity implements EasyLoggable {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "twin_id")
    private UUID twinId;

    @Column(name = "touch_id")
    @Enumerated(EnumType.STRING)
    private Touch touchId;

    @Column(name = "user_id")
    private UUID userId;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Timestamp createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false, nullable = false)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "twin_id", insertable = false, updatable = false, nullable = false)
    private TwinEntity twin;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinTouch[" + id + "]";
            default -> "twinTouch[" + id + ", twinId:" + twinId + ", touchId:" + touchId + ", userId:" + userId + "]";
        };
    }

    @Getter
    public enum Touch {
        WATCHED("WATCHED"),
        STARRED("STARRED"),
        REVIEWED("REVIEWED");

        private final String id;

        Touch(String id) {
            this.id = id;
        }

        public static Touch valueOfId(String type) {
            return Arrays.stream(Touch.values()).filter(t -> t.id.equalsIgnoreCase(type)).findAny().orElseThrow();
        }
    }
}
