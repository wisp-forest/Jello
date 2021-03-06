package io.wispforest.jello.compat;

import io.wispforest.jello.Jello;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import java.util.ArrayList;
import java.util.List;

@Config(name = Jello.MODID)
public class JelloConfig implements ConfigData {

    @ConfigEntry.Category("common")
    @ConfigEntry.Gui.Tooltip
    public boolean enableDyeingEntities = true;

    @ConfigEntry.Category("common")
    @ConfigEntry.Gui.Tooltip
    public boolean enableDyeingPlayers = true;

    @ConfigEntry.Category("common")
    @ConfigEntry.Gui.Tooltip
    public boolean enableDyeingBlocks = true;

    @ConfigEntry.Category("client")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.RequiresRestart
    public boolean addCustomJsonColors = true;

    @ConfigEntry.Category("client")
    @ConfigEntry.Gui.Tooltip
    public boolean enableTransparencyFixCauldrons = true;

    @ConfigEntry.Category("client")
    @ConfigEntry.Gui.Tooltip
    public boolean enableGrayScalingOfEntities = false;

    @ConfigEntry.Category("client")
    @ConfigEntry.Gui.Tooltip
    public boolean enableGrayScaleRainbowEntities = false;

    @ConfigEntry.Category("client")
    @ConfigEntry.Gui.Tooltip
    public List<String> grayScaledBlackListModid = new ArrayList<>();

    @ConfigEntry.Category("client")
    @ConfigEntry.Gui.Tooltip
    public List<String> dyeColorBlackListModid = new ArrayList<>();
}



