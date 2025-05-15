package com.cofeecode.application.powerhauscore.security;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;

public class InactivityLogoutHandler implements AfterNavigationObserver {

    private static final int TIMEOUT_MINUTES = 15;

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        addInactivityScript();
    }

    private void addInactivityScript() {
        int timeoutMillis = TIMEOUT_MINUTES * 60 * 1000;

        UI.getCurrent().getPage().executeJs("""
            if (!window.inactivityScriptInjected) {
                let logoutTimeout;
                const logoutDelay = $0;

                function resetLogoutTimer() {
                    clearTimeout(logoutTimeout);
                    logoutTimeout = setTimeout(() => {
                        window.location.href = '/auto-logout';
                    }, logoutDelay);
                }

                ['click', 'mousemove', 'keydown', 'scroll'].forEach(evt => {
                    document.addEventListener(evt, resetLogoutTimer, true);
                });

                resetLogoutTimer();
                window.inactivityScriptInjected = true;
            }
        """, timeoutMillis);
    }
}
