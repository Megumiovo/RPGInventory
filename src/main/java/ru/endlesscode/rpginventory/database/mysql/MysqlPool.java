package ru.endlesscode.rpginventory.database.mysql;

import ru.endlesscode.rpginventory.RPGInventory;
import ru.endlesscode.rpginventory.misc.config.Config;

import java.util.LinkedList;

public class MysqlPool {
    private RPGInventory plugin;
    private String host;
    private String port;
    private String database;
    private String users;
    private String password;
    private LinkedList<MysqlHandler> pools;
    private int min;
    private int max;

    public MysqlPool(RPGInventory plugin) {
        this.plugin = plugin;
        this.host = Config.getConfig().getString("MYSQL.Host");
        this.port = Config.getConfig().getString("MYSQL.Port");
        this.database = Config.getConfig().getString("MYSQL.Database");
        this.users = Config.getConfig().getString("MYSQL.Users");
        this.password = Config.getConfig().getString("MYSQL.Password");
        this.pools = new LinkedList<>();
        this.min = 1;
        this.max = 5;
    }

    public void init() {
        pools.clear();
        for (int i = min; i <= max; i++) {
            pools.add(new MysqlHandler(host, port, database, users, password));
        }
    }

    public MysqlHandler getHandler() {
        if (pools.size() == 0) return new MysqlHandler(host, port, database, users, password);
        else return pools.remove(0);
    }

    public void recover(MysqlHandler handler) {
        if (pools.size() >= max) return;
        pools.add(handler);
    }
}