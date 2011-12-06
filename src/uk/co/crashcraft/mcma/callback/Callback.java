package uk.co.crashcraft.mcma.callback;

import uk.co.crashcraft.mcma.Main;
import uk.co.crashcraft.mcma.callback.JSONHandler;

import org.bukkit.ChatColor;

import java.util.HashMap;

public class Callback {
    private final Main MCAC;

    public Callback (Main p) {
        MCAC = p;
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
			} catch (InterruptedException e) {
			}
		}
	}

    public void forceRequest () {
        mainRequest();
    }

    private void mainRequest(){
		JSONHandler webHandle = new JSONHandler( MCAC );
		HashMap<String, String> url_items = new HashMap<String, String>();
		url_items.put( "maxPlayers", String.valueOf( MCAC.getServer().getMaxPlayers() ) );
		url_items.put( "version", MCAC.getDescription().getVersion() );
		url_items.put( "exec", "callBack" );
		HashMap<String, String> response = webHandle.mainRequest(url_items);
		if(response.containsKey("oldVersion")){
            String oldVersion = response.get("oldVersion");
			if(!oldVersion.equals("")){
				// Version replies can be:
				// 3.3imp, 3.3
				// The former being the update is important and should be downloaded ASAP, the latter is that the update does not contain a critical fix/patch
				if (oldVersion.endsWith("imp")) {
					oldVersion = oldVersion.replace("imp", "");
					MCAC.broadcastView( ChatColor.BLUE + "A newer version of MCAC (" + oldVersion + ") is now available!");
					MCAC.broadcastView( ChatColor.RED + "This is an important/critical update.");
				} else {
					MCAC.broadcastView( ChatColor.BLUE + "A newer version of MCAC (" + oldVersion + ") is now available!");
				}
                if (response.containsKey("patchNotes")) {
                    String patchNotes = response.get("patchNotes");
                    if(!patchNotes.equals("")){
                        MCAC.broadcastView( ChatColor.BLUE + "Patch Notes v" + oldVersion);
                        MCAC.broadcastView(patchNotes);
                    }
                }
			}
		}
        if(response.containsKey("hasNotices")) {
            for(String cb : response.keySet()) {
                if (cb.contains("notice")) {
                    MCAC.broadcastView( ChatColor.GOLD + "Notice: " + ChatColor.WHITE + response.get(cb));
                }
            }
        }
		MCAC.hasErrored(response);
	}
}
