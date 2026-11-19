package ru.winxboyz.reore.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import ru.winxboyz.reore.managers.RestorationManager;

public class BlockDamageListener implements Listener {
    
    private RestorationManager restorationManager;

    @EventHandler 
    public void onBlockDamage(PlayerInteractEvent event) {
        if(!(event.getAction() == org.bukkit.event.block.Action.LEFT_CLICK_BLOCK)) return;
        if(restorationManager.handleBlockDamage(event.getClickedBlock(), event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    public BlockDamageListener(RestorationManager restorationManager) {
        this.restorationManager = restorationManager;
    }
}
