package com.ryanmichela.giantcaves;

import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Event;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 */
public class GCPlugin extends JavaPlugin {

    public void onEnable() {
        getServer().getLogger().info("[Giant Caves] Started.");
        getServer().getPluginManager().registerEvent(Event.Type.WORLD_INIT, new GCWorldListener(), Event.Priority.Normal, this);
    }

    public void onDisable() {
    }

    private class GCWorldListener extends WorldListener {
        @Override
        public void onWorldInit(WorldInitEvent event) {
            Config config = parseConfig(event.getWorld());
            if(config != null) {
                getServer().getLogger().info("[Giant Caves] Attaching cave populator to world \"" + event.getWorld().getName() + "\"");
                event.getWorld().getPopulators().add(new GiantCavePopulator(config));
            }
        }
    }

    private Config parseConfig(World bukkitWorld) {
        // create the plugin directory if it does not exist
        try
        {
            File configFile = new File(getDataFolder(), "config.yml");
            if(!configFile.exists()){
                configFile.getParentFile().mkdirs();
                copy(getResource("config.yml"), configFile);
            }
        }
        catch(IOException ex)
        {
            getConfig().options().copyDefaults(true);
            getServer().getLogger().log(Level.SEVERE, "[Giant Caves] Failed to initialize configuration! Falling back to defaults.", ex);
        }


        List<Map<String, Object>> worlds = getConfig().getMapList("worlds");
        for(Map<String, Object> worldConfig : worlds) {
            if(worldConfig.get("name").equals(bukkitWorld.getName())) {
                return new Config(worldConfig);
            }
        }
        return null;
    }

    private void copy(InputStream in, File file) throws IOException {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while((len=in.read(buf))>0) {
                out.write(buf,0,len);
            }
            out.close();
            in.close();
    }

}
