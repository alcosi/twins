package org.twins.core.dao.view;

import lombok.Data;

import jakarta.persistence.*;
import org.cambium.i18n.dao.I18nEntity;
import org.twins.core.dao.Channel;

import java.util.UUID;

@Entity
@Data
@Table(name = "view_tab_override")
public class ViewTabOverrideEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "override_view_tab_id")
    private UUID overrideViewTabId;

    @Column(name = "override_for_channel_id")
    @Enumerated(EnumType.STRING)
    private Channel overrideForChannel;

    @Column(name = "override_eclipse")
    private Boolean overrideEclipse;

    @Column(name = "logo")
    private String logo;

    @Column(name = "name_i18n_id")
    private UUID nameI18NId;

    @Column(name = "order")
    private Integer order;

    @Column(name = "view_tab_layout_id")
    private UUID viewTabLayoutId;

    @ManyToOne
    @JoinColumn(name = "override_view_tab_id", insertable = false, updatable = false, nullable = false)
    private ViewTabEntity overrideViewTab;

    @ManyToOne
    @JoinColumn(name = "name_i18n_id", insertable = false, updatable = false)
    private I18nEntity i18NByNameI18NId;

    @ManyToOne
    @JoinColumn(name = "view_tab_layout_id", insertable = false, updatable = false)
    private ViewTabLayoutEntity viewTabLayout;
}
