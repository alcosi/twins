package org.twins.core.dao.card;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cambium.common.util.UuidUtils;
import org.twins.core.dao.Channel;

import java.util.UUID;

@Entity
@Data
@Table(name = "card_override")
public class CardOverrideEntity {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

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
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private CardEntity overrideCard;

//    @ManyToOne
//    @JoinColumn(name = "name_i18n_id", insertable = false, updatable = false)
//    private I18nEntity nameI18n;

    @ManyToOne
    @JoinColumn(name = "card_layout_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private CardLayoutEntity cardLayout;
}
