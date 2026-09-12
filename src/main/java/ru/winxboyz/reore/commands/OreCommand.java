package ru.winxboyz.reore.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.SessionManager;
import com.sk89q.worldedit.session.SessionOwner;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.BlockPositionResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import ru.winxboyz.reore.ReOre;
import ru.winxboyz.reore.managers.DatabaseManager;
import ru.winxboyz.reore.managers.PluginManager;
import ru.winxboyz.reore.utils.OreData;

public class OreCommand {

    private ReOre plugin;

    private ConcurrentHashMap<Location,OreData> ores;
    private Map<Material,Long> timings;
    

    private final LiteralArgumentBuilder<CommandSourceStack>
        ore      = Commands.literal("ore")
        .requires(source -> source.getSender().isOp() && source.getSender() instanceof Player),
        add      = Commands.literal("add"),
        remove   = Commands.literal("remove"),
        list     = Commands.literal("list");
    private final RequiredArgumentBuilder<CommandSourceStack,BlockPositionResolver>
        pos = Commands.argument("pos", ArgumentTypes.blockPosition());

    private DatabaseManager databaseManager = PluginManager.getInstance().getDatabaseManager();

    private void addLocation   (Player player, Location location) {
        World world = player.getWorld();
        if(ores.containsKey(location)) {
            player.sendMessage(Component
                .text("Блок с координатами ")
                    .append(Component.text(location.blockX() 
                        + " " + location.blockY()
                        + " " + location.blockZ())
                    .color(NamedTextColor.GOLD))
                    .append(Component.text(" в мире "))
                    .append(Component.text(world.getName()).color(NamedTextColor.AQUA))
                    .append(Component.text(" уже находится в списке возобновляемых ресурсов!"))
                .color(NamedTextColor.RED));
            return;
        }
        else if(!timings.containsKey(location.getBlock().getType())) {
            // player.sendMessage(Component
            //     .text("Тип блока с координатами ")
            //         .append(Component.text(location.blockX() 
            //             + " " + location.blockY()
            //             + " " + location.blockZ())
            //         .color(NamedTextColor.GOLD))
            //         .append(Component.text(" в мире "))
            //         .append(Component.text(world.getName()).color(NamedTextColor.AQUA))
            //         .append(Component.text(" не входит в список возобновляемых типов"))
            //     .color(NamedTextColor.RED));
            return;
        }
        ores.put(location,OreData.EMPTY);
        databaseManager.addLocation(location);
        player.sendMessage(Component
            .text("Блок с координатами ")
                .append(Component.text(location.blockX()
                    + " " + location.blockY()
                    + " " + location.blockZ())
                .color(NamedTextColor.GOLD))
                .append(Component.text(" в мире "))
                .append(Component.text(world.getName()).color(NamedTextColor.AQUA))
                .append(Component.text(" успешно добавлен в список возобновляемых ресурсов!"))
            .color(NamedTextColor.GREEN)
        );
    }
    private void removeLocation(Player player, Location location) {
        World world = player.getWorld();
        if(ores.containsKey(location)) {
            ores.remove(location);
            databaseManager.removeLocation(location);
            player.sendMessage(Component
                .text("Блок с координатами ")
                    .append(Component.text(location.blockX() 
                        + " " + location.blockY()
                        + " " + location.blockZ())
                    .color(NamedTextColor.GOLD))
                    .append(Component.text(" в мире "))
                    .append(Component.text(world.getName()).color(NamedTextColor.AQUA))
                    .append(Component.text(" успешно удален из списка возобновлемых ресурсов!"))
                .color(NamedTextColor.GREEN));
            return;
        }
        player.sendMessage(Component
            .text("Блок с координатами ")
                .append(Component.text(location.blockX()
                    + " " + location.blockY()
                    + " " + location.blockZ())
                .color(NamedTextColor.GOLD))
                .append(Component.text(" в мире "))
                .append(Component.text(world.getName()).color(NamedTextColor.AQUA))
                .append(Component.text(" не состоит в списке возобновлемых ресурсов!"))
            .color(NamedTextColor.RED)
        );
    }
    private int handleLocations(Player player, String mode) {
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
                if(mode.equals("add") && timings.containsKey(loc.getBlock().getType())) {
                    ores.put(loc,OreData.EMPTY);
                    locs.add(loc);
                }
                else if(ores.containsKey(loc)) {
                    ores.remove(loc);
                    locs.add(loc);
                }
            }
            BlockVector3 min = region.getMinimumPoint(), max = region.getMaximumPoint();
            player.sendMessage(
                Component.text("Ресурсы в Регионе ")
                .append(Component.text(min.x() + " " + min.y() + " " + min.z()).color(NamedTextColor.GOLD))
                .append(Component.text(" <—> "))
                .append(Component.text(max.x() + " " + max.y() + " " + max.z()).color(NamedTextColor.GOLD))
                .append(Component.text(" в мире "))
                .append(Component.text(world.getName()).color(NamedTextColor.AQUA))
                .append(Component.text(" теперь " + (mode.equals("add") ? "" : "не") + "возобновляемые!")).color(NamedTextColor.GREEN)
            );
            if(mode.equals("add"))
                databaseManager.addLocations   (locs);
            else
                databaseManager.removeLocations(locs);
        } catch(IncompleteRegionException e) {}
        return Command.SINGLE_SUCCESS;
    }

    private final Command<CommandSourceStack>
    executeAdd          = ctx -> {
        Player player = (Player) ctx.getSource().getSender();
        World world = player.getWorld();

        BlockPositionResolver resolver = ctx.getArgument("pos", BlockPositionResolver.class);
        try {
            Location location = resolver.resolve(ctx.getSource()).toLocation(world).toBlockLocation();
            addLocation    (player, location);
        } catch(CommandSyntaxException e) {
            plugin.getLogger().warning("Синтаксическая ошибка при выполнении команды add:\n" + e.getMessage());
        }
        return Command.SINGLE_SUCCESS;
    },
    executeAddRegion    = ctx -> {
        handleLocations((Player) ctx.getSource().getSender(), "add");
        return Command.SINGLE_SUCCESS;
    },
    executeRemove       = ctx -> {
        Player player = (Player) ctx.getSource().getSender();
        World world = player.getWorld();

        BlockPositionResolver resolver = ctx.getArgument("pos", BlockPositionResolver.class);
        try {
            Location location = resolver.resolve(ctx.getSource()).toLocation(world).toBlockLocation();
            removeLocation    (player, location);
        }  catch(CommandSyntaxException e) {
            plugin.getLogger().warning("Синтаксическая ошибка при выполнении команды remove:\n" + e.getMessage());
        }
        return Command.SINGLE_SUCCESS;
    },
    executeRemoveRegion = ctx -> {
        handleLocations((Player) ctx.getSource().getSender(), "remove");
        return Command.SINGLE_SUCCESS;
    };
      
    public LiteralCommandNode<CommandSourceStack> initialize() {
        LiteralArgumentBuilder<CommandSourceStack> builder = ore
            .then(add   .executes(executeAddRegion)   .then(pos.executes(executeAdd)))
            .then(remove.executes(executeRemoveRegion).then(pos.executes(executeRemove)))
            .then(list.executes(ctx -> {
                Player player = (Player) ctx.getSource().getSender();
                List<World> worlds = new ArrayList<>(0);
                for(Location loc : ores.keySet())
                    if(!worlds.contains(loc.getWorld()))
                        worlds.add(loc.getWorld());
                Component output = Component.text("---------------------");
                for(World world : worlds) {
                    output = output.append(Component.text("\n" + world.getName()).color(NamedTextColor.AQUA));
                    for(Location loc : ores.keySet())
                        if(loc.getWorld() == world)
                            output = output.append(
                                Component.text(
                                    "\n"
                                    + loc.blockX()
                                    + " " + loc.blockY()
                                    + " " + loc.blockZ()).color(NamedTextColor.GOLD));
                }
                player.sendMessage(output);

                return Command.SINGLE_SUCCESS;
            }));
        return builder.build();
    }

    public OreCommand(ReOre plugin) {
        this.plugin = plugin;
        ores = plugin.getOres();
        timings = plugin.getDefaultTimings();
    }
}
