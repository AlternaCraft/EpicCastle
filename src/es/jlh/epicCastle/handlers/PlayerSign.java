/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package es.jlh.epicCastle.handlers;

import com.massivecraft.factions.Rel;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColl;
import com.massivecraft.factions.entity.MPlayer;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import es.jlh.epicCastle.plugin.EpicCastle;
import static es.jlh.epicCastle.plugin.EpicCastleManager.CASTLEDIAMOND;
import static es.jlh.epicCastle.plugin.EpicCastleManager.CASTLEGOLD;
import static es.jlh.epicCastle.plugin.EpicCastleManager.CASTLEMONEY;
import static es.jlh.epicCastle.plugin.EpicCastleManager.CASTLEXP;
import static es.jlh.epicCastle.plugin.EpicCastleManager.OWNER;
import static es.jlh.epicCastle.plugin.EpicCastleManager.PLUGIN;
import static es.jlh.epicCastle.plugin.EpicCastleManager.PLUGINNAME;
import static es.jlh.epicCastle.plugin.EpicCastleManager.TCONQUISTA;
import static es.jlh.epicCastle.plugin.EpicCastleManager.TRECOMPENSA;
import es.jlh.epicCastle.utils.Area;
import es.jlh.epicCastle.utils.Lang;
import java.util.HashMap;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.scheduler.BukkitTask;

/**
 * Clase evento para comprobar la creacion del cartel de un castillo
 * @author Julian
 * @version 1.0
 */
public class PlayerSign implements Listener, EventExecutor {   
     
    private final EpicCastle plugin;
    private final WorldGuardPlugin wgplugin;        
    private final HashMap<String, Player> conquistadores = new HashMap();
    private final HashMap<Player, BukkitTask> conquista = new HashMap();
    private BukkitTask countdown;    
    
    public PlayerSign(EpicCastle pl) {
        this.plugin = pl;
        wgplugin = this.plugin.getWorldguard();
    }
    
    @EventHandler
    public void signCreate(SignChangeEvent e)  {
        Player pl = e.getPlayer();        
        String castle = null;
        
        // Objeto para obtener las zonas del world guard de un mundo
        RegionManager regionManager = wgplugin.getRegionManager(pl.getWorld()); 
        
        // Vector de la posicion del cartel
        BlockVector pos = new BlockVector(new Vector (
                e.getBlock().getLocation().getBlockX(), 
                e.getBlock().getLocation().getBlockY(), 
                e.getBlock().getLocation().getBlockZ()));        
        
        ProtectedRegion xp = regionManager.getRegion("castleXP");        
        ProtectedRegion gold = regionManager.getRegion("castleGold");
        ProtectedRegion diamond = regionManager.getRegion("castleDiamond");
        ProtectedRegion money = regionManager.getRegion("castleMoney");
        
        ProtectedRegion[] zonas = {xp, gold, diamond, money};
        
        for (ProtectedRegion pr : zonas) {
            if (pr != null && pr.contains(pos)) {
                castle = pr.getId();
                break;
            }
        }
        
        String plug = e.getLine(PLUGINNAME);
        
        // Compruebo la primera linea para saber si estan creando un cartel del plugin
        if (plug.contains("EpicCastle") || plug.contains("epiccastle")
                || plug.compareToIgnoreCase("epiccastle") == 0) {            
            
            if (!pl.hasPermission("ec.sign")) {
                pl.sendMessage(EpicCastle.PLUGIN + Lang.PLUGIN_NO_PERMISSION.getText());
                e.getBlock().breakNaturally();
                return;
            }
            
            if (castle == null) {
                pl.sendMessage(EpicCastle.PLUGIN + Lang.SIGN_ERROR_ZONE.getText());
                e.getBlock().breakNaturally();
                return;
            }              
            
            int tc, tr;
            
            try {
                tc = Integer.valueOf(e.getLine(TCONQUISTA));
                tr = Integer.valueOf(e.getLine(TRECOMPENSA));
                
                // Si no son numeros devuelve -1
                if (tc < 0 || tr <= 0 || tc > 999999999 || tr > 999999999) {
                    pl.sendMessage(EpicCastle.PLUGIN + Lang.SIGN_ERROR_TIME.getText());
                    e.getBlock().breakNaturally();
                    return;
                }                 
            }
            catch (NumberFormatException ex) {
                pl.sendMessage(EpicCastle.PLUGIN + Lang.SIGN_ERROR_TIME.getText());
                e.getBlock().breakNaturally();
                return;
            }
                        
            // Compruebo si ya existe uno creado
            if (plugin.getManager().carteles[compTipo(castle)] != null) {
                pl.sendMessage(EpicCastle.PLUGIN + Lang.SIGN_ERROR_CREATE.getText()
                        .replace("%CASTLE%", cambiaString(castle ,true)));
                e.getBlock().breakNaturally();
                return;
            }

            // Edito el cartel
            e.setLine(PLUGINNAME, PLUGIN);
            e.setLine(TCONQUISTA, "TC [" + String.valueOf(tc) + "]");
            e.setLine(TRECOMPENSA, "TR [" + String.valueOf(tr) + "]");
            e.setLine(OWNER, "");                          

            pl.sendMessage(EpicCastle.PLUGIN + Lang.SIGN_CREATED.getText().replace("%TYPE%", cambiaString(castle, true)));
            plugin.getManager().registraCartel(e.getBlock().getLocation(), cambiaString(castle ,false));
            plugin.getManager().cargaCarteles();        
        }
    }
    
