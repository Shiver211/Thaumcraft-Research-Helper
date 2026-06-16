package com.shiver.researchhelper.data;

public enum NodeStatus {
    COMPLETED,
    IN_PROGRESS,
    READY,
    LOCKED,
    UNKNOWN;

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
