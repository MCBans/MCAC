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
    private ArrayList<String> activeUsernames = new ArrayList<String>();

    public void onDisable() {
        logger.log("Shutting Down..");
	}

    public void onEnable() {
        if (!gitVersion.contains("GITREVISION") || !buildVersion.contains("BUILDVERSION")) {
            logger.log("Using v" + this.getServer().getVersion() + " git-" + gitVersion + " b" + buildVersion + "bamboo");
        } else {
            logger.log("Using v" + this.getServer().getVersion());
        }
        logger.log("Please wait.. Starting.");

		pluginManager = getServer().getPluginManager();
        craftServer = (CraftServer) getServer();

        if (!craftServer.getServer().onlineMode) {
            logger.log(MCACLogger.logState.FATAL, "You must be running in online mode!");
            return;
        }

        logger.log(MCACLogger.logState.INFO, "Checking Compatibility");

        Plugin noCheatPlug = pluginManager.getPlugin("NoCheat");
        if (noCheatPlug == null) {
            logger.log(MCACLogger.logState.FATAL, "NoCheat could not be found!");
            return;
        }

        noCheat = (NoCheat) noCheatPlug;

        logger.log(MCACLogger.logState.INFO, "Found NoCheat");

        logger.log(MCACLogger.logState.INFO, "Hooking Listeners");

        pluginManager.registerEvent(Type.PLAYER_PRELOGIN, new PlayerListener () {
            @Override
            public void onPlayerPreLogin(PlayerPreLoginEvent event) {
                if (!craftServer.getServer().onlineMode) {
                    logger.log(MCACLogger.logState.FATAL, "You must be running in online mode!");
                    return;
                }
                String playerName = event.getName();
                JSONHandler webHandle = new JSONHandler( Main.this );
                HashMap<String, String> url_items = new HashMap<String, String>();
                url_items.put("player", playerName);
                url_items.put("playerip", event.getAddress().getHostAddress());
                url_items.put("exec", "playerConnect");
                HashMap<String, String> serverData = webHandle.mainRequest(url_items);
                try {
                    if (serverData.containsKey("isBanned")) {
                        if (serverData.get("isBanned").equals("true")) {
                            event.disallow(Result.KICK_BANNED, "[MCAC] Your account has been banned due to a cheating infraction.");
                            return;
                        }
                    } else {
                        hasErrored(serverData);
                    }
                } catch (NullPointerException e) {
                    logger.log(MCACLogger.logState.WARNING, "Unable to connect to the MCAC Server!");
                }
                activeUsernames.add(playerName);
            }
        }, Priority.Highest, this);

        pluginManager.registerEvent(Type.PLAYER_JOIN, new PlayerListener () {
            @Override
            public void onPlayerJoin(PlayerJoinEvent event) {
                event.getPlayer().sendMessage(ChatColor.AQUA + "[MCAC] " + ChatColor.RED + "This server is MCAC protected!");
                // Disclaimer: Upon seeing this message, or this message being sent from the server the user is responsible fully for his/her actions and the resulting MCAC punishment
            }
        }, Priority.Monitor, this);

        pluginManager.registerEvent(Type.PLAYER_QUIT, new PlayerListener () {
            @Override
            public void onPlayerQuit(PlayerQuitEvent event) {
                String playerName = event.getPlayer().getName();
                activeUsernames.remove(playerName);
            }
        }, Priority.Monitor, this);
        
        
        //Registering commands!
        getCommand("disable").setExecutor(new Disable(this));
        getCommand("test").setExecutor(new Test(this));

        
        logger.log(MCACLogger.logState.INFO, "Communicating with Master Server");

        callback = new Callback(this);

        callback.forceRequest();


        logger.log(MCACLogger.logState.INFO, "All Systems Operational!");
    }


    /**
     * Sends a message to all users with the mcac.view permission node
     * @param msg
     */
    public void broadcastView(String msg){
		for( Player player: this.getServer().getOnlinePlayers() ){
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
		Player target = this.getServer().getPlayer(Player);
		if(target!=null){
			target.sendMessage(ChatColor.AQUA + "[MCAC] " + ChatColor.WHITE + msg );
		}else{
		    logger.log( msg );
		}
	}

    /**
     * Returns a boolean depending on if the HashMap contains an error response
     * @param response
     * @return boolean
     */
    public boolean hasErrored (HashMap<String, String> response) {
		if (response.containsKey("error")) {
			String error = response.get("error");
			if (error.contains("Server Disabled")) {
				broadcastView(ChatColor.RED + "MCAC Abuse Detected");
				broadcastView("Abuse of the MCAC System was detected and resulted in this server being blacklisted.");
				logger.log(MCACLogger.logState.SEVERE, "The server API key has been disabled by an MCAC ");
				logger.log(MCACLogger.logState.FATAL, "To appeal this decision, please contact an administrator");
			} else {
				broadcastView( ChatColor.RED + "Unexpected reply from MCAC API!");
				logger.log(MCACLogger.logState.SEVERE, "API returned an invalid error:");
				logger.log(MCACLogger.logState.SEVERE, "Server said: " + error);
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
}
