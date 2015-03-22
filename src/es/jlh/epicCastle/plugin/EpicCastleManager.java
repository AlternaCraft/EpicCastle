/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package es.jlh.epicCastle.plugin;

import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColl;
import es.jlh.epicCastle.utils.Lang;
import java.io.File;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Julian
 */
public class EpicCastleManager {
    
    public static final String HEADER = 
        "###############################################\n" +
        "#########  CONFIG EPIC CASTLE 1.7.X  ##########\n" +
        "###############################################\n" +
        "##         *** Creado por JuLiTo ***         ##\n" +
        "##              - 1.0 | 1.7.X -              ##\n" +
        "##        alternativecraft@hotmail.es        ##\n" +
        "###############################################\n" +
        "#############  INFORMACIÓN EXTRA  #############\n" +
        "###############################################\n" +
        "##     Guardo el dueño del castillo para     ##\n" +
        "##        hacer la llamada a las armas        ##\n" +
        "###############################################\n" +
        "###############  ¡¡DISFRÚTALO!!  ##############\n" +
        "###############################################\n\n" +
        "SI EL SERVER ESTA ENCENDIDO NO EDITES EL FICHERO DIRECTAMENTE";
    
    public static final int TICKS = 20;
    
    public static final String PLUGIN = ChatColor.WHITE + "[EpicCastle]";
    
    public static final int CASTLEXP = 0;
    public static final int CASTLEGOLD = 1;
    public static final int CASTLEDIAMOND = 2;
    public static final int CASTLEMONEY = 3;
    
    public static final int PLUGINNAME = 0;
    public static final int TCONQUISTA = 1;
    public static final int TRECOMPENSA = 2;
    public static final int OWNER = 3;    
    
    private int tareaXP = 0;
    private int tareaGold = 0;
    private int tareaDiamond = 0;
    private int tareaMoney = 0;
    
    private final EpicCastle plugin;    
    private FileConfiguration config;
    
    public double power;
    public Sign[] carteles;
    public Location[] teleports;
    
    //private List<Localizacion> configuration = new ArrayList();
    
    /**
     * Constructor de la clase
     * @param pl
     */
    public EpicCastleManager(EpicCastle pl) {
        plugin = pl;
    }

    public void onEnable() {        
        //Cargar el config
        plugin.reloadConfig();
        
        if (!new File(new StringBuilder().append(
                plugin.getDataFolder()).append(
                        File.separator).append(
                                "config.yml").toString()).exists()) {
            plugin.getConfig().options().header(HEADER);        
            plugin.getConfig().options().copyDefaults(true);
        }
        
        plugin.saveConfig();
        config = plugin.getConfig();
        
        power = config.getDouble("rewards.power");
        
        // Cargo los carteles
        cargaCarteles();       
        recompensador();
    }
    
    public void onDisable() {
        plugin.saveConfig();
        
        if (tareaXP != 0) Bukkit.getServer().getScheduler().cancelTask(tareaXP);
        if (tareaGold != 0) Bukkit.getServer().getScheduler().cancelTask(tareaGold);
        if (tareaDiamond != 0) Bukkit.getServer().getScheduler().cancelTask(tareaDiamond);
        if (tareaMoney != 0) Bukkit.getServer().getScheduler().cancelTask(tareaMoney);
    }
    
    public void cargaCarteles() {
        int vuelta = 0;
        int cont = 0;
        carteles = new Sign[4];
        teleports = new Location[4];
        
        do {
            String x = config.getString(turno(vuelta) + ".location.x");
            String y = config.getString(turno(vuelta) + ".location.y");
            String z = config.getString(turno(vuelta) + ".location.z");
            String worldName = config.getString(turno(vuelta) + ".location.world");
            String teleport = config.getString(turno(vuelta) + ".teleport");
            
            // Variable que guarda la localización del teleport del castillo
            if (teleport != null) {
                String[] coords = teleport.split(" ");
                teleports[vuelta] = new Location(
                        Bukkit.getServer().getWorld(coords[0]), 
                        Integer.valueOf(coords[1]), 
                        Integer.valueOf(coords[2]), 
                        Integer.valueOf(coords[3]));
            }

            if (x!=null && y != null && z != null && worldName != null) {
                World world = Bukkit.getServer().getWorld(worldName);
                
                Block pr = world.getBlockAt(Integer.valueOf(x), Integer.valueOf(y), Integer.valueOf(z));
                
                if (pr.getType() != Material.SIGN_POST && pr.getType() != Material.WALL_SIGN) {
                    plugin.getServer().getConsoleSender().sendMessage (
                            EpicCastle.PLUGIN + Lang.SIGN_ERROR_NOT_EXISTS.getText()
                                    .replace("%TYPE%", turno(vuelta)));
                    return;
                }
                
                try {
                    Sign castle = (Sign) pr.getState();
                    carteles[vuelta] = castle;
                    cont++;
                }
                catch (ClassCastException ex) {
                    //System.out.println("Excepcion: " + ex.getMessage());
                }                
            }       
            
            if (vuelta == 3) {
                plugin.getServer().getConsoleSender().sendMessage(
                        EpicCastle.PLUGIN + Lang.SIGN_LOADS.getText()
                                .replace("%CANT%", String.valueOf(cont)));
                break;
            }
            
            vuelta++;
        }   
        while(true);
    }
    
