package es.jlh.epicCastle.utils;

import java.io.File;
import java.io.IOException;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

public enum Lang {
    PLUGIN_ENABLED("Plugin activado correctamente"),
    PLUGIN_DISABLED("Plugin desactivado correctamente"),
    PLUGIN_NO_PERMISSION("&4No tienes permiso para hacer eso"),
    PLUGIN_RELOAD("&6Plugin recargado correctamente"),
    COMMAND_ARGUMENTS("&4Te faltan/sobran argumentos!"),
    CANCEL_TELEPORT_PLAYER("&cSe ha cancelado la peticion de teleport"),
    DELAY_TELEPORT_PLAYER("&bTienes que esperar %TIME% segundos"),
    CASTLE_TYPE_XP("Castillo XP"),
    CASTLE_TYPE_GOLD("Castillo Oro"),
    CASTLE_TYPE_MONEY("Castillo Dinero"),
    CASTLE_TYPE_DIAMOND("Castillo Diamante"),
    CASTLE_TELEPORT_PLAYER("&6Has sido teletransportado cerca del castillo"),    
    CASTLE_TELEPORT_REGISTER("&2Zona de teleport establecida correctamente"),
    CASTLE_WELCOME_MESSAGE("&fHas entrado en el %CASTLE% propiedad de %OWNER%"),
    CASTLE_LEAVE_MESSAGE("&fHas salido del %CASTLE% propiedad de %OWNER%"),    
    CASTLE_SOMEONE_JOIN("&fAlguien ha entrado en %CASTLE%"),
    CASTLE_SOMEONE_LEAVE("&fAlguien ha salido de %CASTLE%"),    
    CASTLE_NOFACTION_JOIN("&b[NF] &f%PLAYER% ha entrado en %CASTLE%"),
    CASTLE_NOFACTION_LEAVE("&b[NF] &f%PLAYER% ha salido de %CASTLE%"),    
    CASTLE_ENEMY_JOIN("&4[E] &f%PLAYER% ha entrado en %CASTLE%"),
    CASTLE_ENEMY_LEAVE("&4[E] &f%PLAYER% ha salido de %CASTLE%"),
    CASTLE_NEUTRAL_JOIN("&7[N] &f%PLAYER% ha entrado a %CASTLE%"),
    CASTLE_NEUTRAL_LEAVE("&7[N] &f%PLAYER% ha salido de %CASTLE%"),
    CASTLE_ALLY_JOIN("&5[A] &f%PLAYER% ha entrado a %CASTLE%"),
    CASTLE_ALLY_LEAVE("&5[A] &f%PLAYER% ha salido de %CASTLE%"),
    CASTLE_DELAY_TAKEN("&6Tienes que aguantar %SEC%s lo mas cerca del cartel para conquistarlo"),
    CASTLE_BEING_TAKEN("&4El castillo ya esta siendo tomado"),
    CASTLE_YOURS_BEING_TAKEN("&cLa faccion %FACTION% esta tomando vuestro %CASTLE%"),
    CASTLE_TAKEN("&a%PLAYER% &6ha tomado &b%CASTLE% &6para &e%FACTION%"),
    CASTLE_NO_TAKEN("&4No has aguantado en la zona de toma del castillo (Cerca del cartel)"),
    CASTLE_RELEASED("&4%CASTLE% ha sido liberado"),
    CASTLE_DESTROY("&6El %CASTLE% ha sido destruido y habeis perdido la bonificacion de power"),
    CASTLE_REWARD("&6Has recibido %CANT% %TYPE%"),
    FACTION_POWER_REWARD("&6Habeis recibido %CANT% de power extra por conquistar el castillo!!!"),
    FACTION_POWER_LOST("&6Habeis perdido %CANT% de power extra por perder el castillo D:"),
    SIGN_CREATED("&a%TYPE% creado correctamente"),
    SIGN_DELETED("&a%TYPE% borrado correctamente"),
    SIGN_LOADS("&e%CANT% carteles cargados correctamente"),
    SIGN_ERROR_NOT_EXISTS("&4El cartel de %TYPE% no existe o esta mal definido"),
    SIGN_ERROR_NO_FACTION("&4Necesitas una faccion para poder conquistar"),
    SIGN_ERROR_ALLY("&4No puedes robarle el castillo a una faccion aliada"),
    SIGN_ERROR_ZONE("&4Crealo en una de estas zonas: castleXP, castleGold, castleDiamond o castleMoney"),
    SIGN_ERROR_TIME("&4El tiempo tiene que ser igual o mayor que cero"),
    SIGN_ERROR_CREATE("&4%CASTLE% ya esta creado");

    private final String value;
    public static YamlConfiguration lang = null;
    public static File langFile = new File("plugins/EpicCastle/messages.yml");
    public static File backupFile = new File("plugins/PvpTitles/messages.backup.yml");    

    private Lang(final String value) {
        this.value = value;
    }

    public String getText() {
        String valor = this.getValue();
        if (lang != null && lang.contains(this.name())) {
                valor = lang.getString(this.name());
        }
        valor = ChatColor.translateAlternateColorCodes('&', valor);
        return valor;
    }

    public String getValue() {
        return this.value;
    }

    public static void load() throws Exception {
        if (!langFile.exists()) {
            createConfig();
        }
        
        lang = YamlConfiguration.loadConfiguration(langFile);
        
        if (!compLocales()) {
            throw new Exception("Error loading locales, a new one has been created.");
        }
    }

    private static boolean compLocales() {
        // Compruebo si esta completo
        for (Lang idioma : Lang.values()) {
            if (!lang.contains(idioma.name())) {                
                try {
                    lang.save(backupFile); // Guardo una copia de seguridad                    
                    createConfig();
                    lang = YamlConfiguration.loadConfiguration(langFile);
                    return false;
                } 
                catch (IOException ex) {}
            }
        }
        return true;
    }    
    
    public static void createConfig() {
        YamlConfiguration newConfig = new YamlConfiguration();
        
        newConfig.options().header (
            "#########################################\n" + 
            "## [LOCALES] No edites las %variables% ##\n" +
            "#########################################"
        );
        newConfig.options().copyHeader(true);
        
        for (Lang idioma : Lang.values())
        {
            String name = idioma.name();
            String value = idioma.getValue();
            newConfig.set(name, value);
        }
        
        try {
            newConfig.save(langFile);
        } 
        catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
