package com.businesspilot.common.tenant;

public final class TenantContext {

    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    private TenantContext() {}

    public static String getCurrentTenant() {
        return CURRENT_TENANT.get();
    }

    public static void setCurrentTenant(String organizationId) {
        CURRENT_TENANT.set(organizationId);
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
