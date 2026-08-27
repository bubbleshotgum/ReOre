package ru.winxboyz.reore.managers;

import com.mojang.brigadier.tree.LiteralCommandNode;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import ru.winxboyz.reore.ReOre;
import ru.winxboyz.reore.commands.OreCommand;

public class CommandManager {
    private static CommandManager instance;
    
    private CommandManager() {}

    public static CommandManager getInstance() {
        if (instance == null) {
            instance = new CommandManager();
        }
        return instance;
    }

    private LiteralCommandNode<CommandSourceStack> oreCommand;
    public LiteralCommandNode<CommandSourceStack> getOreCommand() {
        return oreCommand;
    }

    public void initialize(ReOre plugin) {
        oreCommand = new OreCommand(plugin).initialize();

        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(oreCommand);
        });
    }
}
