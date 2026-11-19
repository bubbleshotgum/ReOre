package ru.winxboyz.reore.managers;

import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import ru.winxboyz.reore.ReOre;
import ru.winxboyz.reore.utils.ResourceData;
import ru.winxboyz.reore.utils.TimeMessage;

public class RestorationManager {

    private ReOre plugin;
    private final ConcurrentHashMap<Location,ResourceData> registries;
    private final ConcurrentHashMap<Location,Long> cooldowns;


    public boolean isResource(Location loc) {
        return registries.containsKey(loc);
    }

    public boolean isOnCooldown(Location loc) {
        return cooldowns.containsKey(loc);
    }

    public boolean shouldRestore(Location loc) {
        if(!registries.containsKey(loc)) return false;
        if(!cooldowns.containsKey(loc)) return true;
        return cooldowns.get(loc) <= System.currentTimeMillis();
    }

    public boolean handleBlockBreak(Player player, Block block, int exp) {
        Location loc = block.getLocation();
        
        if(!registries.containsKey(loc)) return false;

        ResourceData data = registries.get(loc);
        if(data == null) return false;

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
        return true;
    }

    public boolean handleBlockDamage(Block block, Player player) {
        Location loc = block.getLocation();
        if(isResource(loc) && isOnCooldown(loc)) {
            int cooldownSeconds = (int) (cooldowns.get(loc) - System.currentTimeMillis()) / 1000;
            player.sendActionBar(
                Component.text(plugin.getMessages().get("cooldown") + " ").color(NamedTextColor.RED)
                .append(Component.text(TimeMessage.toString(cooldownSeconds, plugin.getLocale())).color(NamedTextColor.AQUA))
            );
            return true;
        }
        return false;
    }

    public RestorationManager(ReOre plugin) {
        this.plugin = plugin;
        this.registries = plugin.getRegistries();
        this.cooldowns = plugin.getCooldowns();
    }

}
