package org.twins.core.dao.twin;

import lombok.Data;

import jakarta.persistence.*;
import org.twins.core.dao.user.UserEntity;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@Table(name = "twin_work")
public class TwinWorkEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "twin_id")
    private UUID twinId;

    @Column(name = "logged_at")
    private Timestamp loggedAt;

    @Column(name = "author_user_id")
    private UUID authorUserId;

    @Column(name = "minutes_spent")
    private int minutesSpent;

    @ManyToOne
    @JoinColumn(name = "twin_id", insertable = false, updatable = false, nullable = false)
    private TwinEntity twinByTwinId;

    @ManyToOne
    @JoinColumn(name = "author_user_id", insertable = false, updatable = false, nullable = false)
    private UserEntity authorUser;
}
