package org.twins.core.dao.card;

import jakarta.persistence.*;
import lombok.Data;
import org.cambium.i18n.dao.I18nEntity;
import org.twins.core.dao.Channel;

import java.util.UUID;

@Entity
@Data
@Table(name = "card_override")
public class CardOverrideEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "override_card_id")
    private UUID overrideCardId;

    @Column(name = "override_for_channel_id")
    @Enumerated(EnumType.STRING)
    private Channel overrideForChannel;

    @Column(name = "override_eclipse")
    private Boolean overrideEclipse;

    @Column(name = "logo")
    private String logo;

    @Column(name = "name_i18n_id")
    private UUID nameI18NId;

    @Column(name = "card_layout_id")
    private UUID cardLayoutId;

    @ManyToOne
    @JoinColumn(name = "override_card_id", insertable = false, updatable = false, nullable = false)
    private CardEntity overrideCard;

    @ManyToOne
    @JoinColumn(name = "name_i18n_id", insertable = false, updatable = false)
    private I18nEntity nameI18n;

    @ManyToOne
    @JoinColumn(name = "card_layout_id", insertable = false, updatable = false)
    private CardLayoutEntity cardLayout;
}
