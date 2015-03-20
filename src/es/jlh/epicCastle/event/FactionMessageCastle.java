package es.jlh.epicCastle.event;

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
import es.jlh.epicCastle.utils.Lang;
import java.util.HashMap;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.EventExecutor;

/**
 * Clase evento para comprobar los jugadores que entran y salen de los castillos
 * @author Julián
 * @version 1.0
 */
public class FactionMessageCastle implements Listener, EventExecutor {

    private final EpicCastle plugin;    
    private final WorldGuardPlugin wgplugin;
    private final HashMap<String, String> owner = new HashMap(); 
    
    private static final HashMap<Player, String> atacantes = new HashMap();
    
    public FactionMessageCastle(EpicCastle plugin) {
        this.plugin = plugin;
        this.wgplugin = this.plugin.getWorldguard();               
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player pl = e.getPlayer();        

        owner.clear();
        owner.put("castleXP", plugin.getManager().valorSign(CASTLEXP, OWNER));
        owner.put("castleGold", plugin.getManager().valorSign(CASTLEGOLD, OWNER));
        owner.put("castleMoney", plugin.getManager().valorSign(CASTLEDIAMOND, OWNER));
        owner.put("castleDiamond", plugin.getManager().valorSign(CASTLEMONEY, OWNER));        
        
        // Objeto para obtener las zonas del world guard de un mundo
        RegionManager regionManager = wgplugin.getRegionManager(pl.getWorld());       
        
        // Vector de la posicion del jugador
        BlockVector pos = new BlockVector(new Vector (
                pl.getLocation().getBlockX(), 
                pl.getLocation().getBlockY(), 
                pl.getLocation().getBlockZ()));

        // Si el jugador ya ha entrado en la zona comprobamos si ha salido
        if (!atacantes.isEmpty() && atacantes.containsKey(pl)) {            
            boolean vip = false;
            
            ProtectedRegion all = regionManager.getRegion(atacantes.get(pl));
            
            if (!all.contains(pos)) {                                
                String ow = this.owner.get(atacantes.get(pl));
                
                // Si se ha borrado el castillo no tiene dueno
                if (ow == null) {
                    atacantes.remove(pl, atacantes.get(pl));
                    return;
                }
                
                Faction faction = FactionColl.get().getByName(ow);                

                MPlayer uplayer = MPlayer.get(pl);
                Faction other = uplayer.getFaction();                
                
                // Si ha borrado su faccion despues de entrar salimos
                if (faction == null) {
                    atacantes.remove(pl, atacantes.get(pl));
                    return;
                }
                
                // Compruebo si alguno de los miembros de la faccion tiene el permiso vip
                for (Player p : faction.getOnlinePlayers()) {                    
                    if (p.hasPermission("ec.reports")) {
                        vip = true;
                        break;
                    }
                }        
                
                // Modulo VIP / user normal
                if (vip && other == null) {
                    faction.sendMessage(EpicCastle.PLUGIN + Lang.CASTLE_NOFACTION_LEAVE.getText()
                                .replace("%CASTLE%", cambiaNombre(atacantes.get(pl)))
                                .replace("%PLAYER%", pl.getDisplayName()));
                }
                else if (vip) {                
                    if (other.getRelationWish(faction) == Rel.ALLY) {
                        faction.sendMessage(EpicCastle.PLUGIN + Lang.CASTLE_ALLY_LEAVE.getText()
                                .replace("%CASTLE%", cambiaNombre(atacantes.get(pl)))
                                .replace("%PLAYER%", pl.getDisplayName()));
                    }
                    else if (other.getRelationWish(faction) == Rel.NEUTRAL || 
                            other.getRelationWish(faction) == Rel.TRUCE) {
                        faction.sendMessage(EpicCastle.PLUGIN + Lang.CASTLE_NEUTRAL_LEAVE.getText()
                                .replace("%CASTLE%", cambiaNombre(atacantes.get(pl)))
                                .replace("%PLAYER%", pl.getDisplayName()));
                    }
                    else {
                        faction.sendMessage(EpicCastle.PLUGIN + Lang.CASTLE_ENEMY_LEAVE.getText()
                                .replace("%CASTLE%", cambiaNombre(atacantes.get(pl)))
                                .replace("%PLAYER%", pl.getDisplayName()));
                    }                    
                }  
                else {
                    faction.sendMessage(EpicCastle.PLUGIN + Lang.CASTLE_SOMEONE_LEAVE.getText()
                            .replace("%CASTLE%", cambiaNombre(atacantes.get(pl))));
                }
                
                atacantes.remove(pl, atacantes.get(pl));                
            }
            
            return;
        }

        // En caso de que el jugador sea la primera vez que entra empieza aqui
        ProtectedRegion xp = regionManager.getRegion("castleXP");
        ProtectedRegion gold = regionManager.getRegion("castleGold");
        ProtectedRegion diamond = regionManager.getRegion("castleDiamond");
        ProtectedRegion money = regionManager.getRegion("castleMoney");
        
        if (xp != null && xp.contains(pos)) {            
            String ow = this.owner.get("castleXP");
            compruebaFac(ow, pl, "castleXP");
        }
        else if (gold != null && gold.contains(pos)) {
            String ow = this.owner.get("castleGold");
            compruebaFac(ow, pl, "castleGold");
        }
        else if (diamond != null && diamond.contains(pos)) {
            String ow = this.owner.get("castleDiamond");
            compruebaFac(ow, pl, "castleDiamond");
        }
        else if (money != null && money.contains(pos)) {
            String ow = this.owner.get("castleMoney");
            compruebaFac(ow, pl, "castleMoney");
        }
    }
    
