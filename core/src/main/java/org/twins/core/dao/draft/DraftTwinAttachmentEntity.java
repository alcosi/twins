package org.twins.core.dao.draft;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Cascade;
import org.twins.core.dao.CUD;
import org.twins.core.dao.CUDConverter;

import java.util.Map;
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

    @Column(name = "time_in_millis")
    private long timeInMillis;

    @Column(name = "cud_id")
    @Convert(converter = CUDConverter.class)
    private CUD cud;

    @Column(name = "twin_attachment_id")
    private UUID twinAttachmentId;

    //we can not create @ManyToOne relation, because it can be new twin here, which is not in twin table yet
    @Column(name = "twin_id")
    private UUID twinId;

    @Column(name = "twinflow_transition_id")
    private UUID twinflowTransitionId;

    @Column(name = "storage_link")
    private String storageLink;

    @ElementCollection
    @CollectionTable(
            name = "draft_twin_attachment_modification_links",
            joinColumns = @JoinColumn(name = "draft_twin_attachment_id"),
            foreignKey = @ForeignKey(name = "FK_draft_twin_attachment_mod_links_draft_twin_attachment_id")
    )
    @MapKeyColumn(name = "mod_key")
    @Column(name = "mod_link")
    @Cascade(org.hibernate.annotations.CascadeType.ALL)
    private Map<String, String> modificationLinks;

    @Column(name = "created_by_user_id")
    private UUID createdByUserId;

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

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "draft_id", insertable = false, updatable = false)
    private DraftEntity draft;

}
