package org.twins.core.dao.specifications.locale;

import lombok.extern.slf4j.Slf4j;
import org.twins.core.dao.i18n.I18nLocaleEntity;
import org.springframework.data.jpa.domain.Specification;

@Slf4j
public class I18nLocaleSpecification {

    public static Specification<I18nLocaleEntity> checkLocale(String locale) {
        return (root, query, cb) -> cb.equal(root.get(I18nLocaleEntity.Fields.locale), locale);
    }
}
