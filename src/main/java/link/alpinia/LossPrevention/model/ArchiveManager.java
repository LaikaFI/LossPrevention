package link.alpinia.LossPrevention.model;

import java.util.HashMap;

public class ArchiveManager {

    /**
     * <Channel Name, Archive Info Class>
     */
    public HashMap<String, Archive> channelsForArchival;

    public ArchiveManager() {
        channelsForArchival = new HashMap<>();
    }

    public HashMap<String, Archive> getChannelsForArchival() {
        return channelsForArchival;
    }

    public void setChannelsForArchival(HashMap<String, Archive> channelsForArchival) {
        this.channelsForArchival = channelsForArchival;
    }

    public void addArchive(Archive archive) {
        if(!channelsForArchival.containsKey(archive.getChannelName())) {
            //Not contained, put.
            channelsForArchival.put(archive.getChannelName(), archive);
        }
    }
}
