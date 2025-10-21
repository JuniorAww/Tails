package me.junioraww.tails.data.types.items;

import me.junioraww.tails.data.types.wallet.Item;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class Graffiti extends Item {
    static MiniMessage serializer = MiniMessage.miniMessage();

    private Meta meta;

    public Graffiti(int id) {
        super(id, Type.GRAFFITI);
    }

    @Override
    public ItemStack getDisplay() {
        ItemStack display = new ItemStack(Material.FILLED_MAP);
        ItemMeta meta = display.getItemMeta();
        meta.displayName(serializer.deserialize("<gold>Граффити"));
        display.setItemMeta(meta);
        return display;
    }

    private Component equipped = serializer.deserialize("<gold>Нажмите ЛКМ для добавления в набор");
    private Component unequipped = serializer.deserialize("<red>Нажмите ЛКМ для удаления с набора");

    @Override
    public ItemStack getFullDisplay() {
        ItemStack display = new ItemStack(Material.FILLED_MAP);
        ItemMeta meta = display.getItemMeta();
        meta.displayName(serializer.deserialize("<gold>Граффити"));
        List<Component> lore = new ArrayList<>();
        lore.add(used ? equipped : unequipped);
        meta.lore(lore);
        display.setItemMeta(meta);
        return display;
    }

    public class Meta {
        private String uri;
    }
}
