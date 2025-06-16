package org.twins.core.domain.auth;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class EmailVerificationByIDP extends EmailVerificationHolder {
}
