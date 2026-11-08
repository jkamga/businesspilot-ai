package com.businesspilot.common.plan;

public enum PlanResource {
    PROSPECT("prospect"),
    CONVERSATION("conversation"),
    KNOWLEDGE_ENTRY("entrée de base de connaissances");

    private final String label;

    PlanResource(String label) { this.label = label; }

    public String getLabel() { return label; }
}
