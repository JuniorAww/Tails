package me.junioraww.tails.data.types.wallet;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class Item {
    static MiniMessage serializer = MiniMessage.miniMessage();
    /*
    Предмет имеет id, type (отрицателен если экипировано)
    Опционально: имя и мета
     */
    private final int id;
    private final Type type;
    private boolean used;
    private String name;
    private int[] meta;
    /*
    Примеры
    type = 1: граффити (meta = ID граффити)
    type = 2: хвост (meta = офиц. или кастомный, ID хвоста, цвета)
     */

    public Item(int id, Type type) {
        this.id = id;
        this.type = type;
    }

    public Item(int id, Type type, String name, int[] meta) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.meta = meta;
    }

    public boolean isEquipped() {
        return used;
    }

    public void equip() {
        used = true;
    }

    public void unequip() {
        used = false;
    }

    public int getId() { return id; }
    public Type getType() { return type; }
    public String getName() { return name; }
    public int[] getMeta() { return meta; }

    /* Отображение в мире */
    public ItemStack getDisplay() {
        final int tid = type.getType() - 1;
        ItemStack display = new ItemStack(materials[tid]);
        ItemMeta meta = display.getItemMeta();
        meta.displayName(name != null ? serializer.deserialize(name) : names[tid]);
        display.setItemMeta(meta);
        return display;
    }

    /* Отображение командой /items */
    public ItemStack getFullDisplay() {
        final int tid = type.getType() - 1;
        ItemStack display = new ItemStack(materials[tid]);
        ItemMeta meta = display.getItemMeta();
        meta.displayName(name != null ? serializer.deserialize(name) : names[tid]);
        List<Component> lore = new ArrayList<>();
        lore.add(used ? unequip[tid] : equip[tid]);
        meta.lore(lore);
        display.setItemMeta(meta);
        return display;
    }

    private static Material[] materials = new Material[] {
            Material.MAP,
            Material.NETHER_STAR,
            Material.GOLDEN_HELMET,
            Material.WOLF_SPAWN_EGG,
            Material.IRON_BOOTS,
            Material.IRON_SWORD
    };

    private static Component[] names = new Component[] {
            serializer.deserialize("<gold>Граффити"),
            serializer.deserialize("<gold>Хвост"),
            serializer.deserialize("<gold>Уши"),
            serializer.deserialize("<gold>Питомец"),
            serializer.deserialize("<gold>Эффект ходьбы"),
            serializer.deserialize("<gold>Эффект добычи"),
    };

    // optimize same objects
    private static Component[] equip = new Component[] {
            serializer.deserialize("<gold>Нажмите ЛКМ для добавления в набор"),
            serializer.deserialize("<gold>Нажмите ЛКМ для того, чтобы надеть"),
            serializer.deserialize("<gold>Нажмите ЛКМ для экипировки"),
            serializer.deserialize("<gold>Нажмите ЛКМ для того, чтобы призвать"),
            serializer.deserialize("<gold>Нажмите ЛКМ для экипировки"),
            serializer.deserialize("<gold>Нажмите ЛКМ для экипировки"),
    };

    private static Component[] unequip = new Component[] {
            serializer.deserialize("<red>Нажмите ЛКМ для удаления с набора"),
            serializer.deserialize("<red>Нажмите ЛКМ для того, чтобы снять"),
            serializer.deserialize("<red>Нажмите ЛКМ для того, чтобы снять"),
            serializer.deserialize("<red>Нажмите ЛКМ для того, чтобы вернуть"),
            serializer.deserialize("<red>Нажмите ЛКМ для того, чтобы снять"),
            serializer.deserialize("<red>Нажмите ЛКМ для того, чтобы снять"),
    };

    public enum Type {
        GRAFFITI(0),
        TAIL(1),
        EARS(2),
        PET(3),
        WALK_EFFECT(4),
        KILL_EFFECT(5);

        private final int type;
        Type(int type) {
            this.type = type;
        }

        public int getType() {
            return type;
        }
    }
}
