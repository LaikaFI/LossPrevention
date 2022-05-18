package link.alpinia.LossPrevention.model;

import link.alpinia.LossPrevention.LossPrevention;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.ArrayList;
import java.util.List;

public class ArchiveManager {
    public List<Archive> channelsForArchival;

    public ArchiveManager() {
        channelsForArchival = new ArrayList<>();
    }

    public List<Archive> getChannelsForArchival() {
        return channelsForArchival;
    }

    /**
     * Returns using a safe response in-case the name of the channel had been changed.
     * @param channel the channel to look for
     * @return archive if located.
     */
    public Archive getArchiveByChannel(TextChannel channel) {
        for(var a : channelsForArchival) {
            if(a.getChannel().equals(channel)) {
                return a;
            }
        }
        return null;
    }

    public List<Archive> fetchArchivesByGuild(Guild guild) {
        List<Archive> a = new ArrayList<>();
        for(var ar : channelsForArchival) {
            if(ar.getChannel().getGuild().equals(guild)) {
                a.add(ar);
            }
        }
        return a;
    }

    public void deregisterTextChannel(TextChannel channel) {
        var arch = getArchiveByChannel(channel);
        LossPrevention.instance.database.deleteArchive(arch);
        channelsForArchival.remove(arch);
    }

    public void loadArchives(List<Archive> archives) {
        this.channelsForArchival = archives;
    }

    public void addArchive(Archive archive) {
        channelsForArchival.add(archive);
    }
}
