package com.shiver.researchhelper.core;

import com.shiver.researchhelper.data.KnowledgeReq;
import com.shiver.researchhelper.data.ResearchNode;
import com.shiver.researchhelper.data.StageInfo;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchCategory;
import thaumcraft.api.research.ResearchEntry;
import thaumcraft.api.research.ResearchStage;

import java.util.LinkedHashMap;
import java.util.List;
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
                String localized = I18n.format(key);
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

        /*
         * 物品要求：obtain（需持有/消耗）与 craft（需合成）。
         */
        Object[] obtain = s.getObtain();
        if (obtain != null) {
            for (Object o : obtain) {
                String name = describeItemEntry(o);
                if (name != null) info.obtainReqs.add(name);
            }
        }
        Object[] craft = s.getCraft();
        if (craft != null) {
            for (Object o : craft) {
                String name = describeItemEntry(o);
                if (name != null) info.craftReqs.add(name);
            }
        }
        return info;
    }

    /**
     * 把 ResearchStage.obtain / craft 里的单个元素解析为可读名称。
     * 返回 null 表示该元素无法解析或为空，调用方应跳过。
     */
    private static String describeItemEntry(Object o) {
        if (o == null) return null;
        if (o instanceof ItemStack) {
            ItemStack stack = (ItemStack) o;
            if (stack.isEmpty()) return null;
            return stack.getDisplayName();
        }
        if (o instanceof String) {
            return oreDictDisplayName((String) o);
        }
        if (o instanceof Ingredient) {
            ItemStack[] stacks = ((Ingredient) o).getMatchingStacks();
            return firstDisplayName(stacks);
        }
        if (o instanceof ItemStack[]) {
            return firstDisplayName((ItemStack[]) o);
        }
        if (o instanceof List) {
            for (Object e : (List<?>) o) {
                String name = describeItemEntry(e);
                if (name != null) return name;
            }
            return null;
        }
        return o.toString();
    }

    private static String oreDictDisplayName(String oreKey) {
        if (oreKey == null || oreKey.isEmpty()) return null;
        try {
            NonNullList<ItemStack> ores = OreDictionary.getOres(oreKey, false);
            String name = firstDisplayName(ores.toArray(new ItemStack[0]));
            return name != null ? name : oreKey;
        } catch (Throwable ignored) {
            return oreKey;
        }
    }

    private static String firstDisplayName(ItemStack[] stacks) {
        if (stacks == null) return null;
        for (ItemStack s : stacks) {
            if (s == null || s.isEmpty()) continue;
            try {
                return s.getDisplayName();
            } catch (Throwable ignored) {}
        }
        return null;
    }

    private static String safeText(ResearchStage s) {
        try {
            String t = s.getTextLocalized();
            if (t != null && !t.isEmpty()) return t;
        } catch (Exception ignored) {}
        String raw = s.getText();
        if (raw == null || raw.isEmpty()) return "";
        try {
            return I18n.format(raw);
        } catch (Throwable ignored) {
            return raw;
        }
    }
}
