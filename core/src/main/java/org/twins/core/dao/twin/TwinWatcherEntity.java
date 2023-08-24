package org.twins.core.dao.twin;

import jakarta.persistence.*;
import lombok.Data;
import org.twins.core.dao.user.UserEntity;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@Table(name = "twin_watcher")
public class TwinWatcherEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "twin_id")
    private UUID twinId;

    @Column(name = "watcher_user_id")
    private UUID watcherUserId;

    @Column(name = "added_at")
    private Timestamp addedAt;

    @ManyToOne
    @JoinColumn(name = "twin_id", insertable = false, updatable = false, nullable = false)
    private TwinEntity twinByTwinId;

    @ManyToOne
    @JoinColumn(name = "watcher_user_id", insertable = false, updatable = false, nullable = false)
    private UserEntity watcherUser;
}
