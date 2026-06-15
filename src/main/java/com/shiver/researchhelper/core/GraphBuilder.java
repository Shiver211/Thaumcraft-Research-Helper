package com.shiver.researchhelper.core;

import com.shiver.researchhelper.data.KnowledgeReq;
import com.shiver.researchhelper.data.ResearchNode;
import com.shiver.researchhelper.data.StageInfo;
import net.minecraft.util.text.translation.I18n;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import thaumcraft.api.capabilities.IPlayerKnowledge;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchCategory;
import thaumcraft.api.research.ResearchEntry;
import thaumcraft.api.research.ResearchStage;

import java.util.LinkedHashMap;
import java.util.Map;

public final class GraphBuilder {

    private static final Logger LOGGER = LogManager.getLogger("ResearchHelper");

    private static volatile Map<String, ResearchNode> GRAPH = null;

    private GraphBuilder() {}

    /**
     * 构建研究图谱，遍历所有研究类别和条目。
     * 同时合成扫描触发节点。
     */
    public static synchronized Map<String, ResearchNode> build() {
        Map<String, ResearchNode> out = new LinkedHashMap<>();
        int categories = 0;
        int entries = 0;
        java.util.Set<String> scanTriggers = new java.util.LinkedHashSet<>();

        for (ResearchCategory cat : ResearchCategories.researchCategories.values()) {
            categories++;
            if (cat.research == null) continue;
            for (ResearchEntry entry : cat.research.values()) {
                if (entry.getKey() == null) continue;
                ResearchNode node = snapshot(entry, scanTriggers);
                out.put(entry.getKey(), node);
                entries++;
            }
        }

        int scans = 0;
        for (String scanKey : scanTriggers) {
            if (out.containsKey(scanKey)) continue;
            out.put(scanKey, makeScanNode(scanKey));
            scans++;
        }

        GRAPH = out;
        LOGGER.info("ResearchHelper graph built: {} categories, {} entries, {} scan triggers",
                categories, entries, scans);
        return out;
    }

    /**
     * 获取缓存的图谱，如果未构建则延迟构建。
     */
    public static synchronized Map<String, ResearchNode> getOrBuild() {
        if (GRAPH == null || GRAPH.isEmpty()) {
            return build();
        }
        return GRAPH;
    }

    /**
     * 使图谱失效，下次访问时强制重新构建。
     */
    public static synchronized void invalidate() {
        GRAPH = null;
    }

    private static ResearchNode snapshot(ResearchEntry entry, java.util.Set<String> scanTriggersSink) {
        boolean hidden = false, autoUnlock = false;
        if (entry.getMeta() != null) {
            for (ResearchEntry.EnumResearchMeta m : entry.getMeta()) {
                if (m == ResearchEntry.EnumResearchMeta.HIDDEN) hidden = true;
                else if (m == ResearchEntry.EnumResearchMeta.AUTOUNLOCK) autoUnlock = true;
            }
        }

        ResearchStage[] rawStages = entry.getStages();
        int stageCount = rawStages == null ? 0 : rawStages.length;

        String localizedCatName = "";
        try {
            localizedCatName = ResearchCategories.getCategoryName(entry.getCategory());
        } catch (Exception ignored) {}

        ResearchNode node = new ResearchNode(
                entry.getKey(),
                entry.getCategory(),
                localizedCatName,
                entry.getLocalizedName(),
                hidden, autoUnlock, stageCount);

        ParentKeyCleaner.CleanedKeys parents = ParentKeyCleaner.clean(entry.getParentsClean());
        node.parentKeys.addAll(parents.researchKeys);
        node.parentKeys.addAll(parents.scanTriggerKeys);
        scanTriggersSink.addAll(parents.scanTriggerKeys);

        ParentKeyCleaner.CleanedKeys siblings = ParentKeyCleaner.clean(entry.getSiblings());
        node.siblingKeys.addAll(siblings.researchKeys);
        node.siblingKeys.addAll(siblings.scanTriggerKeys);
        scanTriggersSink.addAll(siblings.scanTriggerKeys);

        if (rawStages != null) {
            for (ResearchStage s : rawStages) {
                node.stages.add(snapshotStage(s));
            }
        }
        return node;
    }

    private static ResearchNode makeScanNode(String scanKey) {
        String name = localizeScanTrigger(scanKey);
        return new ResearchNode(scanKey, name);
    }

    private static String localizeScanTrigger(String scanKey) {
        String[] candidates = {
                "research." + scanKey + ".text",
                "research." + scanKey + ".title",
        };
        for (String key : candidates) {
            try {
                String localized = I18n.translateToLocal(key);
                if (!localized.equals(key) && !localized.isEmpty()) {
                    return localized;
                }
            } catch (Exception ignored) {}
        }
        return "(scan) " + scanKey;
    }

    private static StageInfo snapshotStage(ResearchStage s) {
        StageInfo info = new StageInfo(safeText(s), s.getWarp());

        if (s.getKnow() != null) {
            for (ResearchStage.Knowledge k : s.getKnow()) {
                if (k == null) continue;
                String typeName = k.type != null ? k.type.name() : "?";
                String catKey = k.category != null ? k.category.key : "?";
                info.knowledgeReqs.add(new KnowledgeReq(typeName, catKey, k.amount));
            }
        }

        if (s.getResearch() != null) {
            for (String r : s.getResearch()) {
                if (r == null || r.isEmpty()) continue;
                String cleaned = r;
                if (cleaned.startsWith("~")) cleaned = cleaned.substring(1);
                int at = cleaned.indexOf('@');
                if (at > 0) cleaned = cleaned.substring(0, at);
                info.researchReqs.add(cleaned);
            }
        }
        return info;
    }

    private static String safeText(ResearchStage s) {
        try {
            String t = s.getTextLocalized();
            if (t != null && !t.isEmpty()) return t;
        } catch (Exception ignored) {}
        String raw = s.getText();
        if (raw == null || raw.isEmpty()) return "";
        try {
            return I18n.translateToLocal(raw);
        } catch (Throwable ignored) {
            return raw;
        }
    }

    public static String knowledgeTypeLabel(IPlayerKnowledge.EnumKnowledgeType type) {
        return type == null ? "?" : type.name();
    }
}
