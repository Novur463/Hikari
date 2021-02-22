package com.novur.hdt;

import com.novur.hdt.database.ConnectionPool;
import com.novur.hdt.database.Database;
import com.novur.hdt.events.TestListeners;
import org.bukkit.plugin.java.JavaPlugin;

public class DataTest extends JavaPlugin {
    private ConnectionPool connectionPool;
    private Database database;

    @Override
    public void onEnable() {
        connectionPool = new ConnectionPool(this);
        connectionPool.connectToDatabase();

        if(connectionPool.isConnected()) {
            database = new Database(this);
            database.createTable();

            getLogger().warning("Database successfully paired!");

            getServer().getPluginManager().registerEvents(new TestListeners(this), this);
        }

        else {
            getLogger().severe("Database unsuccessfully paired, aborting...");
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        if(connectionPool.isConnected()) connectionPool.disconnectDatabase();
    }

    public ConnectionPool getConnectionPool() {
        return connectionPool;
    }

    public Database getDatabase() {
        return database;
    }
}
