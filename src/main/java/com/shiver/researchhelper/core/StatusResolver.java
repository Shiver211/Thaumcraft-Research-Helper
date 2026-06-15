package com.shiver.researchhelper.core;

import com.shiver.researchhelper.data.NodeStatus;
import com.shiver.researchhelper.data.ResearchNode;
import net.minecraft.entity.player.EntityPlayer;
import thaumcraft.api.capabilities.IPlayerKnowledge;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import thaumcraft.common.lib.research.ResearchManager;

import javax.annotation.Nullable;

public final class StatusResolver {

    private StatusResolver() {}

    /**
     * 解析研究节点的状态，根据是否为扫描触发节点分派处理。
     */
    public static NodeStatus resolve(@Nullable EntityPlayer player, @Nullable ResearchNode node) {
        if (node == null) return NodeStatus.UNKNOWN;
        if (node.scanTrigger) {
            return resolveScan(player, node.key);
        }
        return resolve(player, node.key);
    }

    /**
     * 根据研究键解析玩家的研究状态。
     */
    public static NodeStatus resolve(@Nullable EntityPlayer player, @Nullable String key) {
        if (player == null || key == null || key.isEmpty()) return NodeStatus.UNKNOWN;

        IPlayerKnowledge knowledge;
        try {
            knowledge = ThaumcraftCapabilities.getKnowledge(player);
        } catch (Exception t) {
            return NodeStatus.UNKNOWN;
        }
        if (knowledge == null) return NodeStatus.UNKNOWN;

        try {
            if (knowledge.isResearchComplete(key)) return NodeStatus.COMPLETED;
        } catch (Exception ignored) {}
        try {
            if (knowledge.isResearchKnown(key)) return NodeStatus.IN_PROGRESS;
        } catch (Exception ignored) {}

        boolean ready;
        try {
            ready = ResearchManager.doesPlayerHaveRequisites(player, key);
        } catch (Exception t) {
            ready = false;
        }
        return ready ? NodeStatus.READY : NodeStatus.LOCKED;
    }

    private static NodeStatus resolveScan(@Nullable EntityPlayer player, @Nullable String key) {
        if (player == null || key == null || key.isEmpty()) return NodeStatus.UNKNOWN;
        IPlayerKnowledge knowledge;
        try {
            knowledge = ThaumcraftCapabilities.getKnowledge(player);
        } catch (Exception t) {
            return NodeStatus.UNKNOWN;
        }
        if (knowledge == null) return NodeStatus.UNKNOWN;
        try {
            if (knowledge.isResearchKnown(key)) return NodeStatus.COMPLETED;
        } catch (Exception ignored) {}
        return NodeStatus.READY;
    }
}
