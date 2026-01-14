package org.twins.core.dao.twin;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.enums.twin.Touch;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Table(name = "twin_touch")
@FieldNameConstants
public class TwinTouchEntity implements EasyLoggable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            this.id = UuidCreator.getTimeOrderedEpoch();
        }
    }

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

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false, nullable = false)
    private UserEntity user;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
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

}
