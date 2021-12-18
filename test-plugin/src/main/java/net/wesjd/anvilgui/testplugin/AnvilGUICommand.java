package net.wesjd.anvilgui.testplugin;

import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;

public class AnvilGUICommand implements CommandExecutor {

    private final TestPlugin plugin;

    private final Map<String, Map.Entry<String, BiConsumer<AnvilGUI.Builder, String>>> builderModifier = new HashMap<>();

    public AnvilGUICommand(TestPlugin plugin) {
        this.plugin = plugin;

        builderModifier.put("preventclose", new AbstractMap.SimpleEntry<>("preventclose", (builder, arg) -> builder.preventClose()));
        builderModifier.put("title", new AbstractMap.SimpleEntry<>("title=Some-Text", AnvilGUI.Builder::title));
        builderModifier.put("text", new AbstractMap.SimpleEntry<>("text=Some-Text-for-the-item", AnvilGUI.Builder::text));
        builderModifier.put("itemleft", new AbstractMap.SimpleEntry<>("itemright=GRASS_BLOCK",
                (builder, arg) -> builder.itemLeft(new ItemStack(Material.matchMaterial(arg)))));
        builderModifier.put("itemright", new AbstractMap.SimpleEntry<>("itemleft=GRASS_BLOCK",
                (builder, arg) -> builder.itemLeft(new ItemStack(Material.matchMaterial(arg)))));
        builderModifier.put("oncomplete", new AbstractMap.SimpleEntry<>(
                "oncomplete=close closes the anvilgui\noncomplete=text replaces the current text with 'text'\noncomplete=inventory opens the own inventory",
                (builder, arg) -> builder.onComplete((player, text) -> {
                    player.sendMessage(text);

                    switch (arg.toLowerCase(Locale.ROOT)) {
                        default:
                        case "close":
                            return AnvilGUI.Response.close();
                        case "text":
                            return AnvilGUI.Response.text("text");
                        case "inventory":
                            return AnvilGUI.Response.openInventory(player.getInventory());
                    }
                })));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player))
            return false;
        if (args.length == 0) {
            builderModifier.values().stream().map(Map.Entry::getKey).forEach(sender::sendMessage);
            return true;
        }

        Player player = (Player) sender;

        AnvilGUI.Builder builder = new AnvilGUI.Builder().plugin(plugin);

        for (String arg : args) {
            int splitIndex = arg.indexOf('=');
            String key = arg, value = "";
            if (splitIndex >= 0) {
                key = arg.substring(0, splitIndex);
                value = arg.substring(splitIndex + 1);
            }

            if (builderModifier.containsKey(key)) {
                builderModifier.get(key).getValue().accept(builder, value);
            } else {
                player.sendMessage("BuilderModifier " + key + " not found");
            }
        }

        builder.onLeftInputClick(p -> p.sendMessage("Left input clicked"));
        builder.onRightInputClick(p -> p.sendMessage("Right input clicked"));
        builder.onClose(p -> p.sendMessage("Closed"));

        builder.open(player);
        return true;
    }
}
