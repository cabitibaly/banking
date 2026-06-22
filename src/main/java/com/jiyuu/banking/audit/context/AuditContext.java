package com.jiyuu.banking.audit.context;

public class AuditContext {

    private static class AuditEntry {
        Object oldValue;
        Object newValue;
    }

    private static final ThreadLocal<AuditEntry> context = new ThreadLocal<>();

    public static void setOldValue(Object value) {
        getOrCreate().oldValue = value;
    }

    public static void setNewValue(Object value) {
        getOrCreate().newValue = value;
    }

    public static Object getOldValue() {
        AuditEntry entry = context.get();
        return entry != null ? entry.oldValue : null;
    }

    public static Object getNewValue() {
        AuditEntry entry = context.get();
        return entry != null ? entry.newValue : null;
    }

    public static void clear() {
        context.remove();
    }

    private static AuditEntry getOrCreate() {
        AuditEntry entry = context.get();
        if (entry == null) {
            entry = new AuditEntry();
            context.set(entry);
        }
        return entry;
    }
}