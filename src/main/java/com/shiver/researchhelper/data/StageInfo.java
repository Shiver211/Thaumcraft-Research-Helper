package com.shiver.researchhelper.data;

import java.util.ArrayList;
import java.util.List;

public class StageInfo {
    public final String text;
    public final List<KnowledgeReq> knowledgeReqs = new ArrayList<>();
    public final List<String> researchReqs = new ArrayList<>();
    // 阶段需要消耗（持有）的物品
    public final List<String> obtainReqs = new ArrayList<>();
    //阶段需要合成的物品
    public final List<String> craftReqs = new ArrayList<>();
    public final int warp;

    public StageInfo(String text, int warp) {
        this.text = text == null ? "" : text;
        this.warp = warp;
    }
}
