package org.twins.core.dao.twin;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggableImpl;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Table(name = "twin_starred")
public class TwinStarredEntity extends EasyLoggableImpl {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "twin_id")
    private UUID twinId;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Timestamp createdAt;

    @Override
    public String easyLog(Level level) {
        switch (level) {
            default:
                return "TwinStarred{" + id + ", userId:" + userId + ", twinId:" + twinId + "}";
        }
    }
}
