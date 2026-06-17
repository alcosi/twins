package org.twins.core.dao.action;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.twins.core.dao.i18n.I18nTranslationEntity;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "twin_action")
public class TwinActionEntity {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "name_i18n_id")
    private UUID nameI18nId;

    // Direct join to i18n_translation by raw FK — skips intermediate i18n table
    @Deprecated //for specification only
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "i18n_id", referencedColumnName = "name_i18n_id", insertable = false, updatable = false)
    private List<I18nTranslationEntity> nameI18nTranslationsSpecOnly;
}
