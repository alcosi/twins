package org.twins.core.dao.link;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Data
@Table(name = "link_tree_node")
public class LinkTreeNodeEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "link_tree_id")
    private UUID linkTreeId;

    @Column(name = "depth")
    private int depth;

    @Column(name = "link_id")
    private UUID linkId;

    @ManyToOne
    @JoinColumn(name = "link_tree_id", insertable = false, updatable = false, nullable = false)
    private LinkTreeEntity linkTree;

    @ManyToOne
    @JoinColumn(name = "link_id", insertable = false, updatable = false, nullable = false)
    private LinkEntity link;
}
