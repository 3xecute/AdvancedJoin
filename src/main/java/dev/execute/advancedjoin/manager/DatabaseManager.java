package dev.execute.advancedjoin.manager;

import dev.execute.advancedjoin.AdvancedJoin;
import dev.execute.advancedjoin.model.PlayerData;
import org.bukkit.Bukkit;

import java.io.File;
import java.sql.*;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;

public class DatabaseManager {

    private final AdvancedJoin plugin;
    private Connection connection;
    private boolean isMySQL;

    private final Map<UUID, PlayerData> cache = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "AdvancedJoin-DB");
        t.setDaemon(true);
        return t;
    });

    public DatabaseManager(AdvancedJoin plugin) {
        this.plugin = plugin;
    }

    public void init() {
        String type = plugin.getConfig().getString("database.type", "sqlite");
        isMySQL = type.equalsIgnoreCase("mysql");

        try {
            if (isMySQL) {
                initMySQL();
            } else {
                initSQLite();
            }
            createTable();
            plugin.getLogger().info("Database connected (" + type.toUpperCase() + ").");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to connect to database!", e);
        }
    }

    private void initSQLite() throws SQLException {
        File dbFile = new File(plugin.getDataFolder(), "advancedjoin.db");
        connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath() + "?journal_mode=WAL");
    }

    private void initMySQL() throws SQLException {
        String host     = plugin.getConfig().getString("database.host", "localhost");
        int    port     = plugin.getConfig().getInt("database.port", 3306);
        String db       = plugin.getConfig().getString("database.name", "advancedjoin");
        String user     = plugin.getConfig().getString("database.username", "root");
        String password = plugin.getConfig().getString("database.password", "");
        String url = "jdbc:mysql://" + host + ":" + port + "/" + db
                + "?useSSL=false&autoReconnect=true&characterEncoding=UTF-8";
        connection = DriverManager.getConnection(url, user, password);
    }

    private void createTable() throws SQLException {
        String upsertKey = isMySQL
                ? "ON DUPLICATE KEY UPDATE"
                : "ON CONFLICT(uuid) DO UPDATE SET";

        String sql = """
                CREATE TABLE IF NOT EXISTS player_data (
                    uuid TEXT PRIMARY KEY,
                    message_id TEXT NOT NULL DEFAULT 'default',
                    sound_id TEXT NOT NULL DEFAULT 'levelup',
                    particle_id TEXT NOT NULL DEFAULT 'heart',
                    animation_id TEXT NOT NULL DEFAULT 'NONE',
                    quit_message_id TEXT NOT NULL DEFAULT 'default',
                    message_enabled INTEGER NOT NULL DEFAULT 1,
                    sound_enabled INTEGER NOT NULL DEFAULT 1,
                    particle_enabled INTEGER NOT NULL DEFAULT 1,
                    animation_enabled INTEGER NOT NULL DEFAULT 0,
                    quit_message_enabled INTEGER NOT NULL DEFAULT 1,
                    streak INTEGER NOT NULL DEFAULT 0,
                    last_join_date TEXT NOT NULL DEFAULT '',
                    first_join INTEGER NOT NULL DEFAULT 1
                );
                """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            if (!isMySQL) {
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_uuid ON player_data(uuid);");
            }
        }
        // Add missing columns for existing installs (migration)
        migrateColumns();
    }

    private void migrateColumns() {
        String[][] columns = {
                {"quit_message_id",      "TEXT NOT NULL DEFAULT 'default'"},
                {"quit_message_enabled", "INTEGER NOT NULL DEFAULT 1"},
                {"streak",               "INTEGER NOT NULL DEFAULT 0"},
                {"last_join_date",       "TEXT NOT NULL DEFAULT ''"},
                {"first_join",           "INTEGER NOT NULL DEFAULT 1"},
        };
        for (String[] col : columns) {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("ALTER TABLE player_data ADD COLUMN " + col[0] + " " + col[1]);
            } catch (SQLException ignored) {
                // Column already exists — safe to ignore
            }
        }
    }

    public void close() {
        executor.shutdown();
        try { executor.awaitTermination(5, TimeUnit.SECONDS); } catch (InterruptedException ignored) {}
        try { if (connection != null && !connection.isClosed()) connection.close(); }
        catch (SQLException e) { plugin.getLogger().log(Level.WARNING, "Error closing DB.", e); }
    }

    // ─── Load ─────────────────────────────────────────────────────────

    public void loadPlayer(UUID uuid, Consumer<PlayerData> callback) {
        if (cache.containsKey(uuid)) {
            Bukkit.getScheduler().runTask(plugin, () -> callback.accept(cache.get(uuid)));
            return;
        }
        executor.submit(() -> {
            PlayerData data = loadSync(uuid);
            cache.put(uuid, data);
            Bukkit.getScheduler().runTask(plugin, () -> callback.accept(data));
        });
    }

    private PlayerData loadSync(UUID uuid) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM player_data WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new PlayerData(
                        uuid,
                        rs.getString("message_id"),
                        rs.getString("sound_id"),
                        rs.getString("particle_id"),
                        rs.getString("animation_id"),
                        rs.getString("quit_message_id"),
                        rs.getInt("message_enabled")      == 1,
                        rs.getInt("sound_enabled")        == 1,
                        rs.getInt("particle_enabled")     == 1,
                        rs.getInt("animation_enabled")    == 1,
                        rs.getInt("quit_message_enabled") == 1,
                        rs.getInt("streak"),
                        rs.getString("last_join_date"),
                        rs.getInt("first_join") == 1
                );
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load data for " + uuid, e);
        }
        return new PlayerData(uuid);
    }

    // ─── Save ─────────────────────────────────────────────────────────

    public void savePlayer(PlayerData data) {
        executor.submit(() -> saveSync(data));
    }

    private void saveSync(PlayerData data) {
        String sql = isMySQL
                ? """
                  INSERT INTO player_data (uuid, message_id, sound_id, particle_id, animation_id,
                      quit_message_id, message_enabled, sound_enabled, particle_enabled,
                      animation_enabled, quit_message_enabled, streak, last_join_date, first_join)
                  VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                  ON DUPLICATE KEY UPDATE
                      message_id=VALUES(message_id), sound_id=VALUES(sound_id),
                      particle_id=VALUES(particle_id), animation_id=VALUES(animation_id),
                      quit_message_id=VALUES(quit_message_id),
                      message_enabled=VALUES(message_enabled), sound_enabled=VALUES(sound_enabled),
                      particle_enabled=VALUES(particle_enabled), animation_enabled=VALUES(animation_enabled),
                      quit_message_enabled=VALUES(quit_message_enabled),
                      streak=VALUES(streak), last_join_date=VALUES(last_join_date),
                      first_join=VALUES(first_join)
                  """
                : """
                  INSERT INTO player_data (uuid, message_id, sound_id, particle_id, animation_id,
                      quit_message_id, message_enabled, sound_enabled, particle_enabled,
                      animation_enabled, quit_message_enabled, streak, last_join_date, first_join)
                  VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                  ON CONFLICT(uuid) DO UPDATE SET
                      message_id=excluded.message_id, sound_id=excluded.sound_id,
                      particle_id=excluded.particle_id, animation_id=excluded.animation_id,
                      quit_message_id=excluded.quit_message_id,
                      message_enabled=excluded.message_enabled, sound_enabled=excluded.sound_enabled,
                      particle_enabled=excluded.particle_enabled, animation_enabled=excluded.animation_enabled,
                      quit_message_enabled=excluded.quit_message_enabled,
                      streak=excluded.streak, last_join_date=excluded.last_join_date,
                      first_join=excluded.first_join
                  """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, data.getUuid().toString());
            ps.setString(2, data.getMessageId());
            ps.setString(3, data.getSoundId());
            ps.setString(4, data.getParticleId());
            ps.setString(5, data.getAnimationId());
            ps.setString(6, data.getQuitMessageId());
            ps.setInt(7,  data.isMessageEnabled()      ? 1 : 0);
            ps.setInt(8,  data.isSoundEnabled()        ? 1 : 0);
            ps.setInt(9,  data.isParticleEnabled()     ? 1 : 0);
            ps.setInt(10, data.isAnimationEnabled()    ? 1 : 0);
            ps.setInt(11, data.isQuitMessageEnabled()  ? 1 : 0);
            ps.setInt(12, data.getStreak());
            ps.setString(13, data.getLastJoinDate());
            ps.setInt(14, data.isFirstJoin() ? 1 : 0);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save data for " + data.getUuid(), e);
        }
    }

    // ─── Cache ────────────────────────────────────────────────────────

    public PlayerData getCached(UUID uuid) { return cache.get(uuid); }

    public void removeFromCache(UUID uuid) { cache.remove(uuid); }

    public void saveAll() {
        plugin.getLogger().info("Saving " + cache.size() + " player records...");
        cache.values().forEach(this::saveSync);
        cache.clear();
    }
}
