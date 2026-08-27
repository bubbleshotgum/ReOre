package ru.winxboyz.reore.listeners;

import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import ru.winxboyz.reore.ReOre;
import ru.winxboyz.reore.utils.OreData;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class BlockListener implements Listener {
    
    private ReOre plugin;

    private final Map<Material,Long> timings;
    private final ConcurrentHashMap<Location,OreData> ores;

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        World world = player.getWorld();
        if(!plugin.getWorlds().contains(world)) return;
        
        Block block = event.getBlock();
        
        Location loc = block.getLocation();
        
        if(!ores.containsKey(loc)) return;

        Material blockType = block.getType();

        if(!timings.keySet().contains(blockType)) {
            plugin.getLogger().warning(
                "Блок с координатами "
                + loc.blockX() + " " + loc.blockY() + " " + loc.blockZ()
                + " сохранен в списке руд, но не является рудой");
            return;
        }

        event.setCancelled(true);

        ItemStack tool = player.getInventory().getItemInMainHand();
        Collection<ItemStack> drops = block.getDrops(tool,player);

        for(ItemStack drop : drops)
            player.getInventory().addItem(drop);
        player.giveExp(4);

        block.setType(Material.BEDROCK);

        long timing = System.currentTimeMillis() + timings.get(blockType);
        OreData data = new OreData(blockType, timing);
        ores.put(loc, data);
    }

    @EventHandler
    public void onBlockDamage(PlayerInteractEvent event) {
        if(!(event.getAction() == Action.LEFT_CLICK_BLOCK)) return;
        Block block = event.getClickedBlock();
        
        Location loc = block.getLocation();
        if(!ores.containsKey(loc)) return;

        if(block.getType() == Material.BEDROCK) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(
                Component.text("Руда восстановится через ").color(NamedTextColor.RED)
                .append(Component.text(ores.get(loc).toString()).color(NamedTextColor.AQUA))
            );
        }
    }

    public BlockListener(ReOre plugin) {
        this.plugin = plugin;
        this.timings = plugin.getDefaultTimings();
        this.ores = plugin.getOres();
    }
}
