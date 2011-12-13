package uk.co.crashcraft.mcma.log;

import uk.co.crashcraft.mcma.Main;

public class MCACLogger {
    private final Main MCAC;

    public MCACLogger (Main main) {
        this.MCAC = main;
    }

    public enum logState {
        FATAL, SEVERE, WARNING, NOTICE, INFO, NONE
    }

    public void log (String message) {
        this.log(logState.NONE, message);
    }

    public void log (logState type, String message) {
        switch (type) {
            case FATAL:
                System.out.print("[MCAC] [FATAL] " + message);
                this.MCAC.getServer().getPluginManager().disablePlugin(this.MCAC.pluginInterface("MCAC"));
                break;
            case SEVERE:
                System.out.print("[MCAC] [SEVERE] " + message);
                break;
            case WARNING:
                System.out.print("[MCAC] [WARNING] " + message);
                break;
            case NOTICE:
                System.out.print("[MCAC] [NOTICE] " + message);
                break;
            case INFO:
                System.out.print("[MCAC] [INFO] " + message);
                break;
            default:
                System.out.print("[MCAC] " + message);
                break;
        }
    }
}
