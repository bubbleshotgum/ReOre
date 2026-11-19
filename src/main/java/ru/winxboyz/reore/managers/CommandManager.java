package ru.winxboyz.reore.managers;

import com.mojang.brigadier.tree.LiteralCommandNode;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import ru.winxboyz.reore.ReOre;
import ru.winxboyz.reore.commands.ReOreCommand;

public class CommandManager {
    private ReOre plugin;;
    
    public CommandManager(ReOre plugin) {
        this.plugin = plugin;
    }

    private LiteralCommandNode<CommandSourceStack> reoreCommand;
    public LiteralCommandNode<CommandSourceStack> getReoreCommand() {
        return reoreCommand;
    }

    public void initialize() {
        reoreCommand = new ReOreCommand(plugin).initialize();

        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(reoreCommand);
        });
    }
}
