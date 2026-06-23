package org.twins.core.service.space;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.service.EntitySmartService;
import org.springframework.stereotype.Service;
import org.twins.core.dao.space.SpaceRoleUserRepository;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.history.HistoryService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.user.UserService;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class SpaceUserRoleService {
    final EntitySmartService entitySmartService;
    final SpaceRoleUserRepository spaceRoleUserRepository;
    final AuthService authService;
    final TwinService twinService;
    final UserService userService;
    final HistoryService historyService;

    // twinId is equivalent of spaceId


}
