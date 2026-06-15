package com.shiver.researchhelper.client;

import com.shiver.researchhelper.core.I18nHelper;
import com.shiver.researchhelper.core.SearchIndex;
import com.shiver.researchhelper.data.NodeStatus;
import com.shiver.researchhelper.data.PathTree;
import com.shiver.researchhelper.data.ResearchNode;
import com.shiver.researchhelper.data.StageInfo;
import com.shiver.researchhelper.data.KnowledgeReq;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.text.translation.I18n;

import java.util.ArrayList;
import java.util.List;

public final class TreeRenderer {

    public static final int INDENT = 14;
    public static final int ROW_HEIGHT = 12;
    public static final int X_PAD = 6;


    static final class Row {
        static final int TYPE_NODE = 0;
        static final int TYPE_STAGE = 1;
        final int type;
        final int depth;
        final PathTree tree;
        final ResearchNode node;
        final StageInfo stage;
        final int stageIndex;
        int height = ROW_HEIGHT;

        Row(PathTree tree, int depth) {
            this.type = TYPE_NODE; this.tree = tree; this.node = tree.node;
            this.depth = depth; this.stage = null; this.stageIndex = -1;
        }
        Row(PathTree owner, StageInfo stage, int stageIndex, int depth) {
            this.type = TYPE_STAGE; this.tree = owner; this.node = owner.node;
            this.depth = depth; this.stage = stage; this.stageIndex = stageIndex;
        }
    }

    /**
     * 将树结构展平为可绘制的行列表，计算每行的高度。
     */
    static List<Row> flatten(PathTree root, int wrapWidth, FontRenderer fr, boolean showUnlocked) {
        List<Row> rows = new ArrayList<>();
        flattenRec(root, 0, rows, showUnlocked);
        for (Row row : rows) {
            if (row.type == Row.TYPE_STAGE) {
                int textX = row.depth * INDENT + X_PAD + 6;
                int available = wrapWidth - textX - 12;
                if (available < 60) available = 60;
                String text = stageLabel(row.stage, row.stageIndex);
                List<?> lines = fr.listFormattedStringToWidth(text, available);
                row.height = Math.max(ROW_HEIGHT, lines.size() * (fr.FONT_HEIGHT + 1) + 2);
            }
        }
        return rows;
    }

    private static void flattenRec(PathTree tree, int depth, List<Row> out, boolean showUnlocked) {
        if (!showUnlocked && tree.cachedStatus == NodeStatus.COMPLETED) return;
        out.add(new Row(tree, depth));
        if (tree.expanded && tree.node != null) {
            List<StageInfo> stages = tree.node.stages;
            for (int i = 0; i < stages.size(); i++) {
                out.add(new Row(tree, stages.get(i), i, depth + 1));
            }
        }
        for (PathTree branch : tree.branches) {
            flattenRec(branch, depth + 1, out, showUnlocked);
        }
    }


    /**
     * 绘制单行节点或阶段信息。
     */
    static void drawRow(Row row, FontRenderer fr, int x0, int y, int wrapWidth, NodeStatus status) {
        int x = x0 + row.depth * INDENT + X_PAD;
        if (row.type == Row.TYPE_NODE) {
            drawStatusIcon(x - 4, y + 2, status);
            String name = SearchIndex.safeName(row.node);
            String catDisplay = (row.node.localizedCategoryName != null && !row.node.localizedCategoryName.isEmpty())
                    ? row.node.localizedCategoryName : row.node.categoryKey;
            String meta = row.node.scanTrigger ? ("§7" + I18nHelper.tr(I18nHelper.KEY_META_SCAN)) :
                    ("§8[" + catDisplay + "]");
            String fullName = name + " " + meta;
            
            fr.drawStringWithShadow(fullName, x + 6, y, status.getRGB());
            String suffix = statusSuffix(status);
            if (!suffix.isEmpty()) {
                int sx = x + 6 + fr.getStringWidth(fullName) + 6;
                fr.drawStringWithShadow(suffix, sx, y, 0xAAAAAA);
            }
            String hint = I18nHelper.tr(row.tree.expanded
                    ? I18nHelper.KEY_HINT_COLLAPSE : I18nHelper.KEY_HINT_EXPAND);
            int stages = row.node.stages.size();
            if (stages > 0) {
                fr.drawStringWithShadow(hint, x + 6 + fr.getStringWidth(fullName)
                        + (suffix.isEmpty() ? 0 : fr.getStringWidth("  " + suffix)) + 8, y, 0x888888);
            }
        } else {
            String text = stageLabel(row.stage, row.stageIndex);
            int textX = x + 6;
            int available = wrapWidth - (row.depth * INDENT + X_PAD + 6) - 12;
            if (available < 60) available = 60;
            fr.drawSplitString(text, textX, y, available, 0xDDDDDD);
        }
    }

