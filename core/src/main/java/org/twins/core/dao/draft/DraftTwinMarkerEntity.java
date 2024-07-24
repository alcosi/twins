package org.twins.core.dao.draft;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Entity
@Table(name = "draft_twin_marker")
public class DraftTwinMarkerEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    @Column(name = "id")
    private UUID id;

    @Column(name = "draft_id")
    private UUID draftId;

    @Column(name = "twin_id")
    private UUID twinId;

    @Column(name = "create_else_delete")
    private boolean createElseDelete = false;

    @Column(name = "marker_data_list_option_id")
    private UUID markerDataListOptionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "draft_id")
    private DraftEntity draft;

}