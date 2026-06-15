package com.shiver.researchhelper.data;

import net.minecraft.util.text.TextFormatting;

public enum NodeStatus {
    COMPLETED(TextFormatting.GREEN),
    IN_PROGRESS(TextFormatting.YELLOW),
    READY(TextFormatting.GOLD),
    LOCKED(TextFormatting.RED),
    UNKNOWN(TextFormatting.GRAY);

    private final TextFormatting color;

    NodeStatus(TextFormatting color) {
        this.color = color;
    }

    public TextFormatting getColor() {
        return color;
    }

    public int getRGB() {
        switch (this) {
            case COMPLETED:  return 0x55FF55;
            case IN_PROGRESS:return 0xFFFF55;
            case READY:      return 0xFFAA00;
            case LOCKED:     return 0xFF5555;
            default:         return 0xAAAAAA; // UNKNOWN
        }
    }
}
