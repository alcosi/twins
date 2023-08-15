package org.twins.core.dao.twinlink;

import lombok.Data;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Data
@Table(name = "twinlink_tree_node")
public class TwinlinkTreeNodeEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID twinlinkTreeId;

    @Column(name = "depth")
    private int depth;

    @Column(name = "twinlink_id")
    private UUID twinlinkId;

    @ManyToOne
    @JoinColumn(name = "twinlink_tree_id", insertable = false, updatable = false, nullable = false)
    private TwinlinkTreeEntity twinlinkTree;

    @ManyToOne
    @JoinColumn(name = "twinlink_id", insertable = false, updatable = false, nullable = false)
    private TwinlinkEntity twinlink;
}
