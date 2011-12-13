package uk.co.crashcraft.mcma.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import uk.co.crashcraft.mcma.Main;

public class Disable implements CommandExecutor{

    private final Main MCAC;

    public Disable(Main MCAC){
        this.MCAC=MCAC;
    }
    
    @Override
    public boolean onCommand(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
        this.MCAC.broadcastView(ChatColor.RED + "Stopping..");
        this.MCAC.getServer().getPluginManager().disablePlugin(this.MCAC.pluginInterface("MCAC"));
        return true;
    }

}
