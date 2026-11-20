package dev.pupwired.mathesis.core.command;

import org.bukkit.command.CommandSender;
import java.util.List;

/** One subcommand. {@code args} holds only what comes after this subcommand's own name. */
public interface Subcommand {
    String name();

    void execute(CommandSender sender, String label, String[] args);

    default List<String> complete(CommandSender sender, String[] args) {
        return List.of();
    }
}