    private static String stageLabel(StageInfo stage, int index) {
        StringBuilder sb = new StringBuilder();
        sb.append("§b").append(I18nHelper.tr(I18nHelper.KEY_STAGE_PREFIX, index + 1)).append(" §7");
        if (stage == null) return sb.toString();
        boolean first = true;
        for (KnowledgeReq k : stage.knowledgeReqs) {
            if (!first) sb.append(", ");
            sb.append(formatKnowledgeReq(k));
            first = false;
        }
        for (String r : stage.researchReqs) {
            if (!first) sb.append(", ");
            sb.append(formatResearchReq(r));
            first = false;
        }
        if (stage.warp > 0) {
            if (!first) sb.append(", ");
            sb.append(I18nHelper.tr(I18nHelper.KEY_STAGE_WARP, stage.warp));
        }
        if (first) {
            String t = stage.text;
            if (t != null && !t.isEmpty()) {
                sb.append(t.replaceAll("(?i)<br>", "\n"));
            } else {
                sb.append(I18nHelper.tr(I18nHelper.KEY_STAGE_NONE));
            }
        }
        return sb.toString();
    }

    private static String formatKnowledgeReq(KnowledgeReq k) {
        String catName = k.categoryKey;
        try {
            catName = thaumcraft.api.research.ResearchCategories.getCategoryName(k.categoryKey);
        } catch (Exception ignored) {}

        String typeName = k.type;
        try {
            String localizedType = net.minecraft.client.resources.I18n.format("tc.research." + k.type.toLowerCase());
            if (!localizedType.equals("tc.research." + k.type.toLowerCase()) && !localizedType.isEmpty()) {
                typeName = localizedType;
            }
        } catch (Exception ignored) {}

        return "[" + catName + "] " + typeName + " x" + k.amount;
    }

    private static String formatResearchReq(String raw) {
        if (raw.startsWith("!")) {
            return I18nHelper.tr(I18nHelper.KEY_REQ_SCAN, localizeTrigger(raw.substring(1)));
        } else if (raw.startsWith("f_") || raw.startsWith("m_") || raw.startsWith("n_")) {
            return I18nHelper.tr(I18nHelper.KEY_REQ_TRIGGER, localizeTrigger(raw));
        } else {
            return I18nHelper.tr(I18nHelper.KEY_REQ_NEED, raw);
        }
    }

    private static String localizeTrigger(String key) {
        if (key == null || key.isEmpty()) return key;
        try {
            String localized = I18n.translateToLocal("research." + key + ".text");
            if (!localized.equals("research." + key + ".text") && !localized.isEmpty()) {
                return localized;
            }
        } catch (Exception ignored) {}
        return key;
    }

    private static String statusSuffix(NodeStatus status) {
        if (status == null) return "";
        switch (status) {
            case COMPLETED:   return I18nHelper.tr(I18nHelper.KEY_STATUS_DONE);
            case IN_PROGRESS: return I18nHelper.tr(I18nHelper.KEY_STATUS_INPROGRESS);
            case READY:       return I18nHelper.tr(I18nHelper.KEY_STATUS_READY);
            case LOCKED:      return I18nHelper.tr(I18nHelper.KEY_STATUS_LOCKED);
            default:          return "";
        }
    }

    private static void drawStatusIcon(int x, int y, NodeStatus status) {
        int color = status == null ? 0xFFAAAAAA : status.getRGB();
        Gui.drawRect(x, y, x + 6, y + 6, 0xFF000000 | color);
        Gui.drawRect(x + 1, y + 1, x + 5, y + 5, 0xFF000000 | color);
    }

    private TreeRenderer() {}
}
