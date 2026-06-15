package com.shiver.researchhelper.data;

public class KnowledgeReq {
    public final String type;
    public final String categoryKey;
    public final int amount;

    public KnowledgeReq(String type, String categoryKey, int amount) {
        this.type = type;
        this.categoryKey = categoryKey;
        this.amount = amount;
    }

    @Override
    public String toString() {
        return type + " (" + categoryKey + ") x" + amount;
    }
}
