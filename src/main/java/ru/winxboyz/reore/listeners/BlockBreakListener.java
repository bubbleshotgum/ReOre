package ru.winxboyz.reore.listeners;

import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import ru.winxboyz.reore.managers.RestorationManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class BlockBreakListener implements Listener {

    private RestorationManager restorationManager;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if(restorationManager.handleBlockBreak(event.getPlayer(), event.getBlock(), event.getExpToDrop())) {
            event.setCancelled(true);
        }
    }

    public BlockBreakListener(RestorationManager restorationManager) {
        this.restorationManager = restorationManager;
    }
}
