package es.jlh.epicCastle.plugin;

import com.massivecraft.factions.Factions;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import es.jlh.epicCastle.command.InfoCommandExecutor;
import es.jlh.epicCastle.utils.Lang;
import es.jlh.epicCastle.event.FactionLeaveServer;
import es.jlh.epicCastle.event.InventoryClick;
import es.jlh.epicCastle.event.PlayerMessageCastle;
import es.jlh.epicCastle.event.FactionMessageCastle;
import es.jlh.epicCastle.event.PlayerSign;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Julián
 */
public class EpicCastle extends JavaPlugin {

    public static final String PLUGIN = ChatColor.GOLD + "[" + ChatColor.GRAY
            + "EpicCastle" + ChatColor.GOLD + "] ";
                
    private static Logger log;    
    private EpicCastleManager manager = null;    
    private Economy economy;
    
    private boolean invalidBukkit = false;
    
    private void checkBukkitVersion() {
        try {
            Class.forName("net.minecraft.server.v1_7_R3.Packet");
            this.invalidBukkit = false;
        } 
        catch (ClassNotFoundException e) {
            this.invalidBukkit = true;
        }
    }
    
    @Override
    public void onLoad() {
        log = this.getLogger();
        manager = new EpicCastleManager(this);
        
        this.checkBukkitVersion();       

        if (this.invalidBukkit) {
            log.severe("Tu version no es soportada!");
            log.severe("Este plugin funciona con la version 1.7.8/1.7.9!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }        
    }
    
    @Override
    public void onEnable() {
        if (!this.setupEconomy() || !this.setupFactions() || !this.setupWorldGuard()) {
            this.getServer().getConsoleSender().sendMessage(PLUGIN + 
                    ChatColor.RED + ChatColor.BOLD + "Se va a desactivar el plugin");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        
        Bukkit.getServer().getPluginManager().registerEvents(new FactionMessageCastle(this), this);     
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerSign(this), this);
        Bukkit.getServer().getPluginManager().registerEvents(new FactionLeaveServer(this), this);
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerMessageCastle(this), this);
        Bukkit.getServer().getPluginManager().registerEvents(new InventoryClick(this), this);
        
        getCommand("ece").setExecutor(new InfoCommandExecutor(this));
        
        // Cargo los locales
        try {            
            Lang.load();
        } 
        catch (Exception ex) {
            this.getServer().getConsoleSender().sendMessage(PLUGIN + 
                        ChatColor.RED + ex.getMessage());
        }
        
        manager.onEnable();        
        
        log.info(Lang.PLUGIN_ENABLED.getText());
    }

    @Override
    public void onDisable() {
        if (manager != null) {
            manager.onDisable();
        }
        log.info(Lang.PLUGIN_DISABLED.getText());
    }

    public boolean setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {         
            this.getServer().getConsoleSender().sendMessage(PLUGIN + 
                    ChatColor.RED + "Es necesario tener instalado el Vault");
            return false;
        }
        
        RegisteredServiceProvider<Economy> economyProvider = 
                getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }
        else {
            Bukkit.getServer().getConsoleSender().sendMessage(EpicCastle.PLUGIN + 
                    ChatColor.RED + "Es necesario un plugin de economia");
            return false;
        }
        
        return true;
    }

    public boolean setupWorldGuard() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("WorldGuard");
        
        if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
            this.getServer().getConsoleSender().sendMessage(PLUGIN + 
                    ChatColor.RED + "Es necesario tener instalado el WorldGuard");           
            return false;
        }  
        
        return true;
    }

    public boolean setupFactions() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Factions");
        
        if (plugin == null || !(plugin instanceof Factions)) {
            this.getServer().getConsoleSender().sendMessage(PLUGIN + 
                    ChatColor.RED + "Es necesario tener instalado el Factions");
            return false;
        }     
        
        return true;
    }

    public Economy getEconomy() {
            return economy;
    }    

    public WorldGuardPlugin getWorldguard() {
        return (WorldGuardPlugin)Bukkit.getPluginManager().getPlugin("WorldGuard");
    }

    public Factions getFactions() {
        return (Factions)Bukkit.getPluginManager().getPlugin("Factions");
    }
    
    public EpicCastle getPlugin() {
            return this;
    }

    public static Logger getLog() {
        return log;
    }

    public EpicCastleManager getManager() {
            return manager;
    }
}
