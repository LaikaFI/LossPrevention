package moe.kayla.LossPrevention.model;

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
    private long archiveTime;

    public Archive(ArchiveMode type, TextChannel channel) {
        this.type = type;
        this.channel = channel;
        this.channelName = channel.getName();
        this.uuid = UUID.randomUUID();
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
