package uk.co.crashcraft.mcma;

import org.bukkit.ChatColor;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import uk.co.crashcraft.mcma.callback.Callback;
import uk.co.crashcraft.mcma.log.MCACLogger;
import uk.co.crashcraft.mcma.callback.JSONHandler;
import uk.co.crashcraft.mcma.commands.Disable;
import uk.co.crashcraft.mcma.commands.Test;

import cc.co.evenprime.bukkit.nocheat.NoCheat;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.player.PlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;

import java.util.ArrayList;
import java.util.HashMap;

public class Main extends JavaPlugin {
    public PluginManager pluginManager;
    public CraftServer craftServer;
    public Callback callback = null;
    public NoCheat noCheat = null;
    public MCACLogger logger = new MCACLogger(this);
    public String APIKey = "crashdoomtest123";
    public String gitVersion = "@@GITREVISION@@";
    public String buildVersion = "@@BUILDVERSION@@";
    private final ArrayList<String> activeUsernames = new ArrayList<String>();

    @Override
    public void onDisable() {
        this.logger.log("Shutting Down...");
	}

    @Override
    public void onEnable() {
        if (!this.gitVersion.contains("GITREVISION") || !this.buildVersion.contains("BUILDVERSION")) {
            this.logger.log("Initializing v" + this.getServer().getVersion() + " git-" + this.gitVersion + " b" + this.buildVersion + "bamboo");
        } else {
            this.logger.log("Initializing v" + this.getServer().getVersion());
        }

		this.pluginManager = this.getServer().getPluginManager();
        this.craftServer = (CraftServer) this.getServer();

        if(!this.onlineCheck()){
            return;
        }

        final Plugin noCheatPlug = this.pluginManager.getPlugin("NoCheat");
        if (noCheatPlug == null) {
            this.logger.log(MCACLogger.logState.FATAL, "NoCheat could not be found!");
            return;
        }
        this.noCheat = (NoCheat) noCheatPlug;

        this.pluginManager.registerEvent(Type.PLAYER_PRELOGIN, new PlayerListener () {
            @Override
            public void onPlayerPreLogin(PlayerPreLoginEvent event) {
                if(!Main.this.onlineCheck()){
                    return;
                }
                final String playerName = event.getName();
                final JSONHandler webHandle = new JSONHandler( Main.this );
                final HashMap<String, String> url_items = new HashMap<String, String>();
                url_items.put("player", playerName);
                url_items.put("playerip", event.getAddress().getHostAddress());
                url_items.put("exec", "playerConnect");
                final HashMap<String, String> serverData = webHandle.mainRequest(url_items);
                try {
                    if (serverData.containsKey("isBanned")) {
                        if (serverData.get("isBanned").equals("true")) {
                            event.disallow(Result.KICK_BANNED, "[MCAC] Your account has been banned due to a cheating infraction.");
                            return;
                        }
                    } else {
                        Main.this.hasErrored(serverData);
                    }
                } catch (final NullPointerException e) {
                    Main.this.logger.log(MCACLogger.logState.WARNING, "Unable to connect to the MCAC Server!");
                }
                Main.this.activeUsernames.add(playerName);
            }
        }, Priority.Highest, this);

        this.pluginManager.registerEvent(Type.PLAYER_JOIN, new PlayerListener () {
            @Override
            public void onPlayerJoin(PlayerJoinEvent event) {
                event.getPlayer().sendMessage(ChatColor.AQUA + "[MCAC] " + ChatColor.RED + "This server is MCAC protected!");
                // Disclaimer: Upon seeing this message, or this message being sent from the server the user is responsible fully for his/her actions and the resulting MCAC punishment
            }
        }, Priority.Monitor, this);

        this.pluginManager.registerEvent(Type.PLAYER_QUIT, new PlayerListener () {
            @Override
            public void onPlayerQuit(PlayerQuitEvent event) {
                final String playerName = event.getPlayer().getName();
                Main.this.activeUsernames.remove(playerName);
            }
        }, Priority.Monitor, this);
        
        
        //Registering commands!
        this.getCommand("disable").setExecutor(new Disable(this));
        this.getCommand("test").setExecutor(new Test(this));

        
        this.logger.log(MCACLogger.logState.INFO, "Communicating with Master Server");

        this.callback = new Callback(this);

        this.callback.forceRequest();


        this.logger.log(MCACLogger.logState.INFO, "All Systems Operational!");
    }


    /**
     * Sends a message to all users with the mcac.view permission node
     * @param msg
     */
    public void broadcastView(String msg){
		for( final Player player: this.getServer().getOnlinePlayers() ){
			if(player.hasPermission("mcac.view")){
				player.sendMessage(ChatColor.AQUA + "[MCAC] " + ChatColor.WHITE + msg );
			}
		}
	}

    /**
     * Sends a message to the specified player
     * @param Player
     * @param msg
     */
    public void broadcastPlayer( String Player, String msg ){
		final Player target = this.getServer().getPlayer(Player);
		if(target!=null){
			target.sendMessage(ChatColor.AQUA + "[MCAC] " + ChatColor.WHITE + msg );
		}else{
		    this.logger.log( msg );
		}
	}

    /**
     * Returns a boolean depending on if the HashMap contains an error response
     * @param response
     * @return boolean
     */
    public boolean hasErrored (HashMap<String, String> response) {
		if (response.containsKey("error")) {
			final String error = response.get("error");
			if (error.contains("Server Disabled")) {
				this.broadcastView(ChatColor.RED + "MCAC Abuse Detected");
				this.broadcastView("Abuse of the MCAC System was detected and resulted in this server being blacklisted.");
				this.logger.log(MCACLogger.logState.SEVERE, "The server API key has been disabled by MCAC.");
				this.logger.log(MCACLogger.logState.FATAL, "To appeal this decision, please contact an administrator.");
			} else {
				this.broadcastView( ChatColor.RED + "Unexpected reply from MCAC API!");
				this.logger.log(MCACLogger.logState.SEVERE, "API returned an invalid error:");
				this.logger.log(MCACLogger.logState.SEVERE, "Server said: " + error);
			}
			return true;
		} else {
			return false;
		}
	}

    /**
     * Returns the plugin interface for MCBans
     * @param pluginName
     * @return Plugin
     */
    public Plugin pluginInterface( String pluginName ){
		return this.getServer().getPluginManager().getPlugin(pluginName);
	}
    
    public boolean onlineCheck(){
        if (!this.craftServer.getServer().onlineMode) {
            this.logger.log(MCACLogger.logState.FATAL, "You must be running in online mode!");
            return false;
        } return true;
    }
}
