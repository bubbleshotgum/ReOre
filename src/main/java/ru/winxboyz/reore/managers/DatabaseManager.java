package ru.winxboyz.reore.managers;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import ru.winxboyz.reore.ReOre;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class DatabaseManager {

    private ReOre plugin;
    private HikariDataSource dataSource;
    private String tableName;

    public DatabaseManager(ReOre plugin, String tableName) {
        this.plugin = plugin;
        this.tableName = tableName;
    }

    public void connect(String database) {
        HikariConfig config = new HikariConfig();
        
        File file = new File(plugin.getDataFolder(), database + ".db");

        config.setJdbcUrl("jdbc:sqlite:" + file.getAbsolutePath());
        
        config.setDriverClassName("org.sqlite.JDBC");
        config.setMaximumPoolSize(1);
        
        this.dataSource = new HikariDataSource(config);
    }

    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("База данных не инициализирована!");
        }
        return dataSource.getConnection();
    }

    public CompletableFuture<Void> createTableLocations() {
        CompletableFuture<Void> result = new CompletableFuture<>();

        String query = "CREATE TABLE IF NOT EXISTS " + tableName +  " ("
                    +  "loc_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    +  "world VARCHAR2(36),"
                    +  "x INTEGER,"
                    +  "y INTEGER,"
                    +  "z INTEGER"
                    +  ");";

        try(Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.execute();
        } catch(SQLException e) {
            plugin.getLogger().severe("Could not create locations table\n" + e.getErrorCode() + ": " + e.getMessage());
        }

        result.complete(null);
        return result;
    }

    public CompletableFuture<List<Location>> fetchLocations() {
        CompletableFuture<List<Location>> locations = new CompletableFuture<>();
        String query = "SELECT * FROM " + tableName + " ;";
        try(Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            List<Location> locs = new ArrayList<>(0);
            while(rs.next()) {
                locs.add(new Location(
                    Bukkit.getWorld(rs.getString("world")),
                    rs.getInt("x"), 
                    rs.getInt("y"),
                    rs.getInt("z"))
                );
            }
            locations.complete(locs);
        } catch(SQLException e) {
            plugin.getLogger().warning("could not fetch locations\n"+e.getErrorCode()+": "+e.getMessage());
            locations.complete(null);
        }

        return locations;
    }

    public CompletableFuture<Void> addLocation(Location loc) {
        CompletableFuture<Void> result = new CompletableFuture<>();
        String query = "INSERT INTO " + tableName + " (world,x,y,z) VALUES (?,?,?,?);";
        try(Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, loc.getWorld().getName());
            stmt.setInt(2, loc.blockX());
            stmt.setInt(3, loc.blockY());
            stmt.setInt(4, loc.blockZ());
            stmt.executeUpdate();
        } catch(SQLException e) {
            plugin.getLogger().warning("could not add a location\n"+e.getErrorCode()+": "+e.getMessage());
        }

        result.complete(null);
        return result;
    }

    public CompletableFuture<Void> addLocations(List<Location> locs) {
        CompletableFuture<Void> result = new CompletableFuture<>();
        String query = "INSERT INTO " + tableName + " (world,x,y,z) VALUES (?,?,?,?);";
        try(Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(query);
            for(Location loc : locs) {
                stmt.setString(1, loc.getWorld().getName());
                stmt.setInt(2, loc.blockX());
                stmt.setInt(3, loc.blockY());
                stmt.setInt(4, loc.blockZ());
                stmt.addBatch();
            }
            plugin.getLogger().info("Added " + stmt.executeBatch().length + " rows");
        } catch(SQLException e) {
            plugin.getLogger().warning("could not add a location\n"+e.getErrorCode()+": "+e.getMessage());
        }

        result.complete(null);
        return result;
    }

    public CompletableFuture<Void> removeLocation(Location loc) {
        CompletableFuture<Void> result = new CompletableFuture<>();
        String query = "DELETE FROM " + tableName + " WHERE world = ? AND x = ? AND y = ? AND z = ?;";
        try(Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, loc.getWorld().getName());
            stmt.setInt(2, loc.blockX());
            stmt.setInt(3, loc.blockY());
            stmt.setInt(4, loc.blockZ());
            stmt.executeUpdate();
        } catch(SQLException e) {
            plugin.getLogger().warning("could not remove a location\n"+e.getErrorCode()+": "+e.getMessage());
        }

        result.complete(null);
        return result;
    }

    public CompletableFuture<Void> removeLocations(List<Location> locs) {
        CompletableFuture<Void> result = new CompletableFuture<>();
        String query = "DELETE FROM " + tableName + " WHERE world = ? AND x = ? AND y = ? AND z = ?;";
        try(Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(query);
            for(Location loc : locs) {
                stmt.setString(1, loc.getWorld().getName());
                stmt.setInt(2, loc.blockX());
                stmt.setInt(3, loc.blockY());
                stmt.setInt(4, loc.blockZ());
                stmt.addBatch();
            }
            plugin.getLogger().info("Added " + stmt.executeBatch().length + " rows");
        } catch(SQLException e) {
            plugin.getLogger().warning("could not add a location\n"+e.getErrorCode()+": "+e.getMessage());
        }

        result.complete(null);
        return result;
    }


    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
