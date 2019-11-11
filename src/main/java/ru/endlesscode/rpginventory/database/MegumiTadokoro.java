package ru.endlesscode.rpginventory.database;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ru.endlesscode.rpginventory.RPGInventory;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.inventory.PlayerWrapper;
import ru.endlesscode.rpginventory.inventory.backpack.Backpack;
import ru.endlesscode.rpginventory.inventory.backpack.BackpackManager;
import ru.endlesscode.rpginventory.inventory.slot.Slot;
import ru.endlesscode.rpginventory.inventory.slot.SlotManager;
import ru.endlesscode.rpginventory.misc.config.Config;
import ru.endlesscode.rpginventory.utils.ItemUtils;

public class MegumiTadokoro {
    private static RPGInventory plugin = RPGInventory.getInstance();

    public static void load(Player player) {
        if (!plugin.isMysql()) return;

        InventoryLoadTask task = new InventoryLoadTask(plugin, player);
        task.runTaskLater(plugin, Config.getConfig().getInt("Delay"));
    }

    public static void save(Player player) {
        if (!plugin.isMysql()) return;

        plugin.getSqlManager().saveInventory(player);

        PlayerWrapper playerWrapper = InventoryManager.get(player);
        Inventory items = playerWrapper.getInventory();
        Slot slot = SlotManager.instance().getBackpackSlot();
        if (slot == null) return;
        for (Integer id : SlotManager.instance().getBackpackSlot().getSlotIds()) {
            ItemStack item = items.getItem(id);
            if (ItemUtils.isEmpty(item) || slot.isCup(item)) continue;
            Backpack backpack = BackpackManager.getItemBP(item);
            plugin.getSqlManager().saveBackpack(backpack);
        }
    }


}
