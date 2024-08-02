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
@Table(name = "draft_twin_link")
public class DraftTwinLinkEntity {
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

    @Column(name = "twin_link_id")
    private UUID twinLinkId;

    @Column(name = "src_twin_id")
    private UUID srcTwinId;

    @Column(name = "dst_twin_id")
    private UUID dstTwinId;

    @Column(name = "link_id")
    private UUID linkId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "draft_id", insertable = false, updatable = false)
    private DraftEntity draft;
}