package ru.winxboyz.reore.utils;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

public class ResourceData {
    private long cooldown;
    private Material resource;
    private BlockData placeholder;

    public long getCooldown() {
        return cooldown;
    }
    public Material getResource() {
        return resource;
    }
    public BlockData getPlaceholder() {
        return placeholder;
    }

    public ResourceData(Material resource, long cooldown, BlockData placeholder) {
        this.cooldown = cooldown;
        this.resource = resource;
        this.placeholder = placeholder;
    }
    public ResourceData(Material resource, long cooldown) {
        this.cooldown = cooldown;
        this.resource = resource;
        this.placeholder = Material.BEDROCK.createBlockData();
    }
    public ResourceData(Material resource, long cooldown, Material placeholder) {
        this.cooldown = cooldown;
        this.resource = resource;
        this.placeholder = placeholder.createBlockData();
    }
    public ResourceData(long cooldown) {
        this.cooldown = cooldown;
        this.resource = null;
        this.placeholder = null;
    }
}
