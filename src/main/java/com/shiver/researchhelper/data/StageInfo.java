package com.shiver.researchhelper.data;

import java.util.ArrayList;
import java.util.List;

public class StageInfo {
    public final String text;
    public final List<KnowledgeReq> knowledgeReqs = new ArrayList<>();
    public final List<String> researchReqs = new ArrayList<>();
    public final int warp;

    public StageInfo(String text, int warp) {
        this.text = text == null ? "" : text;
        this.warp = warp;
    }
}
