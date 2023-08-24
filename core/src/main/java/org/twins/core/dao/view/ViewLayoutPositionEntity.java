package org.twins.core.dao.view;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Data
@Table(name = "view_layout_position")
public class ViewLayoutPositionEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "view_tab_layout_id")
    private UUID viewTabLayoutId;

    @Column(name = "key")
    private String key;

    @ManyToOne
    @JoinColumn(name = "view_tab_layout_id", insertable = false, updatable = false, nullable = false)
    private ViewTabLayoutEntity viewTabLayoutByViewTabLayoutId;
}
