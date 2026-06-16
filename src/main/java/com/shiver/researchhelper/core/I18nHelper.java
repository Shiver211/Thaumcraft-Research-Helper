package com.shiver.researchhelper.core;

import net.minecraft.client.resources.I18n;

public final class I18nHelper {

    private I18nHelper() {}

    public static String tr(String key) {
        return I18n.format(key);
    }

    public static String tr(String key, String a) {
        return I18n.format(key, a);
    }

    public static String tr(String key, int a) {
        return I18n.format(key, a);
    }

    // ---- GUI labels ---------------------------------------------------------

    public static final String KEY_SEARCH_LABEL      = "researchhelper.gui.search_label";
    public static final String KEY_TARGET_LABEL      = "researchhelper.gui.target_label";
    public static final String KEY_HINT_EMPTY_USAGE  = "researchhelper.gui.hint_empty_usage";
    public static final String KEY_HINT_SHORTCUT     = "researchhelper.gui.hint_shortcut";
    public static final String KEY_REBUILD_BUTTON    = "researchhelper.gui.rebuild";
    public static final String KEY_TOGGLE_UNLOCKED_TRUE = "researchhelper.gui.toggle_unlocked.true";
    public static final String KEY_TOGGLE_UNLOCKED_FALSE = "researchhelper.gui.toggle_unlocked.false";

    public static final String KEY_META_SCAN         = "researchhelper.gui.meta_scan";
    public static final String KEY_META_HIDDEN       = "researchhelper.gui.meta_hidden";

    public static final String KEY_STATUS_DONE       = "researchhelper.gui.status_done";
    public static final String KEY_STATUS_INPROGRESS = "researchhelper.gui.status_in_progress";
    public static final String KEY_STATUS_READY      = "researchhelper.gui.status_ready";
    public static final String KEY_STATUS_LOCKED     = "researchhelper.gui.status_locked";

    public static final String KEY_HINT_COLLAPSE     = "researchhelper.gui.hint_collapse";
    public static final String KEY_HINT_EXPAND       = "researchhelper.gui.hint_expand";

    public static final String KEY_STAGE_PREFIX      = "researchhelper.gui.stage_prefix";
    public static final String KEY_STAGE_WARP        = "researchhelper.gui.stage_warp";
    public static final String KEY_STAGE_NONE        = "researchhelper.gui.stage_none";

    public static final String KEY_REQ_NEED          = "researchhelper.gui.req_need";
    public static final String KEY_REQ_SCAN          = "researchhelper.gui.req_scan";
    public static final String KEY_REQ_TRIGGER       = "researchhelper.gui.req_trigger";
    public static final String KEY_REQ_OBTAIN        = "researchhelper.gui.req_obtain";
    public static final String KEY_REQ_CRAFT         = "researchhelper.gui.req_craft";
}
