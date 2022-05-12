package link.alpinia.LossPrevention;

import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.file.YamlConfiguration;

import java.util.List;

public class LPConfig {

    YamlConfiguration configuration = new YamlConfiguration();

    public boolean loaded = false;

    private String token;
    private String clientId;
    private String prefix;

    private String logChannel;
    private long bypassRole;
    private int banTime;
    private List<String> bypassUsers;

    public LPConfig() {
        initializeCfg();
    }

    private void initializeCfg() {
        try {
            configuration.load("configuration/config.yml");
            loaded = true;
        } catch(Exception ex) {
            LossPrevention.getLogger().error("Failed to initialize Config!");
            ex.printStackTrace();
            Runtime.getRuntime().exit(0);
        }
    }

    public void loadConfigurations() {
        loadDiscordCfg();
        loadLossPreventionCfg();
    }

    private void loadDiscordCfg() {
        ConfigurationSection discord = configuration.getConfigurationSection("discord");
        token = discord.getString("token");
        clientId = discord.getString("clientId");
        prefix = discord.getString("prefix");
    }

    private void loadLossPreventionCfg() {
        ConfigurationSection lpc = configuration.getConfigurationSection("lossPrevention");
        logChannel = lpc.getString("loggingChannel");
        bypassRole = lpc.getLong("bypassRole");
        bypassUsers = lpc.getStringList("userBypass");
        banTime = lpc.getInt("banCooldown");
    }

    public String getToken() {
        return token;
    }

    public String getClientId() {
        return clientId;
    }

    public String getPrefix() { return prefix; }

    public List<String> getBypassUsers() { return bypassUsers;}

    public long getBypassRole() { return bypassRole; }

    public int getBanTimeInMilis() { return banTime * 60 * 1000; }

    public String getLogChannel() { return logChannel; }

    public String getInviteUrl() { return "https://discord.com/oauth2/authorize?client_id=" + getClientId() + "&scope=bot&permissions=8"; }
}
