package uk.co.crashcraft.mcma.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import uk.co.crashcraft.mcma.Main;
import uk.co.crashcraft.mcma.log.MCACLogger;

public class Test implements CommandExecutor{

    private Main MCAC;

    public Test(Main MCAC){
        this.MCAC=MCAC;
    }
    
    @Override
    public boolean onCommand(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
        MCAC.logger.log(MCACLogger.logState.NOTICE, "Dialing Home...");
        MCAC.callback.forceRequest();
        return true;
    }

}
