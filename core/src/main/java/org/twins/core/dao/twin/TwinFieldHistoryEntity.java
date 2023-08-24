package org.twins.core.dao.twin;

import jakarta.persistence.*;
import lombok.Data;
import org.twins.core.dao.user.UserEntity;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@Table(name = "twin_field_history")
public class TwinFieldHistoryEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "twin_field_id")
    private UUID twinFieldId;

    @Column(name = "old_value")
    private String oldValue;

    @Column(name = "new_value")
    private String newValue;

    @Column(name = "author_user_id")
    private UUID authorUserId;

    @Column(name = "changed_at")
    private Timestamp changedAt;

    @ManyToOne
    @JoinColumn(name = "twin_field_id", insertable = false, updatable = false, nullable = false)
    private TwinFieldEntity twinField;

    @ManyToOne
    @JoinColumn(name = "author_user_id", insertable = false, updatable = false, nullable = false)
    private UserEntity authorUser;
}
