package org.twins.core.dao.comment;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;
import org.twins.core.enums.comment.TwinCommentAction;

import java.util.UUID;

@Entity
@Data
@Table(name = "twin_comment_action_self")
public class TwinCommentActionSelfEntity {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            this.id = UuidCreator.getTimeOrderedEpoch();
        }
    }

    @Column(name = "twin_class_id")
    private UUID twinClassId;

    @Column(name = "restrict_twin_comment_action_id")
    @Enumerated(EnumType.STRING)
    private TwinCommentAction restrictTwinCommentAction;
}
