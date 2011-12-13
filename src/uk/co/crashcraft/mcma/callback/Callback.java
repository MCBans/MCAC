package uk.co.crashcraft.mcma.callback;

import uk.co.crashcraft.mcma.Main;
import uk.co.crashcraft.mcma.callback.JSONHandler;

import org.bukkit.ChatColor;

import java.util.HashMap;

public class Callback {
    private final Main MCAC;

    public Callback (Main p) {
        this.MCAC = p;
    }

	public void run(){
		int callBackInterval = 600000;
		if(callBackInterval<600000){
			callBackInterval=600000;
		}
		while(true){
			this.mainRequest();
			try {
				Thread.sleep(callBackInterval);
			} catch (final InterruptedException e) {
			}
		}
	}

    public void forceRequest () {
        this.mainRequest();
    }

    private void mainRequest(){
		final JSONHandler webHandle = new JSONHandler( this.MCAC );
		final HashMap<String, String> url_items = new HashMap<String, String>();
		url_items.put( "maxPlayers", String.valueOf( this.MCAC.getServer().getMaxPlayers() ) );
		url_items.put( "version", this.MCAC.getDescription().getVersion() );
		url_items.put( "exec", "callBack" );
		final HashMap<String, String> response = webHandle.mainRequest(url_items);
		if(response.containsKey("oldVersion")){
            String oldVersion = response.get("oldVersion");
			if(!oldVersion.equals("")){
				// Version replies can be:
				// 3.3imp, 3.3
				// The former being the update is important and should be downloaded ASAP, the latter is that the update does not contain a critical fix/patch
				if (oldVersion.endsWith("imp")) {
					oldVersion = oldVersion.replace("imp", "");
					this.MCAC.broadcastView( ChatColor.BLUE + "A newer version of MCAC (" + oldVersion + ") is now available!");
					this.MCAC.broadcastView( ChatColor.RED + "This is an important/critical update.");
				} else {
					this.MCAC.broadcastView( ChatColor.BLUE + "A newer version of MCAC (" + oldVersion + ") is now available!");
				}
                if (response.containsKey("patchNotes")) {
                    final String patchNotes = response.get("patchNotes");
                    if(!patchNotes.equals("")){
                        this.MCAC.broadcastView( ChatColor.BLUE + "Patch Notes v" + oldVersion);
                        this.MCAC.broadcastView(patchNotes);
                    }
                }
			}
		}
        if(response.containsKey("hasNotices")) {
            for(final String cb : response.keySet()) {
                if (cb.contains("notice")) {
                    this.MCAC.broadcastView( ChatColor.GOLD + "Notice: " + ChatColor.WHITE + response.get(cb));
                }
            }
        }
		this.MCAC.hasErrored(response);
	}
}
