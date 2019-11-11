package ru.endlesscode.rpginventory.database;

import org.bukkit.entity.Player;
import ru.endlesscode.rpginventory.RPGInventory;
import ru.endlesscode.rpginventory.database.mysql.MysqlHandler;
import ru.endlesscode.rpginventory.database.mysql.MysqlPool;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.inventory.PlayerWrapper;
import ru.endlesscode.rpginventory.inventory.backpack.Backpack;
import ru.endlesscode.rpginventory.utils.DateUtil;
import ru.endlesscode.rpginventory.utils.JsonUtil;

import java.sql.ResultSet;
import java.util.UUID;

public class SQLManager {
    private RPGInventory plugin;
    private MysqlPool pool;

    public SQLManager(RPGInventory plugin) {
        this.plugin = plugin;
        this.pool = new MysqlPool(plugin);
        this.pool.init();
    }

    public void init() {
        String inventory = "CREATE TABLE IF NOT EXISTS rpg_inventory (uuid VARCHAR(50) NOT NULL, player VARCHAR(20) NOT NULL, contents TEXT NOT NULL, PRIMARY KEY (uuid)) ENGINE = InnoDB";
        String backpack = "CREATE TABLE IF NOT EXISTS rpg_backpack (uuid VARCHAR(50) NOT NULL, date DATETIME NOT NULL, contents TEXT NOT NULL, PRIMARY KEY (uuid)) ENGINE = InnoDB";
        MysqlHandler sql = pool.getHandler();
        sql.openConnection();
        sql.updateSQL(inventory);
        sql.updateSQL(backpack);
        sql.closeConnection();
        pool.recover(sql);
    }

    public void saveInventory(Player p) {

        PlayerWrapper playerWrapper = InventoryManager.get(p);

        String contents = JsonUtil.getInventoryJson(playerWrapper);

        MysqlHandler sql = pool.getHandler();

        boolean exists = existsPlayer(p);

        try {
            sql.openConnection();
            String update = "UPDATE rpg_inventory set contents = '%contents%' WHERE uuid = '%uuid%'";
            String insert = "INSERT INTO rpg_inventory (uuid, player, contents) VALUES ('%uuid%', '%player%', '%contents%')";

            if (exists) {
                sql.updateSQL(update
                        .replace("%contents%", contents)
                        .replace("%uuid%", p.getUniqueId().toString())
                );
            }
            else {
                sql.updateSQL(insert
                        .replace("%uuid%", p.getUniqueId().toString())
                        .replace("%player%", p.getName())
                        .replace("%contents%", contents)
                );
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            sql.closeConnection();
            pool.recover(sql);
        }
    }

    public void loadInventory(Player p) {
        boolean exists = existsPlayer(p);

        if (!exists) return;

        String select = "SELECT * FROM rpg_inventory WHERE uuid = '%uuid%'";
        MysqlHandler sql = pool.getHandler();
        try {
            sql.openConnection();
            ResultSet set = sql.querySQL(select.replace("%uuid%", p.getUniqueId().toString()));
            if (!set.next()) return;
            String contents = set.getString("contents");
            JsonUtil.setPlayerInventory(p, contents);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            sql.closeConnection();
            pool.recover(sql);
        }
    }

    public void saveBackpack(Backpack backpack) {

        if (backpack == null) return;

        String contents = JsonUtil.getBackpackJson(backpack);

        MysqlHandler sql = pool.getHandler();

        boolean exists = existsBackPack(backpack.getUniqueId());

        try {
            sql.openConnection();
            String update = "UPDATE rpg_backpack set contents = '%contents%', date = '%date%' WHERE uuid = '%uuid%'";
            String insert = "INSERT INTO rpg_backpack (uuid, date, contents) VALUES ('%uuid%', '%date%', '%contents%')";

            if (exists) {
                sql.updateSQL(update
                        .replace("%contents%", contents)
                        .replace("%date%", DateUtil.getDate())
                        .replace("%uuid%", backpack.getUniqueId().toString())
                );
            }
            else {
                sql.updateSQL(insert
                        .replace("%uuid%", backpack.getUniqueId().toString())
                        .replace("%date%", DateUtil.getDate())
                        .replace("%contents%", contents)
                );
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            sql.closeConnection();
            pool.recover(sql);
        }
    }

    public void loadBackPack(UUID uuid) {
        String select = "SELECT * FROM rpg_backpack WHERE uuid = '%uuid%'";

        MysqlHandler sql = pool.getHandler();
        try {
            sql.openConnection();
            ResultSet set = sql.querySQL(select.replace("%uuid%", uuid.toString()));
            if (!set.next()) return;
            String contents = set.getString("contents");
            JsonUtil.addBackPack(uuid, contents);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            sql.closeConnection();
            pool.recover(sql);
        }
    }

    private boolean existsPlayer(Player p) {
        String select = "SELECT * FROM rpg_inventory WHERE uuid = '%uuid%'";

        MysqlHandler sql = pool.getHandler();
        try {
            sql.openConnection();
            ResultSet set = sql.querySQL(select.replace("%uuid%", p.getUniqueId().toString()));
            return set.next();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            sql.closeConnection();
            pool.recover(sql);
        }
        return false;
    }

    public boolean existsBackPack(UUID uuid) {
        String select = "SELECT * FROM rpg_backpack WHERE uuid = '%uuid%'";

        MysqlHandler sql = pool.getHandler();
        try {
            sql.openConnection();
            ResultSet set = sql.querySQL(select.replace("%uuid%", uuid.toString()));
            return set.next();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            sql.closeConnection();
            pool.recover(sql);
        }
        return false;
    }
}
