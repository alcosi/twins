package org.twins.core.domain.usage;

import java.util.List;

/**
 * Marks entities that can collect usages (places where they are referenced from).
 * Lombok {@code @Data @Accessors(chain = true)} entities satisfy it with the generated
 * getUsages/setUsages pair for the {@code usages} transient field (chain setter returns
 * the concrete entity, which is covariant with the return type declared here).
 */
public interface UsageHolder {
    List<Usage> getUsages();

    UsageHolder setUsages(List<Usage> usages);
}
