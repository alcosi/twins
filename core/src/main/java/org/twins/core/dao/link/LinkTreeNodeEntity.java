package org.twins.core.dao.link;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Data
@Table(name = "link_tree_node")
public class LinkTreeNodeEntity {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            this.id = UuidCreator.getTimeOrderedEpoch();
        }
    }

    @Column(name = "link_tree_id")
    private UUID linkTreeId;

    @Column(name = "depth")
    private int depth;

    @Column(name = "link_id")
    private UUID linkId;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "link_tree_id", insertable = false, updatable = false, nullable = false)
    private LinkTreeEntity linkTree;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "link_id", insertable = false, updatable = false, nullable = false)
    private LinkEntity link;
}
