package com.shiver.researchhelper.tab;

import com.shiver.researchhelper.ResearchHelper;
import com.shiver.researchhelper.Tags;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public final class CreativeTabResearchHelper extends CreativeTabs {

    public CreativeTabResearchHelper() {
        super(Tags.MOD_ID);
    }

    @Nonnull
    @Override
    public ItemStack createIcon() {
        return new ItemStack(ResearchHelper.itemResearchCompass);
    }
}
