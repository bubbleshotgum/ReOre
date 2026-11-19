package ru.winxboyz.reore.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.mojang.brigadier.Command;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.SessionManager;
import com.sk89q.worldedit.session.SessionOwner;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import ru.winxboyz.reore.ReOre;
import ru.winxboyz.reore.utils.ResourceData;

public class CommandManager {
    private ReOre plugin;;
    private ConcurrentHashMap<Location,ResourceData> registries;
    private ConcurrentHashMap<Location,Long> cooldowns;
    
    private List<ResourceData> resources;
    private Map<String,String> messages;
    
    public Map<String,String> getMessages() {
        return messages;
    }

    public CommandManager(ReOre plugin) {
        this.plugin = plugin;
    }


    public void addLocation   (Player player, Location location, DatabaseManager databaseManager) {
        World world = player.getWorld();
        if(registries.containsKey(location)) {
            player.sendMessage(Component
                .text(messages.get("block-with-coordinates") + " ")
                    .append(Component.text(location.blockX() 
                        + " " + location.blockY()
                        + " " + location.blockZ())
                    .color(NamedTextColor.GOLD))
                    .append(Component.text(" " + messages.get("in-world") + " "))
                    .append(Component.text(world.getName()).color(NamedTextColor.AQUA))
                    .append(Component.text(" " + messages.get("already") + " " + messages.get("in-list")))
                .color(NamedTextColor.RED));
            return;
        }
        ResourceData data = resources.stream().filter(
            rd -> location.getBlock().getType().equals(rd.getResource())
        ).findFirst().orElse(null);
        if(data == null)
            return;
        registries.put(location,data);
        databaseManager.addLocation(location);
        player.sendMessage(Component
            .text(messages.get("block-with-coordinates") + " ")
                .append(Component.text(location.blockX()
                    + " " + location.blockY()
                    + " " + location.blockZ())
                .color(NamedTextColor.GOLD))
                .append(Component.text(" " + messages.get("in-world") + " "))
                .append(Component.text(world.getName()).color(NamedTextColor.AQUA))
                .append(Component.text(" " + messages.get("success") + " " + messages.get("added")))
            .color(NamedTextColor.GREEN)
        );
    }
    public void removeLocation(Player player, Location location, DatabaseManager databaseManager) {
        World world = player.getWorld();
        if(registries.containsKey(location)) {
            if(cooldowns.containsKey(location)) {
                location.getBlock().setType(registries.get(location).getResource());
                cooldowns.remove(location);
            }
            registries.remove(location);
            databaseManager.removeLocation(location);
            player.sendMessage(Component
                .text(messages.get("block-with-coordinates") + " ")
                    .append(Component.text(location.blockX() 
                        + " " + location.blockY()
                        + " " + location.blockZ())
                    .color(NamedTextColor.GOLD))
                    .append(Component.text(" " + messages.get("in-world") + " "))
                    .append(Component.text(world.getName()).color(NamedTextColor.AQUA))
                    .append(Component.text(" " + messages.get("success") + " " + messages.get("removed")))
                .color(NamedTextColor.GREEN));
            return;
        }
        player.sendMessage(Component
            .text(messages.get("block-with-coordinates") + " ")
                .append(Component.text(location.blockX()
                    + " " + location.blockY()
                    + " " + location.blockZ())
                .color(NamedTextColor.GOLD))
                .append(Component.text(" " + messages.get("in-world") + " "))
                .append(Component.text(world.getName()).color(NamedTextColor.AQUA))
                .append(Component.text(" " + messages.get("not") + " " + messages.get("in-list")))
            .color(NamedTextColor.RED)
        );
    }
    public int handleLocations(Player player, String mode, DatabaseManager databaseManager) {
        World world = player.getWorld();
        
        if(!plugin.getServer().getPluginManager().isPluginEnabled("WorldEdit")) return Command.SINGLE_SUCCESS;

        SessionOwner owner = BukkitAdapter.adapt(player);

        SessionManager sessionManager = WorldEdit.getInstance().getSessionManager();

        LocalSession session = sessionManager.getIfPresent(owner);
        try {
            Region region = session.getSelection(BukkitAdapter.adapt(world));
            List<Location> locs = new ArrayList<>(0);
            for(BlockVector3 vec3 : region) {
                Location loc = new Location(world, vec3.x(), vec3.y(), vec3.z()); 
                ResourceData data = resources.stream().filter(
                    rd -> loc.getBlock().getType().equals(rd.getResource())
                ).findFirst().orElse(null);
                if(mode.equals("add")) { 
                    if(data != null && !registries.containsKey(loc)) {
                        registries.put(loc, data);
                        locs.add(loc);
                    }
                }
                else if(mode.equals("remove")) {
                    if(registries.containsKey(loc)) {
                        if(cooldowns.containsKey(loc)) {
                            loc.getBlock().setBlockData(registries.get(loc).getPlaceholder());
                            cooldowns.remove(loc);
                        }
                        registries.remove(loc);
                        locs.add(loc);
                    }
                }
            }
            BlockVector3 min = region.getMinimumPoint(), max = region.getMaximumPoint();
            player.sendMessage(
                Component.text(messages.get("resources-in-region") + " ")
                .append(Component.text(min.x() + " " + min.y() + " " + min.z()).color(NamedTextColor.GOLD))
                .append(Component.text(" <—> "))
                .append(Component.text(max.x() + " " + max.y() + " " + max.z()).color(NamedTextColor.GOLD))
                .append(Component.text(" " + messages.get("in-world") + " "))
                .append(Component.text(world.getName()).color(NamedTextColor.AQUA))
                .append(Component.text(" " + messages.get("now") + " " + (mode.equals("add") ? "" : messages.get("non")) + messages.get("renewable") + "!")).color(NamedTextColor.GREEN)
            );
            if(mode.equals("add"))
                databaseManager.addLocations   (locs);
            else
                databaseManager.removeLocations(locs);
        }
        catch(IncompleteRegionException e) {}
        catch(NullPointerException e) {
            player.sendMessage(Component.text(messages.get("no-selection")).color(NamedTextColor.RED));
        }

        return Command.SINGLE_SUCCESS;
    }

}
