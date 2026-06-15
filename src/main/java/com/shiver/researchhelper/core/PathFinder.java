package com.shiver.researchhelper.core;

import com.shiver.researchhelper.data.PathTree;
import com.shiver.researchhelper.data.ResearchNode;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class PathFinder {

    private static final int MAX_DEPTH = 64;

    private PathFinder() {}

    /**
     * 构建以目标研究为根的依赖树。
     * 包含循环检测和深度限制。
     */
    @Nullable
    public static PathTree buildTree(Map<String, ResearchNode> graph, String target) {
        ResearchNode root = graph.get(target);
        if (root == null) return null;
        Set<String> path = new HashSet<>();
        return buildRec(root, graph, path, 0);
    }

    private static PathTree buildRec(ResearchNode node, Map<String, ResearchNode> graph,
                                     Set<String> ancestors, int depth) {
        PathTree tree = new PathTree(node);
        if (depth >= MAX_DEPTH) return tree;

        for (String parentKey : node.parentKeys) {
            if (ancestors.contains(parentKey)) continue;
            ResearchNode parent = graph.get(parentKey);
            if (parent == null) continue;
            Set<String> branchPath = new HashSet<>(ancestors);
            branchPath.add(node.key);
            tree.branches.add(buildRec(parent, graph, branchPath, depth + 1));
        }
        return tree;
    }
}
