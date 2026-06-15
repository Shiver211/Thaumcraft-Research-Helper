package com.shiver.researchhelper.data;

import java.util.ArrayList;
import java.util.List;

public class PathTree {
    public final ResearchNode node;
    public boolean expanded;
    public com.shiver.researchhelper.data.NodeStatus cachedStatus;
    public final List<PathTree> branches = new ArrayList<>();

    public PathTree(ResearchNode node) {
        this.node = node;
    }

    public int size() {
        int n = 1;
        for (PathTree b : branches) n += b.size();
        return n;
    }
}
