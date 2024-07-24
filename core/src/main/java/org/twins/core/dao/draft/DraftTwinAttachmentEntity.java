package org.twins.core.dao.draft;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.CUD;
import org.twins.core.dao.CUDConverter;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Entity
@Table(name = "draft_twin_attachment")
public class DraftTwinAttachmentEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    @Column(name = "id")
    private UUID id;

    @Column(name = "draft_id")
    private UUID draftId;

    @Column(name = "cud_id")
    @Convert(converter = CUDConverter.class)
    private CUD cud;
    
    @Column(name = "twin_attachment_id")
    private UUID twinAttachmentId;

    @Column(name = "twin_id")
    private UUID twinId;

    @Column(name = "twinflow_transition_id")
    private UUID twinflowTransitionId;

    @Column(name = "storage_link")
    private String storageLink;

    @Column(name = "view_permission_id")
    private UUID viewPermissionId;

    @Column(name = "external_id")
    private String externalId;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "twin_comment_id")
    private UUID twinCommentId;

    @Column(name = "twin_class_field_id")
    private UUID twinClassFieldId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "draft_id")
    private DraftEntity draft;

}