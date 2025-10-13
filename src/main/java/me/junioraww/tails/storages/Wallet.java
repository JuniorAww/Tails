package me.junioraww.tails.storages;

import me.junioraww.tails.data.types.wallet.Item;

import java.util.List;

public class Wallet {
    private String name;
    private long balance;
    private List<Item> items;

    // TODO перенести punishments

    public Wallet(String name) {
        this.name = name;
    }

    public long getBalance() {
        return balance;
    }

    public List<Item> getItems() {
        return items;
    }
}
