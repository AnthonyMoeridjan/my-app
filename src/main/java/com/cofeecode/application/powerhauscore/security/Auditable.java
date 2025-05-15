package com.cofeecode.application.powerhauscore.security;

import java.util.Date;

public interface Auditable {
    void setCreatedAt(Date createdAt);
    void setUpdatedAt(Date updatedAt);
}
