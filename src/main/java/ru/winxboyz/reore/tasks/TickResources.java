package ru.winxboyz.reore.tasks;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;

import ru.winxboyz.reore.utils.ResourceData;

public class TickResources {

    private final ConcurrentHashMap<Location,ResourceData> registries;
    private final ConcurrentHashMap<Location,Long> cooldowns;

    public void tick() {
        if(registries.isEmpty()) return;
        long currentTime = System.currentTimeMillis();
        Iterator<Map.Entry<Location,Long>> iterator = cooldowns.entrySet().iterator();
        while(iterator.hasNext()) {
            Map.Entry<Location,Long> entry = iterator.next();
            Location loc = entry.getKey();
            Long cooldown = entry.getValue();
            if(cooldown > currentTime) continue;
            ResourceData data = registries.get(loc);
            if(data == null) {
                iterator.remove();
                continue;
            }
            if(loc.getWorld().isChunkLoaded(loc.blockX() >> 4, loc.blockZ() >> 4)) {
                loc.getBlock().setType(data.getResource());
                iterator.remove();
            }
        }
    }
    public void cancelTicks(boolean shouldEmpty) {
        Iterator<Map.Entry<Location,Long>> iterator = cooldowns.entrySet().iterator();
        while(iterator.hasNext()) {
            Map.Entry<Location,Long> entry = iterator.next();
            Location loc = entry.getKey();

            loc.getBlock().setType(registries.get(loc).getResource());
            iterator.remove();
        }
        if(shouldEmpty) {
            registries.clear();
        }
    }

    public TickResources(
        ConcurrentHashMap<Location,ResourceData> resources,
        ConcurrentHashMap<Location,Long> cooldowns
    ) {
        this.registries = resources;
        this.cooldowns = cooldowns;
    }
}
