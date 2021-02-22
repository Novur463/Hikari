package com.novur.hdt.database;

import com.novur.hdt.DataTest;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Database {
    private DataTest dataTest;
    private ConnectionPool connectionPool;

    public Database(DataTest dataTest) {
        this.dataTest = dataTest;
        this.connectionPool = dataTest.getConnectionPool();
    }

    public void createTable() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Connection connection = null;
                PreparedStatement preparedStatement = null;

                try {
                    connection = Database.this.connectionPool.getHikari().getConnection();

                    preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS data (UUID VARCHAR(36),POINTS INT(10) DEFAULT 0,PRIMARY KEY(UUID))");
                    preparedStatement.executeUpdate();
                }

                catch (SQLException e) {
                    e.printStackTrace();
                }

                finally {
                    connectionPool.close(connection, preparedStatement, null);
                }
            }
        }.runTaskAsynchronously(dataTest);
    }

    public boolean playerExists(Player player) throws ExecutionException, InterruptedException {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        new BukkitRunnable() {
            @Override
            public void run() {
                Connection connection = null;
                PreparedStatement preparedStatement = null;
                ResultSet resultSet = null;

                try {
                    connection = connectionPool.getHikari().getConnection();
                    preparedStatement = connection.prepareStatement("SELECT * FROM data WHERE UUID=?");
                    preparedStatement.setString(1,player.getUniqueId().toString());
                    resultSet = preparedStatement.executeQuery();

                    boolean playerExists = false;
                    if(resultSet.next()) playerExists = true;

                    result.complete(playerExists);
                }

                catch(SQLException e) {
                    e.printStackTrace();
                }

                finally {
                    connectionPool.close(connection, preparedStatement, resultSet);
                }
            }
        }.runTaskAsynchronously(dataTest);

        return result.get();
    }

    public void createPlayer(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Connection connection = null;
                PreparedStatement preparedStatement = null;

                try {
                    if(!playerExists(player)) {
                        connection = connectionPool.getHikari().getConnection();
                        preparedStatement = connection.prepareStatement("INSERT IGNORE INTO data (UUID) VALUES (?)");
                        preparedStatement.setString(1, player.getUniqueId().toString());
                        preparedStatement.executeUpdate();
                    }
                }

                catch(SQLException | InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }

                finally {
                    connectionPool.close(connection, preparedStatement, null);
                }
            }
        }.runTaskAsynchronously(dataTest);
    }

    public int getPoints(Player player) throws ExecutionException, InterruptedException {
        CompletableFuture<Integer> result = CompletableFuture.supplyAsync(() -> {
           Connection connection = null;
           PreparedStatement preparedStatement = null;
           ResultSet resultSet = null;

           try {
               connection = connectionPool.getHikari().getConnection();
               preparedStatement = connection.prepareStatement("SELECT POINTS FROM data WHERE UUID=?");
               preparedStatement.setString(1,player.getUniqueId().toString());
               resultSet = preparedStatement.executeQuery();

               if(resultSet.next()) return resultSet.getInt("POINTS");
           }

           catch(SQLException e) {
               e.printStackTrace();
           }

           finally {
               connectionPool.close(connection, preparedStatement, resultSet);
           }

           return null;
        });

        return result.get();
    }

    public void addPoints(Player player, int points) {
        CompletableFuture.supplyAsync(() -> {
           Connection connection = null;
           PreparedStatement preparedStatement = null;

           try {
               connection = connectionPool.getHikari().getConnection();
               preparedStatement = connection.prepareStatement("UPDATE data SET POINTS=? WHERE UUID=?");
               preparedStatement.setInt(1,getPoints(player) + points);
               preparedStatement.setString(2,player.getUniqueId().toString());
               preparedStatement.executeUpdate();

           }

           catch(SQLException | ExecutionException | InterruptedException e) {
               e.printStackTrace();
           }

           finally {
               connectionPool.close(connection, preparedStatement, null);
           }

           return null;
        });
    }
}
