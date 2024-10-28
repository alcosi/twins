
package org.twins.core.dao.twin;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(name = "twin_attachment_action_self")
public class TwinAttachmentActionSelfEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "twin_class_id")
    private UUID twinClassId;

    @Column(name = "restrict_twin_attachment_action_id")
    @Enumerated(EnumType.STRING)
    private TwinAttachmentAction restrictTwinAttachmentAction;

    @Column(name = "twin_validator_set_id")
    private UUID twinValidatorSetId;
}
