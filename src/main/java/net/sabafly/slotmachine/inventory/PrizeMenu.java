package net.sabafly.slotmachine.inventory;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.sabafly.slotmachine.SlotMachine;
import net.sabafly.slotmachine.configuration.Configurations;
import net.sabafly.slotmachine.game.MedalBank;
import net.sabafly.slotmachine.v2.game.slot.Key;
import net.sabafly.slotmachine.v2.game.slot.SlotRegistry;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.geysermc.floodgate.api.FloodgateApi;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class PrizeMenu extends ParaInventory {
    private int page;
    private long medal;
    public PrizeMenu(HumanEntity player, int page) {
        super(SlotMachine.getPlugin(),54, MiniMessage.miniMessage().deserialize(SlotMachine.getMessages().prizeMenu));
        this.page = page;
        Long t = MedalBank.removeMedalAll(player);
        if (t == null) t = 0L;
        this.medal = t;
        update();
    }

    private void update() {
        Inventory inventory = getInventory();

        final ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        final ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.displayName(MiniMessage.miniMessage().deserialize(SlotMachine.getMessages().insertPaperHere));
        glassMeta.getPersistentDataContainer().set(Key.ACTION, PersistentDataType.STRING, "ADD_MEDAL");
        glass.setItemMeta(glassMeta);
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, glass.clone());
            inventory.setItem(45 + i, glass.clone());
        }

        final int[] slot = {9};
        Configurations config = SlotMachine.getPluginConfig();
        final int maxPage = (int) Math.ceil(config.prize.customPrizes.size() / 45.0);
        if (page > maxPage) page = maxPage;
        if (page == 1) {
            addPrize(SlotRegistry.PrizeItem.getLargeItem(), slot[0], config.prize.largePrice);
            addPrize(SlotRegistry.PrizeItem.getMediumItem(), slot[0] + 1, config.prize.mediumPrice);
            addPrize(SlotRegistry.PrizeItem.getSmallItem(), slot[0] + 2, config.prize.smallPrice);
            slot[0] += 3;
        }

        if (!config.prize.customPrizes.isEmpty()) config.prize.customPrizes.stream().skip((page-1) * 45L).limit(45 - slot[0]).forEach(customPrize -> {
            final ItemStack item = new ItemStack(customPrize.item);
            final ItemMeta meta = item.getItemMeta();
            if (customPrize.name != null) meta.displayName(MiniMessage.miniMessage().deserialize(customPrize.name));
            if (customPrize.lore != null) meta.lore(customPrize.lore.lines().map(MiniMessage.miniMessage()::deserialize).toList());
            if (customPrize.count > 1) item.setAmount(customPrize.count);
            item.setItemMeta(meta);
            if (item.getType() == Material.PLAYER_HEAD) {
                final SkullMeta skullMeta = (SkullMeta) item.getItemMeta();
                final String name = customPrize.playerName;
                UUID uuid = Bukkit.getOfflinePlayer(name).getUniqueId();
                if (SlotMachine.isFloodgate()) {
                    final String prefix = FloodgateApi.getInstance().getPlayerPrefix();
                    if (name.startsWith(prefix)) {
                        try {
                            uuid = FloodgateApi.getInstance().getUuidFor(name.substring(prefix.length())).get();
                        } catch (InterruptedException | ExecutionException e) {
                            uuid = Bukkit.getOfflinePlayer(name).getUniqueId();
                        }
                    }
                }
                skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(uuid));
                item.setItemMeta(skullMeta);
            }
            addPrize(item, slot[0], customPrize.price);
            slot[0]++;
        });

    }

    private void addPrize(ItemStack item, int slot, int price) {
        final ItemMeta meta = item.getItemMeta();
        final ArrayList<Component> lore = new ArrayList<>();
        lore.add(MiniMessage.miniMessage().deserialize(SlotMachine.getMessages().prizePrice, TagResolver.builder().tag("price", Tag.inserting(Component.text(price))).build()));
        lore.add(MiniMessage.miniMessage().deserialize(SlotMachine.getMessages().medalCount, TagResolver.builder().tag("medal", Tag.inserting(Component.text(medal))).build()));
        lore.add(Component.empty());
        if (meta.hasLore()) lore.addAll(meta.lore());
        meta.lore(lore);

        meta.getPersistentDataContainer().set(Key.PRICE, PersistentDataType.INTEGER, price);
        meta.getPersistentDataContainer().set(Key.ACTION, PersistentDataType.STRING, "PURCHASE");

        item.setItemMeta(meta);
        final Inventory inventory = getInventory();
        inventory.setItem(slot, item);
    }

    public ItemStack buyItem(@NotNull final ItemStack item) {
        final ItemMeta meta = item.getItemMeta();
        final List<Component> oldLore = meta.lore();
        final ArrayList<Component> lore = new ArrayList<>();
        if (oldLore != null && oldLore.size() > 3) oldLore.stream().skip(3).forEach(lore::add);
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public boolean buy(@NotNull final ItemStack item) {
        if (!item.hasItemMeta()) return false;
        final ItemMeta meta = item.getItemMeta();
        if (!meta.getPersistentDataContainer().has(Key.ACTION, PersistentDataType.STRING)) return false;
        String action = meta.getPersistentDataContainer().get(Key.ACTION, PersistentDataType.STRING);
        if (action == null || !action.equals("PURCHASE")) return false;
        if (!meta.getPersistentDataContainer().has(Key.PRICE, PersistentDataType.INTEGER)) return false;
        Integer price = meta.getPersistentDataContainer().get(Key.PRICE, PersistentDataType.INTEGER);
        price = price == null ? 0 : price;
        if (price > medal) return false;
        setMedal(medal - price);
        return true;
    }

    public void addMedal(long medal) {
        setMedal(this.medal + medal);
    }

    public void setMedal(long medal) {
        this.medal = medal;
        update();
    }

    public void close(HumanEntity player) {
        MedalBank.addMedal(player.getUniqueId(), medal);
        player.sendMessage(MiniMessage.miniMessage().deserialize(SlotMachine.getMessages().medalCount, TagResolver.builder().tag("medal", Tag.inserting(Component.text(medal))).build()));
    }
}