    public void recompensador() {        
        int xp = (valorSign(CASTLEXP, TRECOMPENSA) != null) ? Integer.valueOf(valorSign(CASTLEXP, TRECOMPENSA)) : 300;
        int gold = (valorSign(CASTLEGOLD, TRECOMPENSA) != null) ? Integer.valueOf(valorSign(CASTLEGOLD, TRECOMPENSA)) : 300;
        int diamond = (valorSign(CASTLEDIAMOND, TRECOMPENSA) != null) ? Integer.valueOf(valorSign(CASTLEDIAMOND, TRECOMPENSA)) : 300;
        int money = (valorSign(CASTLEMONEY, TRECOMPENSA) != null) ? Integer.valueOf(valorSign(CASTLEMONEY, TRECOMPENSA)) : 300;            
        
        // Recompensas
        tareaXP = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (carteles[CASTLEXP] != null) {
                String owner = valorSign(CASTLEXP, OWNER);
                // String world = carteles[CASTLEXP].getWorld().getName();
                
                Faction faction = FactionColl.get().getByName(owner);
                int cant = config.getInt("rewards.xp");
                
                if (faction != null) {
                    for (Player player : faction.getOnlinePlayers()) {
                        player.sendMessage(EpicCastle.PLUGIN +
                                Lang.CASTLE_REWARD.getText()
                                        .replace("%CANT%", String.valueOf(cant))
                                        .replace("%TYPE%", "de experiencia"));
                        player.giveExp(cant);
                    } 
                }
            }
        }, TICKS * xp, TICKS * xp);
        
        tareaGold = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (carteles[CASTLEGOLD] != null) {
                String owner = valorSign(CASTLEGOLD, OWNER);
                // String world = carteles[CASTLEGOLD].getWorld().getName();
                Faction faction = FactionColl.get().getByName(owner);
                
                int cant = config.getInt("rewards.gold");
                
                if (faction != null) {
                    for (Player player : faction.getOnlinePlayers()) {
                        player.sendMessage(EpicCastle.PLUGIN +
                                Lang.CASTLE_REWARD.getText()
                                        .replace("%CANT%", String.valueOf(cant))
                                        .replace("%TYPE%", "de oro"));
                        player.getInventory().addItem(new ItemStack(Material.GOLD_INGOT, cant));
                    }                    
                }
            }
        }, TICKS * gold, TICKS * gold);
        
        tareaDiamond = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (carteles[CASTLEDIAMOND] != null) {
                String owner = valorSign(CASTLEDIAMOND, OWNER);
                // String world = carteles[CASTLEDIAMOND].getWorld().getName();
                
                Faction faction = FactionColl.get().getByName(owner);
                int cant = config.getInt("rewards.diamond");
                
                if (faction != null) {
                    for (Player player : faction.getOnlinePlayers()) {
                        player.sendMessage(EpicCastle.PLUGIN +
                                Lang.CASTLE_REWARD.getText()
                                        .replace("%CANT%", String.valueOf(cant))
                                        .replace("%TYPE%", "de diamante"));
                        player.getInventory().addItem(new ItemStack(Material.DIAMOND, cant));
                    }                    
                }
            }
        }, TICKS * diamond, TICKS * diamond);                
        
        tareaMoney = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (carteles[CASTLEMONEY] != null) {
                Economy econ = plugin.getEconomy();
                
                String owner = valorSign(CASTLEMONEY, OWNER);
                // String world = carteles[CASTLEMONEY].getWorld().getName();
                
                Faction faction = FactionColl.get().getByName(owner);
                int cant = config.getInt("rewards.money");
                
                if (faction != null) {
                    for (Player player : faction.getOnlinePlayers()) {
                        player.sendMessage(EpicCastle.PLUGIN +
                                Lang.CASTLE_REWARD.getText()
                                        .replace("%CANT%", String.valueOf(cant))
                                        .replace("%TYPE%", "de dinero"));
                        econ.depositPlayer(player, cant);
                    }                    
                }
            }            
        }, TICKS * money, TICKS * money);
    }
    
    public void registraCartel(Location l, String tipo) {
        config.set(tipo + ".location.x", l.getBlockX());
        config.set(tipo + ".location.y", l.getBlockY());
        config.set(tipo + ".location.z", l.getBlockZ());
        config.set(tipo + ".location.world", l.getWorld().getName());
        plugin.saveConfig();
    }
    
    public void registraTp(Location l, String tipo) {
        config.set(tipo + ".teleport", l.getWorld().getName() + " " + 
                l.getBlockX() + " " + l.getBlockY() + " " + l.getBlockZ());
        plugin.saveConfig();
    }
    
    public void borrarCartel(String tipo) {
        config.set(tipo + ".location", null);
        plugin.saveConfig();
    }
    
    public String valorSign(int cast, int pos) {
        try {   
            if (pos == TCONQUISTA || pos == TRECOMPENSA) {
                String v = carteles[cast].getLine(pos);
                return v.substring(v.indexOf("[")+1, v.indexOf("]"));
            }
            return carteles[cast].getLine(pos);
        } 
        catch (NullPointerException ex) {
            return null;
        }
    }
    
    public String turno(int valor) {
        switch (valor) {
            case CASTLEXP:
                return "castleXP";                
            case CASTLEGOLD:
                return "castleGold";                
            case CASTLEDIAMOND:
                return "castleDiamond";                
            case CASTLEMONEY:
                return "castleMoney";               
            default:
                return "";
        }
    }
    
    /**
     * Metodo para cambiar el <i>nombre por defecto</i> del castillo por uno mas
     * <i>agradable</i>
     * @param c Nombre por defecto
     * @return Nombre adecuado
     */
    public String cambiaNombre(String c) {
        switch (c) {
            case "castleXP":
                return Lang.CASTLE_TYPE_XP.getText();
            case "castleGold":
                return Lang.CASTLE_TYPE_GOLD.getText();
            case "castleDiamond":
                return Lang.CASTLE_TYPE_DIAMOND.getText();
            case "castleMoney":
                return Lang.CASTLE_TYPE_MONEY.getText();
            default:
                return null;
        }
    }
}