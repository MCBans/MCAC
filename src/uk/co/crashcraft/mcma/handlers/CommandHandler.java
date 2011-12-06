package uk.co.crashcraft.mcma.handlers;

import uk.co.crashcraft.mcma.Main;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import uk.co.crashcraft.mcma.log.Logger;

import java.util.HashMap;

public class CommandHandler {

	private Main MCAC;
	//private String[] protectedGroups;
	private HashMap<String, Integer> commandList = new HashMap<String, Integer>();

	public CommandHandler(Main p){
		MCAC = p;
	}

    public enum Commands {
        TEST, DISABLE
    }

	public boolean execCommand(String command, String[] args, CommandSender from){
        switch(Commands.valueOf(command.toUpperCase())){
            case TEST:
                MCAC.logger.log(Logger.logState.NOTICE, "Dialing Home...");
                MCAC.callback.forceRequest();
                break;
            case DISABLE:
                MCAC.broadcastView(ChatColor.RED + "Stopping..");
                MCAC.getServer().getPluginManager().disablePlugin(MCAC.pluginInterface("MCAC"));
                break;
            default:
                MCAC.broadcastView(ChatColor.RED + "Invalid Command!");
                break;
        }
        return true;
    }
}
