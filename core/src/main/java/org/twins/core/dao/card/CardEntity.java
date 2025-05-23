package org.twins.core.dao.card;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "card")
public class CardEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "logo")
    private String logo;

    @Column(name = "card_layout_id")
    private UUID cardLayoutId;

    @Column(name = "key")
    private String key;

    @Column(name = "name_i18n_id")
    private UUID nameI18NId;

    @ManyToOne
    @JoinColumn(name = "card_layout_id", insertable = false, updatable = false, nullable = false)
    private CardLayoutEntity cardLayout;

//    @ManyToOne
//    @JoinColumn(name = "name_i18n_id", insertable = false, updatable = false)
//    private I18nEntity nameI18n;
}
