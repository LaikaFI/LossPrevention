package link.alpinia.LossPrevention.database;

import link.alpinia.LossPrevention.LossPrevention;
import link.alpinia.LossPrevention.model.Archive;
import link.alpinia.LossPrevention.model.ArchiveMode;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LPDatabase {
    private Connection connection;

    private final String INITIALIZE_ARCHIVES = "CREATE TABLE IF NOT EXISTS `archives` (" +
            "`channel` VARCHAR(50) NOT NULL COMMENT 'The Discord ID of a Channel'," +
            "`uuid` VARCHAR(50) NOT NULL COMMENT 'Internal UUID to reference'," +
            "`lastArchiveTimestamp` BIGINT NOT NULL DEFAULT 0 COMMENT 'The last time the channel was archived'," +
            "`archiveTime` BIGINT NOT NULL DEFAULT 0 COMMENT 'How often the channel should be archived'," +
            "`type` VARCHAR(50) NOT NULL COMMENT 'Archive Type'," +
            "PRIMARY KEY (`uuid`)" +
            ");";

    //channel 1,6 uuid 2, 7 lastArchiveTimestamp 3 8 archiveTime 4 9 type = 5 10
    private final String SAVE_ARCHIVE = "INSERT INTO `archives`(channel, uuid, lastArchiveTimestamp, archiveTime, type) VALUES (?,?,?,?,?) " +
            "ON DUPLICATE KEY UPDATE channel=?, uuid=?, lastArchiveTimestamp=?, archiveTime=?, type=?;";

    private final String LOAD_ARCHIVE = "SELECT * FROM `archives`;";

    private final String DELETE_ARCHIVE = "DELETE FROM `archives` WHERE `uuid`=?";

    public LPDatabase(String username, String password, String host, int port, String database) {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password);
        } catch (Exception ex) {
            ex.printStackTrace();
            LossPrevention.getLogger().warn("Failed to initialize SQL, check stack.");
        }

        initializeTables();
    }

    private void initializeTables() {
        try {
            PreparedStatement ps = connection.prepareStatement(INITIALIZE_ARCHIVES);
            ps.execute();
        } catch (Exception ex) {
            ex.printStackTrace();
            LossPrevention.getLogger().warn("Failed to initialize tables. Check stack.");
        }
    }

    public void saveArchives() {
        try {
            PreparedStatement ps = connection.prepareStatement(SAVE_ARCHIVE);
            int i = 0;
            for(var a : LossPrevention.instance.archiveManager.getChannelsForArchival()) {
                //channel 1,6 uuid 2, 7 lastArchiveTimestamp 3 8 archiveTime 4 9 type = 5 10
                ps.setString(1, a.getChannel().getId());
                ps.setString(6, a.getChannel().getId());
                ps.setString(2, a.getUuid().toString());
                ps.setString(7, a.getUuid().toString());
                ps.setLong(3, a.getLastArchiveTimestamp());
                ps.setLong(8, a.getLastArchiveTimestamp());
                ps.setLong(4, a.getArchiveTime());
                ps.setLong(9, a.getArchiveTime());
                ps.setString(5, a.getType().name());
                ps.setString(10, a.getType().name());
                ps.addBatch();
                i++;
            }
            LossPrevention.getLogger().info("Archive Batches: " + i);
            ps.executeBatch();
            LossPrevention.getLogger().info("Batches Cleared.");
        } catch (Exception ex) {
            ex.printStackTrace();
            LossPrevention.getLogger().warn("Failed to save archives!");
        }
    }

    public void loadArchives() {
        List<Archive> archive = new ArrayList<>();
        List<String> forDeletion = new ArrayList<>(); //Channel ID
        try {
            PreparedStatement ps = connection.prepareStatement(LOAD_ARCHIVE);
            ResultSet rs = ps.executeQuery();
            int i = 0;
            while(rs.next()) {
                var txtChannel = LossPrevention.instance.tryFetchTextChannel(rs.getString(1));
                if(txtChannel == null) {
                    LossPrevention.getLogger().info("TextChannel for Archive [" + rs.getString(1) + "] is invalid, scheduling for deletion.");
                    forDeletion.add(rs.getString(1));
                } else {
                    var arc = new Archive(
                            txtChannel,
                            UUID.fromString(rs.getString(2)),
                            rs.getLong(3),
                            rs.getLong(4),
                            ArchiveMode.valueOf(rs.getString(5))
                    );
                    archive.add(arc);
                    i++;
                }
            }
            LossPrevention.getLogger().info("Loaded " + i + " archives. " + forDeletion.size() + " scheduled for deletion.");
            //Load the manager that way we don't spend any more synchronous time slowing down the main handlers.
            LossPrevention.instance.archiveManager.loadArchives(archive);
            PreparedStatement delSt = connection.prepareStatement(DELETE_ARCHIVE);
            for(String s : forDeletion) {
                delSt.setString(1, s);
                delSt.addBatch();
            }
            delSt.executeBatch();
            LossPrevention.getLogger().info("Archives scheduled for deletion cleared.");
        } catch (Exception ex) {
            ex.printStackTrace();
            LossPrevention.getLogger().warn("Failed to load archives from DB, check stack.");
        }
    }

    public void deleteArchive(Archive a) {
        try {
            PreparedStatement ps = connection.prepareStatement(DELETE_ARCHIVE);
            ps.setString(1, a.getUuid().toString());
            ps.execute();
        } catch (Exception ex) {
            ex.printStackTrace();
            LossPrevention.getLogger().warn("Failed to delete an archive, check stack.");
        }
    }
}
