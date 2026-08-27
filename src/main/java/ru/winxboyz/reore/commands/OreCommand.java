package ru.winxboyz.reore.commands;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.BlockPositionResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import ru.winxboyz.reore.ReOre;
import ru.winxboyz.reore.utils.OreData;

public class OreCommand {

    private ReOre plugin;

    private final LiteralArgumentBuilder<CommandSourceStack>
        ore      = Commands.literal("ore")
        .requires(source -> source.getSender().isOp() && source.getSender() instanceof Player),
        add      = Commands.literal("add"),
        remove   = Commands.literal("remove");
    private final RequiredArgumentBuilder<CommandSourceStack,BlockPositionResolver>
        pos = Commands.argument("pos", ArgumentTypes.blockPosition());

    private int addLocation   (CommandContext<CommandSourceStack> ctx) {
        Player player = (Player) ctx.getSource().getSender();
        World world = player.getWorld();

        BlockPositionResolver resolver = ctx.getArgument("pos", BlockPositionResolver.class);
        try {
            Location location = resolver.resolve(ctx.getSource()).toLocation(world).toBlockLocation();
            ConcurrentHashMap<Location,OreData> ores = plugin.getOres();
            if(ores.containsKey(location)) {
                player.sendMessage(Component
                    .text("Блок с координатами ")
                        .append(Component.text(location.blockX() 
                            + " " + location.blockY()
                            + " " + location.blockZ())
                        .color(NamedTextColor.GOLD))
                        .append(Component.text(" в мире "))
                        .append(Component.text(world.getName()).color(NamedTextColor.AQUA))
                        .append(Component.text(" уже находится в списке руд"))
                    .color(NamedTextColor.RED));
                return Command.SINGLE_SUCCESS;
            }
            ores.put(location,OreData.EMPTY);
            plugin.getConfig().set("locations", new ArrayList<>(ores.keySet()));
            plugin.saveConfig();
            player.sendMessage(Component
                .text("Блок с координатами ")
                    .append(Component.text(location.blockX()
                        + " " + location.blockY()
                        + " " + location.blockZ())
                    .color(NamedTextColor.GOLD))
                    .append(Component.text(" в мире "))
                    .append(Component.text(world.getName()).color(NamedTextColor.AQUA))
                    .append(Component.text(" успешно добавлен в список руд"))
                .color(NamedTextColor.GREEN)
            );
        } catch(CommandSyntaxException e) {
            plugin.getLogger().warning("Синтаксическая ошибка при выполнении команды add:\n" + e.getMessage());
        }
        return Command.SINGLE_SUCCESS;
    }
    private int removeLocation(CommandContext<CommandSourceStack> ctx) {
        Player player = (Player) ctx.getSource().getSender();
        World world = player.getWorld();

        BlockPositionResolver resolver = ctx.getArgument("pos", BlockPositionResolver.class);
        try {
            Location location = resolver.resolve(ctx.getSource()).toLocation(world).toBlockLocation();
            ConcurrentHashMap<Location,OreData> ores = plugin.getOres();
            if(ores.containsKey(location)) {
                ores.remove(location);
                plugin.getConfig().set("locations", new ArrayList<>(ores.keySet()));
                plugin.saveConfig();
                player.sendMessage(Component
                    .text("Блок с координатами ")
                        .append(Component.text(location.blockX() 
                            + " " + location.blockY()
                            + " " + location.blockZ())
                        .color(NamedTextColor.GOLD))
                        .append(Component.text(" в мире "))
                        .append(Component.text(world.getName()).color(NamedTextColor.AQUA))
                        .append(Component.text(" успешно удален из списка руд"))
                    .color(NamedTextColor.GREEN));
                return Command.SINGLE_SUCCESS;
            }
            player.sendMessage(Component
                .text("Блок с координатами ")
                    .append(Component.text(location.blockX()
                        + " " + location.blockY()
                        + " " + location.blockZ())
                    .color(NamedTextColor.GOLD))
                    .append(Component.text(" в мире "))
                    .append(Component.text(world.getName()).color(NamedTextColor.AQUA))
                    .append(Component.text(" не состоит в списке руд"))
                .color(NamedTextColor.RED)
            );
        } catch(CommandSyntaxException e) {
            plugin.getLogger().warning("Синтаксическая ошибка при выполнении команды add:\n" + e.getMessage());
        }
        return Command.SINGLE_SUCCESS;
    }

    private final Command<CommandSourceStack>
        executeAdd    = ctx -> addLocation   (ctx),
        executeRemove = ctx -> removeLocation(ctx);
      
    public LiteralCommandNode<CommandSourceStack> initialize() {
        LiteralArgumentBuilder<CommandSourceStack> builder = ore
            .then(add   .then(pos.executes(executeAdd)))
            .then(remove.then(pos.executes(executeRemove)));
        return builder.build();
    }

    public OreCommand(ReOre plugin) {
        this.plugin = plugin;
    }
}
