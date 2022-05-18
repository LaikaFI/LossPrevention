package link.alpinia.LossPrevention.model;

import net.dv8tion.jda.api.entities.TextChannel;

import java.util.UUID;

public class Archive {
    private ArchiveMode type;
    private TextChannel channel;
    private String channelName;
    private UUID uuid;
    //The last time archived.
    private long lastArchiveTimestamp;
    //The intervals at which the archive should be "archived"
    private long archiveTime = 0;

    public Archive(ArchiveMode type, TextChannel channel) {
        this.type = type;
        this.channel = channel;
        this.channelName = channel.getName();
        this.lastArchiveTimestamp = System.currentTimeMillis();
        this.uuid = UUID.randomUUID();
    }

    //Db Const
    public Archive(TextChannel channel, UUID uuid, long lastArchiveTimestamp, long archiveTime, ArchiveMode type) {
        this.channelName = channel.getName();
        this.channel = channel;
        this.uuid = uuid;
        this.lastArchiveTimestamp = lastArchiveTimestamp;
        this.archiveTime = archiveTime;
        this.type = type;
    }

    public UUID getUuid() {
        return uuid;
    }

    public ArchiveMode getType() {
        return type;
    }

    public String getChannelName() {
        return channelName;
    }

    public TextChannel getChannel() {
        return channel;
    }

    public void setChannel(TextChannel channel) {
        this.channel = channel;
    }

    public void setType(ArchiveMode type) {
        this.type = type;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public long getLastArchiveTimestamp() {
        return lastArchiveTimestamp;
    }

    public long getArchiveTime() {
        return archiveTime;
    }

    public void setArchiveTime(long archiveTime) {
        this.archiveTime = archiveTime;
    }

    public void setLastArchiveTimestamp(long lastArchiveTimestamp) {
        this.lastArchiveTimestamp = lastArchiveTimestamp;
    }
}
