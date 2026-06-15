package com.shiver.researchhelper.data;


import java.util.ArrayList;
import java.util.List;

public class ResearchNode {
    public final String key;
    public final boolean scanTrigger;
    public final String categoryKey;
    public final String localizedCategoryName;
    public final String localizedName;
    public final boolean hidden;
    public final boolean autoUnlock;
    public final int stageCount;
    public final List<String> parentKeys = new ArrayList<>();
    public final List<String> siblingKeys = new ArrayList<>();
    public final List<StageInfo> stages = new ArrayList<>();

    public ResearchNode(String key, String categoryKey, String localizedCategoryName,
                        String localizedName,
                        boolean hidden, boolean autoUnlock, int stageCount) {
        this(key, false, categoryKey, localizedCategoryName, localizedName, hidden, autoUnlock, stageCount);
    }

    public ResearchNode(String scanKey, String localizedName) {
        this(scanKey, true, "", "", localizedName, false, false, 0);
    }

    private ResearchNode(String key, boolean scanTrigger, String categoryKey,
                         String localizedCategoryName, String localizedName,
                         boolean hidden, boolean autoUnlock, int stageCount) {
        this.key = key;
        this.scanTrigger = scanTrigger;
        this.categoryKey = categoryKey;
        this.localizedCategoryName = localizedCategoryName != null ? localizedCategoryName : "";
        this.localizedName = localizedName;
        this.hidden = hidden;
        this.autoUnlock = autoUnlock;
        this.stageCount = stageCount;
    }
}
