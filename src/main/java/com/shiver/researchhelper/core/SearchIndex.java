package com.shiver.researchhelper.core;

import com.shiver.researchhelper.data.ResearchNode;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class SearchIndex {

    private SearchIndex() {}

    /**
     * 搜索研究节点，支持名称和键的模糊匹配。
     */
    public static List<ResearchNode> search(Map<String, ResearchNode> graph, String query, int limit) {
        List<ResearchNode> out = new ArrayList<>();
        if (query == null) return out;
        String q = query.trim().toLowerCase(Locale.ROOT);
        if (q.isEmpty()) return out;

        for (ResearchNode node : graph.values()) {
            if (matches(node, q)) {
                out.add(node);
                if (out.size() >= limit) break;
            }
        }
        return out;
    }

    private static boolean matches(ResearchNode node, String loweredQuery) {
        if (node.key != null && node.key.toLowerCase(Locale.ROOT).contains(loweredQuery)) {
            return true;
        }
        String name = safeName(node);
        return name.toLowerCase(Locale.ROOT).contains(loweredQuery);
    }

    public static String safeName(ResearchNode node) {
        String n = node.localizedName;
        if (n == null || n.isEmpty()) return node.key;
        return n;
    }
}
