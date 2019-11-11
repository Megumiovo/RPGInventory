package ru.endlesscode.rpginventory.database;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import ru.endlesscode.rpginventory.RPGInventory;
import ru.endlesscode.rpginventory.inventory.InventoryLocker;

public class InventoryLoadTask extends BukkitRunnable {
    private RPGInventory plugin;
    private Player player;

    public InventoryLoadTask(RPGInventory plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    @Override
    public void run() {
        plugin.getSqlManager().loadInventory(player);

        InventoryLocker.lockSlots(player);
        plugin.lock.remove(player);
    }
}