    /**
     * Método para comprobar si un jugador se encuentra en una zona
     * @param owner Nombre de la faccion
     * @param pl Objeto del jugador que se esta comprobando
     * @param tipo tipo de castillo
     */
    public void compruebaFac(String owner, Player pl, String tipo) {
        
        boolean vip = false;        
        
        if (owner == null || owner.compareTo("") == 0) {
            return;
        }

        Faction faction = FactionColl.get().getByName(owner);
        MPlayer uplayer = MPlayer.get(pl);
        Faction other = uplayer.getFaction();
        
        // Si el dueño de la zona ya no existe salgo
        if (faction == null) {
            return;
        }
        
        if (faction.getOnlinePlayers().contains(pl)) {
            return;
        }

        // Compruebo si alguno de los miembros de la faccion tiene el permiso vip
        for (Player p : faction.getOnlinePlayers()) {
            if (p.hasPermission("ec.reports")) {
                vip = true;
                break;
            }
        }

        // Modulo VIP / user normal
        if (vip) {
            if (vip && other == null) {
                    faction.sendMessage(EpicCastle.PLUGIN + Lang.CASTLE_NOFACTION_JOIN.getText()
                                .replace("%CASTLE%", cambiaNombre(atacantes.get(pl)))
                                .replace("%PLAYER%", pl.getDisplayName()));
            }
            else if (other.getRelationWish(faction) == Rel.ALLY) {
                faction.sendMessage(EpicCastle.PLUGIN + Lang.CASTLE_ALLY_JOIN.getText()
                        .replace("%CASTLE%", cambiaNombre(tipo))
                        .replace("%PLAYER%", pl.getDisplayName()));
            }
            else if (other.getRelationWish(faction) == Rel.NEUTRAL ||
                    other.getRelationWish(faction) == Rel.TRUCE) {
                faction.sendMessage(EpicCastle.PLUGIN + Lang.CASTLE_NEUTRAL_JOIN.getText()
                        .replace("%CASTLE%", cambiaNombre(tipo))
                        .replace("%PLAYER%", pl.getDisplayName()));
            }
            else {
                faction.sendMessage(EpicCastle.PLUGIN + Lang.CASTLE_ENEMY_JOIN.getText()
                        .replace("%CASTLE%", cambiaNombre(tipo))
                        .replace("%PLAYER%", pl.getDisplayName()));
            }            
        }
        else {
            faction.sendMessage(EpicCastle.PLUGIN + Lang.CASTLE_SOMEONE_JOIN.getText()
                    .replace("%CASTLE%", cambiaNombre(tipo)));               
        }

        atacantes.put(pl, tipo);
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
                return "Castillo XP";
            case "castleGold":
                return "Castillo Oro";
            case "castleDiamond":
                return "Castillo Diamante";
            case "castleMoney":
                return "Castillo Dinero";
            default:
                return null;
        }
    }

    public static HashMap<Player, String> getAtacantes() {
        return atacantes;
    }
    
    @Override
    public void execute(Listener ll, Event event)
            throws EventException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
