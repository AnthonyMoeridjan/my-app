package com.cofeecode.application.powerhauscore.security;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.util.Date;

public class AuditListener {

    @PrePersist
    public void setCreatedAt(Object entity) {
        if (entity instanceof Auditable auditable) {
            auditable.setCreatedAt(new Date());
            auditable.setUpdatedAt(new Date());
        }
    }

    @PreUpdate
    public void setUpdatedAt(Object entity) {
        if (entity instanceof Auditable auditable) {
            auditable.setUpdatedAt(new Date());
        }
    }
}
