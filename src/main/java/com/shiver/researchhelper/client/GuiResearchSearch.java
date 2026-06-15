package com.shiver.researchhelper.client;

import com.shiver.researchhelper.core.GraphBuilder;
import com.shiver.researchhelper.core.I18nHelper;
import com.shiver.researchhelper.core.PathFinder;
import com.shiver.researchhelper.core.SearchIndex;
import com.shiver.researchhelper.core.StatusResolver;
import com.shiver.researchhelper.data.NodeStatus;
import com.shiver.researchhelper.data.PathTree;
import com.shiver.researchhelper.data.ResearchNode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class GuiResearchSearch extends GuiScreen {

    private GuiTextField searchField;
    private GuiButton refreshButton;
    private GuiButton toggleUnlockedButton;
    private boolean showUnlocked = true;

    private Map<String, ResearchNode> graph;

    private List<ResearchNode> results = java.util.Collections.emptyList();
    private boolean resultsDirty = true;

    private String selectedKey;
    private PathTree tree;

    // Layout
    private static final int PADDING = 8;
    private static final int SEARCH_H = 14;
    private static final int DROPDOWN_MAX_ROWS = 7;
    private static final int DROPDOWN_ROW_H = 12;

    private int scrollOffset;
    private int treeContentHeight;
    private long lastStatusRefreshTime;
    private List<TreeRenderer.Row> visibleRows;

    public GuiResearchSearch() {
        this.graph = GraphBuilder.getOrBuild();
    }

    @Override
    public void initGui() {
        FontRenderer fr = fontRenderer;
        int w = this.width;
        int fieldY = PADDING + 14;
        int fieldW = w - PADDING * 2 - 140;

        searchField = new GuiTextField(0, fr, PADDING, fieldY, fieldW, SEARCH_H);
        searchField.setFocused(true);
        searchField.setMaxStringLength(64);

        refreshButton = new GuiButton(1, w - PADDING - 136, fieldY - 1, 40, SEARCH_H + 2, I18nHelper.tr(I18nHelper.KEY_REBUILD_BUTTON));
        String toggleText = I18nHelper.tr(showUnlocked ? I18nHelper.KEY_TOGGLE_UNLOCKED_TRUE : I18nHelper.KEY_TOGGLE_UNLOCKED_FALSE);
        toggleUnlockedButton = new GuiButton(2, w - PADDING - 92, fieldY - 1, 92, SEARCH_H + 2, toggleText);
        
        this.buttonList.clear();
        this.buttonList.add(refreshButton);
        this.buttonList.add(toggleUnlockedButton);

        if (selectedKey != null) {
            tree = PathFinder.buildTree(graph, selectedKey);
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            int delta = wheel > 0 ? -TreeRenderer.ROW_HEIGHT * 3 : TreeRenderer.ROW_HEIGHT * 3;
            scrollOffset += delta;
            if (scrollOffset < 0) scrollOffset = 0;
            int max = Math.max(0, treeContentHeight - treeViewportHeight());
            if (scrollOffset > max) scrollOffset = max;
        }
    }

    @Override
    protected void actionPerformed(@Nonnull GuiButton button) {
        if (button == refreshButton) {
            graph = GraphBuilder.build();
            resultsDirty = true;
            if (selectedKey != null) {
                tree = PathFinder.buildTree(graph, selectedKey);
            }
        } else if (button == toggleUnlockedButton) {
            showUnlocked = !showUnlocked;
            toggleUnlockedButton.displayString = I18nHelper.tr(showUnlocked ? I18nHelper.KEY_TOGGLE_UNLOCKED_TRUE : I18nHelper.KEY_TOGGLE_UNLOCKED_FALSE);
            resultsDirty = true;
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (searchField.textboxKeyTyped(typedChar, keyCode)) {
            resultsDirty = true;
            return;
        }
        if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) {
            if (!results.isEmpty()) {
                selectResearch(results.get(0).key);
            }
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton != 0) return;

        searchField.mouseClicked(mouseX, mouseY, mouseButton);

        if (showingDropdown()) {
            int rowIdx = dropdownRowAt(mouseX, mouseY);
            if (rowIdx >= 0 && rowIdx < results.size()) {
                selectResearch(results.get(rowIdx).key);
                return;
            }
        }

        if (tree != null && visibleRows != null) {
            TreeRenderer.Row row = rowAt(mouseX, mouseY);
            if (row != null && row.type == TreeRenderer.Row.TYPE_NODE) {
                if (!row.node.stages.isEmpty()) {
                    row.tree.expanded = !row.tree.expanded;
                }
            }
        }
    }

    /**
     * 绘制主屏幕，包括搜索框、下拉列表和依赖树。
     */
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawDarkPanel(PADDING - 4, PADDING - 4, width - (PADDING - 4) * 2, height - (PADDING - 4) * 2);

        FontRenderer fr = fontRenderer;
        fr.drawStringWithShadow("§f" + I18nHelper.tr(I18nHelper.KEY_SEARCH_LABEL), PADDING, PADDING, 0xFFFFFF);

        if (resultsDirty) {
            results = SearchIndex.search(graph, searchField.getText(), 50);
            resultsDirty = false;
        }

        searchField.drawTextBox();
        
        if (searchField.getText().isEmpty()) {
            String placeholder = I18nHelper.tr(I18nHelper.KEY_HINT_EMPTY_USAGE);
            fr.drawStringWithShadow(placeholder, searchField.x + 4, searchField.y + (searchField.height - 8) / 2, 0x777777);
        }

        int y = searchField.y + searchField.height + 2;
        if (showingDropdown()) {
            drawDropdown(y);
        }

        if (tree != null) {
            drawTree(y + (showingDropdown() ? Math.min(results.size(), DROPDOWN_MAX_ROWS) * DROPDOWN_ROW_H + 6 : 4));
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawDarkPanel(int x, int y, int w, int h) {
        net.minecraft.client.gui.Gui.drawRect(x, y, x + w, y + h, 0xC0101010);
        net.minecraft.client.gui.Gui.drawRect(x, y, x + w, y + 1, 0xFF555555);
        net.minecraft.client.gui.Gui.drawRect(x, y, x + 1, y + h, 0xFF555555);
    }

    private boolean showingDropdown() {
        return !searchField.getText().trim().isEmpty() && !results.isEmpty();
    }

    private int dropdownRowAt(int mouseX, int mouseY) {
        int top = searchField.y + searchField.height + 2;
        if (mouseY < top || mouseX < PADDING || mouseX > width - PADDING) return -1;
        int idx = (mouseY - top) / DROPDOWN_ROW_H;
        if (idx < 0 || idx >= Math.min(results.size(), DROPDOWN_MAX_ROWS)) return -1;
        return idx;
    }

    private void drawDropdown(int topY) {
        int rowH = DROPDOWN_ROW_H;
        int n = Math.min(results.size(), DROPDOWN_MAX_ROWS);
        int left = PADDING;
        int right = width - PADDING;
        net.minecraft.client.gui.Gui.drawRect(left, topY, right, topY + n * rowH, 0xE0202020);
        FontRenderer fr = fontRenderer;
        EntityPlayer player = Minecraft.getMinecraft().player;
        for (int i = 0; i < n; i++) {
            ResearchNode node = results.get(i);
            int ry = topY + i * rowH;
            NodeStatus status = StatusResolver.resolve(player, node);
            net.minecraft.client.gui.Gui.drawRect(left, ry, left + 3, ry + rowH - 1, status.getRGB());
            String name = SearchIndex.safeName(node);
            fr.drawStringWithShadow(name, left + 8, ry + 2, 0xFFFFFF);
            String catDisplay = (node.localizedCategoryName != null && !node.localizedCategoryName.isEmpty())
                    ? node.localizedCategoryName : node.categoryKey;
            String meta = node.scanTrigger ? ("§7" + I18nHelper.tr(I18nHelper.KEY_META_SCAN)) :
                    ("[" + catDisplay + "]" + (node.hidden ? " §8" + I18nHelper.tr(I18nHelper.KEY_META_HIDDEN) : ""));
            fr.drawStringWithShadow(meta, left + 8 + fr.getStringWidth(name) + 8, ry + 2, 0x888888);
        }
    }

    private int treeViewportHeight() {
        int top = treeTopY();
        return height - top - PADDING - 6;
    }

    private int treeTopY() {
        int y = searchField.y + searchField.height + 2;
        if (showingDropdown()) {
            y += Math.min(results.size(), DROPDOWN_MAX_ROWS) * DROPDOWN_ROW_H + 6;
        } else {
            y += 4;
        }
        return y + 12;
    }

    private void drawTree(int headerY) {
        FontRenderer fr = fontRenderer;
        EntityPlayer player = Minecraft.getMinecraft().player;

        long now = System.currentTimeMillis();
        if (now - lastStatusRefreshTime >= 10000 || tree.cachedStatus == null) {
            refreshStatuses(tree, player);
            lastStatusRefreshTime = now;
        }

        ResearchNode sel = graph.get(selectedKey);
        String targetLabel = sel == null ? selectedKey : SearchIndex.safeName(sel);
        NodeStatus targetStatus = StatusResolver.resolve(player, sel);
        String targetCaption = I18nHelper.tr(I18nHelper.KEY_TARGET_LABEL);
        fr.drawStringWithShadow(targetCaption, PADDING, headerY, 0xAAAAAA);
        fr.drawStringWithShadow(targetLabel, PADDING + fr.getStringWidth(targetCaption), headerY, targetStatus.getRGB());

        int right = width - PADDING;
        String legend = I18nHelper.tr(I18nHelper.KEY_HINT_EMPTY_LEGEND);
        fr.drawStringWithShadow(legend, right - fr.getStringWidth(legend), headerY, 0xCCCCCC);

        int top = treeTopY();
        int bottom = height - PADDING - 6;
        int left = PADDING;

        int treeWidth = right - left;
        visibleRows = TreeRenderer.flatten(tree, treeWidth, fr, showUnlocked);
        treeContentHeight = 0;
        for (TreeRenderer.Row row : visibleRows) {
            treeContentHeight += row.height;
        }

        int maxScroll = Math.max(0, treeContentHeight - treeViewportHeight());
        if (scrollOffset > maxScroll) scrollOffset = maxScroll;

        net.minecraft.client.gui.Gui.drawRect(left, top, right, bottom, 0x90000000);
        net.minecraft.client.gui.Gui.drawRect(left, top, right, top + 1, 0xFF333333);

        GlStateManager.pushMatrix();
        int scaleFactor = scaleFactor();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(left * scaleFactor, (mc.displayHeight - bottom * scaleFactor),
                (right - left) * scaleFactor, (bottom - top) * scaleFactor);

        int y = top + 2 - scrollOffset;
        for (TreeRenderer.Row row : visibleRows) {
            if (y + row.height >= top && y <= bottom) {
                TreeRenderer.drawRow(row, fr, left + 2, y, treeWidth,
                        row.type == TreeRenderer.Row.TYPE_NODE ? row.tree.cachedStatus : null);
            }
            y += row.height;
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GlStateManager.popMatrix();

        if (treeContentHeight > (bottom - top)) {
            int trackH = bottom - top - 4;
            int thumbH = Math.max(12, trackH * (bottom - top) / treeContentHeight);
            int thumbY = top + 2 + (trackH - thumbH) * scrollOffset / Math.max(1, maxScroll);
            int sx = right - 4;
            net.minecraft.client.gui.Gui.drawRect(sx, top + 2, sx + 2, bottom - 2, 0xFF2A2A2A);
            net.minecraft.client.gui.Gui.drawRect(sx, thumbY, sx + 2, thumbY + thumbH, 0xFF666666);
        }
    }

    private void refreshStatuses(PathTree t, EntityPlayer player) {
        if (t == null) return;
        t.cachedStatus = StatusResolver.resolve(player, t.node);
        for (PathTree b : t.branches) refreshStatuses(b, player);
    }

    private TreeRenderer.Row rowAt(int mouseX, int mouseY) {
        if (visibleRows == null) return null;
        int top = treeTopY();
        int bottom = height - PADDING - 6;
        if (mouseY < top || mouseY > bottom) return null;
        if (mouseX < PADDING || mouseX > width - PADDING) return null;
        int y = top + 2 - scrollOffset;
        for (TreeRenderer.Row row : visibleRows) {
            int rowBottom = y + row.height;
            if (mouseY >= y && mouseY < rowBottom) return row;
            y = rowBottom;
        }
        return null;
    }

    private int scaleFactor() {
        return new ScaledResolution(mc).getScaleFactor();
    }

    /**
     * 选择研究节点，构建依赖树并重置滚动位置。
     */
    private void selectResearch(String key) {
        selectedKey = key;
        tree = PathFinder.buildTree(graph, key);
        scrollOffset = 0;
        collapseAll(tree);
        searchField.setText("");
        results = java.util.Collections.emptyList();
        resultsDirty = false;
    }

    private void collapseAll(PathTree t) {
        if (t == null) return;
        t.expanded = false;
        for (PathTree b : t.branches) collapseAll(b);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

}
