package com.ryanmichela.giantcaves;

import net.minecraft.server.v1_6_R3.World;
import org.bukkit.craftbukkit.v1_6_R3.CraftWorld;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;

import java.util.*;

/**
 * Copyright 2013 Ryan Michela
 */
public class GCChunkGenerator extends ChunkGenerator {
    private Plugin plugin;
    private String caveSettings;
    private GCChunkProviderGenerate provider = null;

    public GCChunkGenerator(Plugin plugin, String caveSettings) {
        this.plugin = plugin;
        this.caveSettings = caveSettings;
    }

    @Override
    public byte[] generate(org.bukkit.World world, Random random, int x, int z) {
        GCChunkProviderGenerate chunkProvider = lazyGetProvider(world);
        return chunkProvider.getChunkSectionsAt(x, z);
    }

    @Override
    public boolean canSpawn(org.bukkit.World world, int x, int z) {
        return ((CraftWorld) world).getHandle().worldProvider.canSpawn(x, z);
    }

    @Override
    public List<BlockPopulator> getDefaultPopulators(org.bukkit.World world) {
        ArrayList<BlockPopulator> populators = new ArrayList<BlockPopulator>();
        populators.add(new GCBlockPopulator());

        plugin.getLogger().info("Adding Giant Caves to world '" + world.getName() + "' with settings " + caveSettings);
        Config caveConfig = parseCaveConfig(caveSettings);
        populators.add(new GiantCavePopulator(plugin, caveConfig));

        return populators;
    }

    private GCChunkProviderGenerate lazyGetProvider(org.bukkit.World bukkitWorld)
    {
        if (provider == null) {
            World world = ((CraftWorld)bukkitWorld).getHandle();
            provider = new GCChunkProviderGenerate(world, world.getSeed(), world.getWorldData().shouldGenerateMapFeatures());
        }
        return provider;
    }

    private Config parseCaveConfig(String caveSettings) {
        Map<String, Object> kv = new HashMap<String, Object>();
        for(String setting : caveSettings.split(",")) {
            String[] splits = setting.split("=");
            kv.put(splits[0], splits[1]);
        }
        return new Config(kv);
    }

    private class GCBlockPopulator extends BlockPopulator{
        @Override
        public void populate(org.bukkit.World world, Random random, org.bukkit.Chunk chunk) {
            GCChunkProviderGenerate chunkProvider = lazyGetProvider(world);
            chunkProvider.getChunkAt(null, chunk.getX(), chunk.getZ());
        }
    }
}