    @EventHandler
    public void SignClicked(PlayerInteractEvent e) {
        Player pl = e.getPlayer();
        
        // Conquista solo sin gamemode y con el click derecho
        if (pl.getGameMode() == GameMode.CREATIVE || e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        Sign[] carteles = plugin.getManager().carteles;
        String zona = null;
        
        // Compruebo si es un cartel
        if (e.hasBlock() && (e.getClickedBlock().getType() == Material.WALL_SIGN 
                || e.getClickedBlock().getType() == Material.SIGN_POST)) {            
            
            // Debo recibir el estado para que no me de error
            Sign pr = (Sign) e.getClickedBlock().getState();
            
            for (int i = 0; i < carteles.length; i++) {
                if (carteles[i] != null && pr.getLocation().equals(carteles[i].getLocation())) {
                    zona = plugin.getManager().turno(i).toLowerCase();
                    break;
                }
            }
            
            if (zona != null) {                
                String fac = pr.getLine(OWNER);
                String v = pr.getLine(TCONQUISTA);
                int tiempo = Integer.valueOf(v.substring(v.indexOf("[")+1, v.indexOf("]")));

                // Compruebo si ya lo estan conquistando
                if (conquistadores.containsKey(zona)) {
                    pl.sendMessage(EpicCastle.PLUGIN + Lang.CASTLE_BEING_TAKEN.getText());
                    return;
                }

                Faction faction = FactionColl.get().getByName(fac);          
                MPlayer uplayer = MPlayer.get(pl);
                Faction other = uplayer.getFaction();
                
                /*
                  Primero compruebo si el jugador atacante tiene faccion, en caso 
                  contrario no puede atacar, despues si la faccion a la que ataca
                  existe o el castillo esta vacio para que la conquista
                  sea instantanea, si no se da el caso compruebo si el jugador que
                  ataca ya tiene el castillo en poder de su faccion, si no se da 
                  el caso compruebo si la faccion que ataca es aliada de la que 
                  posee el castillo y si no se da nada de lo anterior pasa a la
                  fase de ataque
                */
                
                if (other.isNone()) {
                    pl.sendMessage(EpicCastle.PLUGIN + Lang.SIGN_ERROR_NO_FACTION.getText());
                    return;
                }

                if (faction == null) {
                    pr.setLine(OWNER, other.getName());
                    pr.update(); // Para que se actualize
                    plugin.getManager().cargaCarteles();
                    
                    // Recompensa extra de power                    
                    double cant = other.getPowerBoost() + plugin.getManager().power;                    
                    other.setPowerBoost(cant);                    
                    other.sendMessage(EpicCastle.PLUGIN + Lang.FACTION_POWER_REWARD.getText()
                            .replace("%CANT%", String.valueOf(plugin.getManager().power)));                    
                    
                    Bukkit.getServer().broadcastMessage(EpicCastle.PLUGIN + 
                            Lang.CASTLE_TAKEN.getText()
                                    .replace("%PLAYER%", pl.getDisplayName())
                                    .replace("%CASTLE%", cambiaString(zona, true))
                                    .replace("%FACTION%", other.getName()));
                    return;
                }

                if (faction.equals(other)) {
                    return;
                }

                if (other.getRelationWish(faction) == Rel.ALLY) {
                    pl.sendMessage(EpicCastle.PLUGIN + Lang.SIGN_ERROR_ALLY.getText());
                    return;
                }     

                // Compruebo si alguno tiene el permiso para reducir el tiempo
                for (MPlayer player : other.getMPlayers()) {
                    if (player.getPlayer().hasPermission("ec.conquer")) {
                        tiempo = tiempo/2;
                        break;
                    }
                }
                
                // Aviso de conquista
                faction.sendMessage(EpicCastle.PLUGIN + Lang.CASTLE_YOURS_BEING_TAKEN.getText()
                        .replace("%FACTION%", other.getName())
                        .replace("%CASTLE%", cambiaString(zona, true)));
                pl.sendMessage(EpicCastle.PLUGIN + Lang.CASTLE_DELAY_TAKEN.getText().replace("%SEC%", String.valueOf(tiempo)));
                
                // Guardo como metadato el tiempo
                pl.setMetadata("tiempo", new FixedMetadataValue(plugin, tiempo));

                // Guardo el conquistador
                conquistadores.put(zona, pl);                
                
                // Tarea para contar el tiempo de conquista
                countdown = Bukkit.getServer().getScheduler().runTaskTimer (
                        plugin, new RunnableImpl(pl, pr, zona, faction, other, pl.getLevel()), 0, 20);
                
                conquista.put(pl, countdown);
            }
        }
    }
    
    @EventHandler
    public void signBroken(BlockBreakEvent e) {
        Sign[] carteles = plugin.getManager().carteles;        
        
        for (int i = 0; i < carteles.length; i++) {
            if (carteles[i] != null && carteles[i].equals(e.getBlock().getState())) {                
                // Si no tiene permiso no puede borrarlo
                if (!e.getPlayer().hasPermission("ec.sign")) {
                    e.getPlayer().sendMessage(EpicCastle.PLUGIN + Lang.PLUGIN_NO_PERMISSION.getText());
                    e.setCancelled(true);
                    return;
                }
                
                String zona = plugin.getManager().turno(i).toLowerCase();                 
                String nFac = plugin.getManager().valorSign(i, OWNER);
                
                // Compruebo si lo estan conquistando
                if (conquistadores.containsKey(zona)) {
                    conquista.get(conquistadores.get(zona)).cancel();
                    conquista.remove(conquistadores.get(zona));
                    conquistadores.remove(zona);
                }
                
                // Compruebo si el cartel ha sido tomado por una faccion
                if (nFac != null) {
                    Faction faction = FactionColl.get().getByName(nFac);
                    faction.sendMessage(EpicCastle.PLUGIN + 
                            Lang.CASTLE_DESTROY.getText().replace("%CASTLE%", cambiaString(zona, true)));
                    faction.setPowerBoost(faction.getPowerBoost() - plugin.getManager().power);
                }                                
                
                plugin.getManager().borrarCartel(cambiaString(zona, false));
                plugin.getManager().cargaCarteles();
                e.getPlayer().sendMessage(EpicCastle.PLUGIN + Lang.SIGN_DELETED.getText()
                        .replace("%TYPE%", cambiaString(zona, true)));
                break;
            }
        }
    }
    
    /**
     * Metodo para cambiar el <i>nombre por defecto</i> del castillo por uno mas
     * <i>agradable</i>
     * @param c Nombre por defecto
     * @param tipo Tipo de cambio
     * @return Nombre adecuado
     */
    public String cambiaString(String c, boolean tipo) {
        if (tipo) {
            return plugin.getManager().cambiaNombre(c);
        }
        else {
            switch (c) {
                case "castlexp":
                    return "castleXP";
                case "castlegold":
                    return "castleGold";
                case "castlediamond":
                    return "castleDiamond";
                case "castlemoney":
                    return "castleMoney";
                default:
                    return null;
            } 
        }
    }  
    
    public int compTipo(String n) {
        switch (n) {
            case "castlexp":
                return CASTLEXP;
            case "castlegold":
                return CASTLEGOLD;
            case "castlediamond":
                return CASTLEDIAMOND;
            case "castlemoney":
                return CASTLEMONEY;
            default:
                return -1;
        }
    }

    private class RunnableImpl implements Runnable {
        
        private final Player pl;
        private final Sign pr;
        private final String tipo;
        private final Faction castle;
        private final Faction other;
        private final int exp;

        public RunnableImpl(Player pl, Sign pr, String tipo, Faction castle, Faction other, int exp) {
            this.pl = pl;
            this.pr = pr;
            this.tipo = tipo;
            this.castle = castle;
            this.other = other;
            this.exp = exp;
        }

        @Override
        public void run() {
            List<MetadataValue> values = pl.getMetadata("tiempo");
            int t = 0;
            
            // Consigo el metadato
            for(MetadataValue value : values){
                if(value.getOwningPlugin().getDescription().getName().equals(plugin.getDescription().getName())){
                    t = value.asInt();
                    break;
                }
            }
            
            /*
            Voy estableciendo el tiempo como experiencia para hacerlo mas vistoso
            */
            if (t > 0 && t <= 5) {
                pl.setLevel(t);
                pl.playSound(pl.getLocation(), Sound.ANVIL_LAND, 1, 1);
                pl.setMetadata("tiempo", new FixedMetadataValue(plugin, t-1));
                return;
            }
            else if (t > 0) {
                pl.setLevel(t);
                pl.setMetadata("tiempo", new FixedMetadataValue(plugin, t-1));
                return;
            }
            
            pl.playSound(pl.getLocation(), Sound.EXPLODE, 1, 1);            
            
            if (pl.isOnline() && new Area(pr.getLocation(), 10).contains(pl.getLocation())) { // Caso positivo
                pr.setLine(OWNER, other.getName());
                pr.update();
                plugin.getManager().cargaCarteles();

                Bukkit.getServer().broadcastMessage(EpicCastle.PLUGIN +
                        Lang.CASTLE_TAKEN.getText()
                                .replace("%PLAYER%", pl.getDisplayName())
                                .replace("%CASTLE%", cambiaString(tipo, true))
                                .replace("%FACTION%", other.getName()));
                pl.setLevel(exp);

                // Recompensa extra de power
                double cant1, cant2;

                cant1 = castle.getPowerBoost() - plugin.getManager().power;
                cant2 = other.getPowerBoost() + plugin.getManager().power;

                castle.setPowerBoost(cant1);
                other.setPowerBoost(cant2);                    

                castle.sendMessage(EpicCastle.PLUGIN + Lang.FACTION_POWER_LOST.getText()
                        .replace("%CANT%", String.valueOf(plugin.getManager().power)));
                other.sendMessage(EpicCastle.PLUGIN + Lang.FACTION_POWER_REWARD.getText()
                        .replace("%CANT%", String.valueOf(plugin.getManager().power)));

                // Elimino el aviso del jugador que conquisto
                FactionMessageCastle.getAtacantes().remove(pl);                                      
                
                conquista.get(pl).cancel();
                conquista.remove(pl);
                conquistadores.remove(tipo);
            }            
            else {
                // Caso negativo
                pl.sendMessage(EpicCastle.PLUGIN + Lang.CASTLE_NO_TAKEN.getText());
                pl.setLevel(exp);
                conquista.get(pl).cancel();
                conquista.remove(pl);
                conquistadores.remove(tipo);
            }
        }
    }
    
    @Override
    public void execute(Listener ll, Event event) throws EventException {
    } 
}
