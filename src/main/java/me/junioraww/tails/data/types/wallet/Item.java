package me.junioraww.tails.data.types.wallet;

import com.google.gson.annotations.JsonAdapter;
import me.junioraww.tails.utils.ItemAdapter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.inventory.ItemStack;

@JsonAdapter(ItemAdapter.class)
abstract public class Item {
    static MiniMessage serializer = MiniMessage.miniMessage();
    /*
    enum type можно убрать и сделать сериалайзер gson для типов с instanceof
     */
    protected final int id;
    protected final Type type;
    protected boolean used;
    protected String name;

    public Item(int id, Type type) {
        this.id = id;
        this.type = type;
    }

    public Item(int id, Type type, String name) {
        this.id = id;
        this.type = type;
        this.name = name;
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

    abstract public ItemStack getFullDisplay();
    abstract public ItemStack getDisplay();

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
        ATTACH(1),
        PET(2),
        WALK_EFFECT(3),
        KILL_EFFECT(4);

        private final int type;
        Type(int type) {
            this.type = type;
        }

        public int asNum() {
            return type;
        }
    }
}
