package com.shiver.researchhelper.core;

import thaumcraft.api.research.ResearchCategories;

import java.util.ArrayList;
import java.util.List;

public final class ParentKeyCleaner {

    private ParentKeyCleaner() {}

    public static final class CleanedKeys {
        public final List<String> researchKeys = new ArrayList<>();
        public final List<String> scanTriggerKeys = new ArrayList<>();

        public boolean isEmpty() {
            return researchKeys.isEmpty() && scanTriggerKeys.isEmpty();
        }
    }

    /**
     * 清理原始字符串，分离研究键和扫描触发键。
     */
    public static CleanedKeys clean(String[] rawCleaned) {
        CleanedKeys out = new CleanedKeys();
        if (rawCleaned == null) return out;
        for (String p : rawCleaned) {
            if (p == null || p.isEmpty()) continue;
            if (p.startsWith("!")) {
                String scan = p.substring(1);
                if (scan.isEmpty()) continue;
                if (!out.scanTriggerKeys.contains(scan)) out.scanTriggerKeys.add(scan);
            } else             if (resolvesToResearch(p)) {
                if (!out.researchKeys.contains(p)) out.researchKeys.add(p);
            }
        }
        return out;
    }

    public static boolean resolvesToResearch(String key) {
        return ResearchCategories.getResearch(key) != null;
    }
}
