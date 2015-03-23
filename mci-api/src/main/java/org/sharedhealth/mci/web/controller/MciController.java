package org.sharedhealth.mci.web.controller;

import org.sharedhealth.mci.web.infrastructure.security.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

public class MciController {

    private static final Logger logger = LoggerFactory.getLogger(MciController.class);
    protected UserInfo getUserInfo() {
        return (UserInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    protected void logAccessDetails(UserInfo userInfo, String action) {
        logger.info(String.format("ACCESS: USER=%s TYPE=%s ACTION=%s",
                userInfo.getProperties().getId(), userInfo.getProperties().getName(), action));
    }
}
