package org.twins.core.dao.specifications;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.enums.SortDirection;

@Data
@Accessors(chain = true)
public class SortOption<S extends SortField<?>> {
    private S sortField;
    private SortDirection sortDirection = SortDirection.ASC;

    public SortOption() {
    }

    public SortOption(S sortField) {
        this.sortField = sortField;
    }

    public SortOption(S sortField, SortDirection sortDirection) {
        this.sortField = sortField;
        this.sortDirection = sortDirection;
    }

    public void setIfNotNull(SortOption<S> value) {
        if (value != null) {
            this.sortField = value.sortField;
            this.sortDirection = value.sortDirection;
        }
    }

    public boolean isAscending() {
        return sortDirection != SortDirection.DESC;
    }

    @SuppressWarnings("unchecked")
    public <T> Specification<T> toSortSpecification() {
        if (sortField == null)
            return null;
        return ((SortField<T>) sortField).toSortSpecification(isAscending());
    }
}
