package ru.winxboyz.reore.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.BlockPositionResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import ru.winxboyz.reore.ReOre;
import ru.winxboyz.reore.managers.CommandManager;
import ru.winxboyz.reore.managers.DatabaseManager;
import ru.winxboyz.reore.managers.PluginManager;

public class ReOreCommand {

    private ReOre plugin;
    private CommandManager commandManager;

    private final LiteralArgumentBuilder<CommandSourceStack>
        ore      = Commands.literal("reore")
        .requires(source -> source.getSender().isOp() && source.getSender() instanceof Player),
        add      = Commands.literal("add"),
        remove   = Commands.literal("remove"),
        list     = Commands.literal("list"),
        reload   = Commands.literal("reload");
    private final RequiredArgumentBuilder<CommandSourceStack,BlockPositionResolver>
        pos  = Commands.argument("pos", ArgumentTypes.blockPosition());
    private final RequiredArgumentBuilder<CommandSourceStack,Integer>
        page = Commands.argument("page", IntegerArgumentType.integer(1)); 
    

    private DatabaseManager databaseManager = PluginManager.getInstance().getDatabaseManager();

    private final Command<CommandSourceStack>
    executeAdd          = ctx -> {
        Player player = (Player) ctx.getSource().getSender();
        World world = player.getWorld();

        BlockPositionResolver resolver = ctx.getArgument("pos", BlockPositionResolver.class);
        try {
            Location location = resolver.resolve(ctx.getSource()).toLocation(world).toBlockLocation();
            commandManager.addLocation(player, location, databaseManager);
        } catch(CommandSyntaxException e) {
            plugin.getLogger().warning(commandManager.getMessages().get("syntax-error") + " add:\n" + e.getMessage());
        }
        return Command.SINGLE_SUCCESS;
    },
    executeAddRegion    = ctx -> {
        commandManager.handleLocations((Player) ctx.getSource().getSender(), "add", databaseManager);
        return Command.SINGLE_SUCCESS;
    },
    executeRemove       = ctx -> {
        Player player = (Player) ctx.getSource().getSender();
        World world = player.getWorld();

        BlockPositionResolver resolver = ctx.getArgument("pos", BlockPositionResolver.class);
        try {
            Location location = resolver.resolve(ctx.getSource()).toLocation(world).toBlockLocation();
            commandManager.removeLocation(player, location, databaseManager);
        }  catch(CommandSyntaxException e) {
            plugin.getLogger().warning(commandManager.getMessages().get("syntax-error") + " remove:\n" + e.getMessage());
        }
        return Command.SINGLE_SUCCESS;
    },
    executeRemoveRegion = ctx -> {
        commandManager.handleLocations((Player) ctx.getSource().getSender(), "remove", databaseManager);
        return Command.SINGLE_SUCCESS;
    };
    
    private final int LIMIT = 10;

    private void displayPage(Player player, int page) {
        databaseManager.fetchLocations(LIMIT, page).thenAccept(locs -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                Component output = Component.text("--------------------------------------");
                for(Location loc : locs)
                    output = output
                        .append(Component.text("\n"+loc.getWorld().getName()).color(NamedTextColor.AQUA))
                        .append(Component.text(" " + loc.blockX() + " " + loc.blockY() + " " + loc.blockZ()).color(NamedTextColor.GOLD));
                player.sendMessage(output);
            });
        });
    }

    public LiteralCommandNode<CommandSourceStack> initialize() {
        LiteralArgumentBuilder<CommandSourceStack> builder = ore
            .then(add   .executes(executeAddRegion)   .then(pos.executes(executeAdd)))
            .then(remove.executes(executeRemoveRegion).then(pos.executes(executeRemove)))
            .then(list.executes(ctx -> {
                Player player = (Player) ctx.getSource().getSender();
                displayPage(player, 1);

                return Command.SINGLE_SUCCESS;
            }).then(page.executes(ctx -> {
                Player player = (Player) ctx.getSource().getSender();
                int pg = ctx.getArgument("page", Integer.class);
                displayPage(player, pg);

                return Command.SINGLE_SUCCESS;
            })))
            .then(reload.executes(ctx -> {
                Player player = (Player) ctx.getSource().getSender();
                plugin.reload();
                player.sendMessage(Component.text(commandManager.getMessages().get("reloaded")).color(NamedTextColor.GREEN));
                return Command.SINGLE_SUCCESS;
            }));
        return builder.build();
    }

    public ReOreCommand(ReOre plugin) {
        this.plugin = plugin;
        this.commandManager = PluginManager.getInstance().getCommandManager();
    }
}
