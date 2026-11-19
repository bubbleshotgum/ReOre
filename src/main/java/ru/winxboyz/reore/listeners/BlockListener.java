package ru.winxboyz.reore.listeners;

import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import ru.winxboyz.reore.ReOre;
import ru.winxboyz.reore.utils.ResourceData;
import ru.winxboyz.reore.utils.TimeMessage;

import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class BlockListener implements Listener {

    private final ReOre plugin;
    private final ConcurrentHashMap<Location,ResourceData> registries;
    private final ConcurrentHashMap<Location,Long> cooldowns;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        
        Block block = event.getBlock();
        Location loc = block.getLocation();
        
        if(!registries.containsKey(loc)) return;

        ResourceData data = registries.get(loc);
        if(data == null) return;

        int exp = event.getExpToDrop();

        event.setCancelled(true);
        
        ItemStack tool = player.getInventory().getItemInMainHand();
        Collection<ItemStack> drops = block.getDrops(tool,player);
        

        for(ItemStack drop : drops) {
            HashMap<Integer, ItemStack> leftOvers = player.getInventory().addItem(drop);
            for(ItemStack item : leftOvers.values())
                loc.getWorld().dropItem(loc, item);
        }
        tool.damage(1, player);
        player.giveExp(exp);
        block.setBlockData(data.getPlaceholder());

        long cooldown = System.currentTimeMillis() + data.getCooldown();
        cooldowns.put(loc, cooldown);
    }

    @EventHandler
    public void onBlockDamage(PlayerInteractEvent event) {
        if(!(event.getAction() == Action.LEFT_CLICK_BLOCK)) return;
        Block block = event.getClickedBlock();
        
        Location loc = block.getLocation();
        if(!registries.containsKey(loc)) return;

        if(cooldowns.containsKey(loc)) {
            event.setCancelled(true);
            int cooldownSeconds = (int) (cooldowns.get(loc) - System.currentTimeMillis()) / 1000;
            event.getPlayer().sendActionBar(
                Component.text(plugin.getMessages().get("cooldown") + " ").color(NamedTextColor.RED)
                .append(Component.text(TimeMessage.toString(cooldownSeconds, plugin.LOCALE())).color(NamedTextColor.AQUA))
            );
        }
    }

    public BlockListener(ReOre plugin) {
        this.plugin = plugin;
        this.registries = plugin.getRegistries();
        this.cooldowns = plugin.getCooldowns();
    }
}
