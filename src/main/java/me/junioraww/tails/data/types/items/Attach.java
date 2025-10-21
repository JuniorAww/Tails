package me.junioraww.tails.data.types.items;

import me.junioraww.tails.data.types.wallet.Item;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Attach extends Item {
    static MiniMessage serializer = MiniMessage.miniMessage();

    private Meta meta;

    public Attach(int id) {
        super(id, Type.ATTACH);
    }

    public enum AttachType {
        BACK,
        HEAD
    }

    public Meta getMeta() {
        return meta;
    }

    private static Component equipped = serializer.deserialize("<gold>Нажмите ЛКМ чтобы одеть");
    private static Component unequipped = serializer.deserialize("<red>Нажмите ЛКМ чтобы снять");

    @Override
    public ItemStack getFullDisplay() {
        ItemStack display = new ItemStack(Material.FILLED_MAP);
        ItemMeta meta = display.getItemMeta();
        meta.displayName(serializer.deserialize("<gold>" + (this.name != null ? this.name : "Аксессуар")));
        List<Component> lore = new ArrayList<>();
        lore.add(used ? equipped : unequipped);
        meta.lore(lore);
        display.setItemMeta(meta);
        return display;
    }

    @Override
    public ItemStack getDisplay() {
        return new ItemStack(Material.COPPER_INGOT);
    }

    public class Meta {
        private int at;
        private String uri;
        private Map<String, String> colors;

        public AttachType getAt(final int i) {
            return AttachType.values()[i];
        }

        public String getUri() {
            return uri;
        }

        public Map<String, String> getColors() {
            return colors;
        }
    }
}
