package me.junioraww.tails.data.types.wallet;

public class Item {
    /*
    Предмет имеет id, type (отрицателен если экипировано)
    Опционально: имя и мета
     */
    private final int id;
    private final Type type;
    private boolean equipped;
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
        return equipped;
    }

    public void equip() {
        equipped = true;
    }

    public void unequip() {
        equipped = false;
    }

    public int getId() { return id; }
    public Type getType() { return type; }
    public String getName() { return name; }
    public int[] getMeta() { return meta; }

    public enum Type {
        GRAFFITI(1),
        TAIL(2),
        EARS(3),
        PET(4),
        WALK_EFFECT(5),
        KILL_EFFECT(6);

        private final int type;
        Type(int type) {
            this.type = type;
        }

        public int getType() {
            return type;
        }
    }
}
