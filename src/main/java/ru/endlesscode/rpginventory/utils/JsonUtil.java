package ru.endlesscode.rpginventory.utils;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import ru.endlesscode.rpginventory.RPGInventory;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.inventory.PlayerWrapper;
import ru.endlesscode.rpginventory.inventory.backpack.Backpack;
import ru.endlesscode.rpginventory.inventory.backpack.BackpackManager;
import ru.endlesscode.rpginventory.inventory.backpack.BackpackType;
import ru.endlesscode.rpginventory.inventory.slot.Slot;
import ru.endlesscode.rpginventory.inventory.slot.SlotManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JsonUtil {
    private static RPGInventory plugin = RPGInventory.getInstance();
    private static ItemConvertUtil convertUtil = new ItemConvertUtil(plugin.getVersion());

    public static String getInventoryJson(PlayerWrapper playerWrapper) {

        JSONObject slotsList = new JSONObject();
        for (Slot slot : SlotManager.instance().getSlots()) {

            JSONObject slotContent = new JSONObject();
            JSONArray itemList = new JSONArray();

            List<Integer> slotIds = slot.getSlotIds();
            Inventory inventory = playerWrapper.getInventory();

            for (Integer slotId : slotIds) {

                ItemStack itemStack = inventory.getItem(slotId);
                if (!ItemUtils.isEmpty(itemStack) && !slot.isCup(itemStack)) {
                    itemList.add(convertUtil.convert(itemStack));
                }
                else {
                    itemList.add(null);
                }
            }

            if (itemList.size() > 0 || playerWrapper.isBuyedSlot(slot.getName())) {


                slotContent.put("type", slot.getSlotType().name());
                if (playerWrapper.isBuyedSlot(slot.getName())) {
                    slotContent.put("buyed", "true");
                }
                slotContent.put("items", itemList);
            }

            slotsList.put(slot.getName(), slotContent);
        }

        int slots = playerWrapper.getBuyedGenericSlots();

        JSONObject playerData = new JSONObject();
        playerData.put("slots", slotsList);
        playerData.put("buyed-slots", slots);

        return playerData.toJSONString();
    }

    public static void setPlayerInventory(Player p, String contents) {
        PlayerWrapper playerWrapper = InventoryManager.get(p);

        JSONObject playerData = (JSONObject) JSONValue.parse(contents);

        int buySlot = Integer.parseInt(playerData.get("buyed-slots").toString());
        playerWrapper.setBuyedSlots(buySlot);

        JSONObject slots = (JSONObject) playerData.get("slots");

        for (Slot slot : SlotManager.instance().getSlots()) {

            String slotName = slot.getName();
            if (slots.get(slotName) == null) continue;
            JSONObject slotData = (JSONObject) slots.get(slotName);

            String type = slotData.get("type").toString();
            if (slot.getSlotType() != Slot.SlotType.valueOf(type)) continue;

            if (slotData.containsKey("buyed")) {
                playerWrapper.setBuyedSlots(slot.getName());
            }

            Object items = slotData.get("items");
            if (items == null) continue;
            JSONArray itemList = (JSONArray) items;

            for (int i = 0; i < slot.getSlotIds().size(); i++) {
                playerWrapper.getInventory().setItem(slot.getSlotIds().get(i), slot.getCup());
                if (itemList.size() > i) {
                    Object base64Item = itemList.get(i);
                    if (base64Item == null) continue;
                    playerWrapper.getInventory().setItem(slot.getSlotIds().get(i), convertUtil.convert(itemList.get(i).toString()));
                }
            }
        }
    }

    public static String getBackpackJson(Backpack backpack) {
        String type = backpack.getType().getId();
        ItemStack[] bpItems = backpack.getContents();

        List<ItemStack> items = new ArrayList<>();
        for (ItemStack item : bpItems) {
            if (ItemUtils.isEmpty(item) || item.equals(InventoryManager.getFillSlot())) continue;
            items.add(item);
        }

        JSONArray itemList = new JSONArray();
        if (items.size() != 0) {
            for (ItemStack item : bpItems) {
                itemList.add(ItemUtils.isEmpty(item) ? null : convertUtil.convert(item));
            }
        }

        JSONObject bpData = new JSONObject();
        bpData.put("items", itemList);
        bpData.put("type", type);
        bpData.put("last-use", backpack.getLastUse());

        return bpData.toJSONString();
    }

    public static void addBackPack(UUID uuid, String contents) {
        JSONObject bpData = (JSONObject) JSONValue.parse(contents);

        long last_use = Long.parseLong(bpData.get("last-use").toString());
        String type = bpData.get("type").toString();

        List<ItemStack> itemList = new ArrayList<>();
        Object items = bpData.get("items");
        if (items != null) {
            JSONArray array = (JSONArray) items;
            for (Object obj : array) {
                if (obj == null) continue;
                itemList.add(convertUtil.convert(obj.toString()));
            }
        }

        BackpackType bpType = BackpackManager.getBackpackType(type);
        if (bpType == null) return;

        Backpack backpack = bpType.createBackpack(uuid);
        ItemStack[] itemStacks = itemList.toArray(new ItemStack[bpType.getSize()]);
        backpack.setContents(itemStacks);
        backpack.setLastUse(last_use);

        BackpackManager.getBACKPACKS().put(uuid, backpack);
    }
}
