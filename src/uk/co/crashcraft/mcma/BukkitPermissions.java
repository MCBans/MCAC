package uk.co.crashcraft.mcma;

import com.nijiko.permissions.PermissionHandler;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

@SuppressWarnings("unused")
public class BukkitPermissions {
	private static PermissionHandler permissionHandler = null;
	private Main MCAC;

	public BukkitPermissions(Main p){
		MCAC = p;
	}
	public void setupPermissions() {
		Plugin permissionsPlugin = MCAC.pluginInterface("Permissions");
		if (permissionHandler == null) {
			if (permissionsPlugin != null) {
				permissionHandler = ((Permissions) permissionsPlugin).getHandler();
				MCAC.logger.log("Permissions plugin found!");
			}else{
				MCAC.logger.log("Using Bukkit permissions!");
			}
		}
	}
	public boolean isAllow(String WorldName, String PlayerName, String PermissionNode){
		Player target = MCAC.getServer().getPlayer(PlayerName);
		return target != null && isAllow( target, PermissionNode );
	}
	public boolean isAllow( Player Player, String PermissionNode ){
		if( permissionHandler != null ){
			if( permissionHandler.has( Player, "mcac."+PermissionNode ) ){
				return true;
			}
		}else if( Player.hasPermission( "mcac."+PermissionNode ) ){
			return true;
		}
		return false;
	}
	public  boolean inGroup( String WorldName, String PlayerName, String GroupName ){
		if( permissionHandler.inGroup( WorldName, PlayerName, GroupName ) ){
			return true;
		}
		return false;
	}
}
