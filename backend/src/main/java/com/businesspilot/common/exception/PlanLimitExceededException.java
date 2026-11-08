package com.businesspilot.common.exception;

public class PlanLimitExceededException extends RuntimeException {

    private final int limit;

    public PlanLimitExceededException(String resource, int limit) {
        super("Limite du plan atteinte pour " + resource + " (max " + limit + ")");
        this.limit = limit;
    }

    public int getLimit() { return limit; }
}
