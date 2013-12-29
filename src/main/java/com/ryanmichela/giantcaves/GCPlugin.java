//    Copyright (C) 2011  Ryan Michela
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.

package com.ryanmichela.giantcaves;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 */
public class GCPlugin extends JavaPlugin {

    public void onEnable() {
        try {
            Metrics metrics = new Metrics(this);
            metrics.start();
        } catch (IOException e) {
            // Failed to submit the stats :-(
        }

        // create the plugin directory if it does not exist
        this.saveDefaultConfig();

        // attach to worlds automatically when onlyUseWorldManagers is false
        if (!getConfig().getBoolean("onlyUseWorldManagers", false)) {
            getServer().getPluginManager().registerEvents(new GCWorldListener(), this);
        }
    }

    public void onDisable() {
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        if (id == null || id == "") {
            id = "sxz=200,sy=100,cutoff=62,miny=6,maxy=50";
        }
        return new GCChunkGenerator(this, id);
    }

    private class GCWorldListener implements Listener {
        @EventHandler
        public void onWorldInit(WorldInitEvent event) {
            Config config = parseConfig(event.getWorld());
            if(config != null) {
                getLogger().info("Attaching cave populator to world \"" + event.getWorld().getName() + "\"");
                event.getWorld().getPopulators().add(new GiantCavePopulator(GCPlugin.this, config));
            }
        }
    }

    private Config parseConfig(World bukkitWorld) {
        List<Map<?, ?>> worlds = getConfig().getMapList("worlds");
        for(Map<?, ?> worldConfig : worlds) {
            if(worldConfig.get("name").equals(bukkitWorld.getName())) {
                return new Config((Map<String, Object>)worldConfig);
            }
        }
        return null;
    }
}
