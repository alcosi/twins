package org.twins.core.dao.specifications;

import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.twins.core.dao.twin.TwinEntity;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommonSpecificationTest {

    @Mock
    private Root<TwinEntity> root;

    @Mock
    private CriteriaQuery<?> query;

    @Mock
    private CriteriaBuilder cb;

    @Mock
    private Path<Object> path;

    @Mock
    private Expression<Boolean> functionExpression;

    @Mock
    private Predicate predicate;

    @Test
    public void testCheckUuidIn() {
        UUID uuid = UUID.randomUUID();
        var uuids = Collections.singletonList(uuid);
        String field = "id";

        when(root.get(field)).thenReturn(path);
        when(cb.literal(any())).thenReturn(mock(Expression.class));
        when(cb.function(eq("uuid_in_array"), eq(Boolean.class), any(Expression.class), any(Expression.class)))
                .thenReturn(functionExpression);
        when(cb.isTrue(functionExpression)).thenReturn(predicate);
        when(path.isNotNull()).thenReturn(predicate);
        when(cb.and(any(Predicate.class), any(Predicate.class))).thenReturn(predicate);

        var spec = CommonSpecification.<TwinEntity>checkUuidIn(uuids, false, false, field);
        spec.toPredicate(root, query, cb);

        verify(cb).function(eq("uuid_in_array"), eq(Boolean.class), eq(path), any(Expression.class));
        verify(cb).isTrue(functionExpression);
    }
}
