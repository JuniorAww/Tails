package me.junioraww.tails.commands;

import me.junioraww.tails.Main;
import me.junioraww.tails.storages.Wallet;
import me.junioraww.tails.utils.Locales;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.ScrollGui;
import xyz.xenondevs.invui.gui.structure.Markers;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.item.impl.controlitem.ScrollItem;
import xyz.xenondevs.invui.window.Window;

import java.util.*;

public class Items implements CommandExecutor {
    static Item border = new SimpleItem(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setDisplayName(""));

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (sender instanceof Player player) {
            Wallet wallet = Main.getPlugin().getStorage().getWallet(player);

            List<Item> items = wallet.getItems().stream().map(item -> (Item) new SimpleItem(item.getFullDisplay())).toList();
            /*Arrays.stream(Material.values()).filter(item -> !item.isAir() && item.isItem())
            .map(item -> (Item) new SimpleItem(new ItemBuilder(item)))
            .toList();*/

            var gui = ScrollGui.items()
                    .setStructure(
                            "x x x x x x x x u",
                            "x x x x x x x x #",
                            "x x x x x x x x #",
                            "x x x x x x x x #",
                            "x x x x x x x x d")
                    .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
                    .addIngredient('#', border)
                    .addIngredient('u', new ScrollUpItem(player.locale()))
                    .addIngredient('d', new ScrollDownItem(player.locale()))
                    .setContent(items)
                    .build();

            String title = LegacyComponentSerializer.legacyAmpersand().serialize(
                    Locales.Text.Inventory.get(player)
            );

            var window = Window.single()
                    .setViewer(player)
                    .setTitle(title)
                    .setGui(gui)
                    .build();

            window.open();
            return true;
        }

        return false;
    }

    public static class ScrollDownItem extends ScrollItem {
        private Locale locale;

        public ScrollDownItem(Locale locale) {
            super(1);
            this.locale = locale;
        }

        @Override
        public ItemProvider getItemProvider(ScrollGui<?> gui) {
            ItemBuilder builder = new ItemBuilder(Material.GREEN_STAINED_GLASS_PANE);
            builder.setDisplayName(Locales.Text.ScrollDown.getAmpersand(locale));
            if (!gui.canScroll(1))
                builder.addLoreLines(Locales.Text.ScrollDownMax.getAmpersand(locale));

            return builder;
        }
    }

    public static class ScrollUpItem extends ScrollItem {
        private Locale locale;

        public ScrollUpItem(Locale locale) {
            super(-1);
            this.locale = locale;
        }

        @Override
        public ItemProvider getItemProvider(ScrollGui<?> gui) {
            ItemBuilder builder = new ItemBuilder(Material.RED_STAINED_GLASS_PANE);
            builder.setDisplayName(Locales.Text.ScrollUp.getAmpersand(locale));
            if (!gui.canScroll(-1))
                builder.addLoreLines(Locales.Text.ScrollUpMax.getAmpersand(locale));

            return builder;
        }

    }
}

