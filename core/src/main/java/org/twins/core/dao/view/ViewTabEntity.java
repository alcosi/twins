package org.twins.core.dao.view;

import jakarta.persistence.*;
import lombok.Data;
import org.cambium.i18n.dao.I18nEntity;

import java.util.UUID;

@Entity
@Data
@Table(name = "view_tab")
public class ViewTabEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "view_id")
    private UUID viewId;

    @Column(name = "logo")
    private String logo;

    @Column(name = "order")
    private Integer order;

    @Column(name = "view_tab_layout_id")
    private UUID viewTabLayoutId;

    @Column(name = "key")
    private String key;

    @Column(name = "name_i18n_id")
    private UUID nameI18NId;

    @ManyToOne
    @JoinColumn(name = "view_id", insertable = false, updatable = false, nullable = false)
    private ViewEntity view;

    @ManyToOne
    @JoinColumn(name = "view_tab_layout_id", insertable = false, updatable = false, nullable = false)
    private ViewTabLayoutEntity viewTabLayout;

    @ManyToOne
    @JoinColumn(name = "name_i18n_id", insertable = false, updatable = false)
    private I18nEntity i18NByNameI18NId;
}
