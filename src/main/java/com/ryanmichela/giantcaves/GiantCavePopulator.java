//Copyright (C) 2011  Ryan Michela
//
//This program is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with this program.  If not, see <http://www.gnu.org/licenses/>.
package com.ryanmichela.giantcaves;

import net.minecraft.server.v1_6_R3.*;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_6_R3.CraftChunk;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.plugin.Plugin;

import java.util.Random;

public class GiantCavePopulator extends BlockPopulator {

    public final Plugin plugin;
    private final Config config;

    // Material
    private final Material material;

    public GiantCavePopulator(Plugin plugin, Config config) {
        this.plugin = plugin;
        this.config = config;
        material = config.debugMode ? Material.STONE : Material.AIR; // Stone in debug, air in release
        plugin.getServer().getPluginManager().registerEvents(new GCWaterHandler(config), plugin);
    }

    @Override
    public void populate(final World world, final Random random, final Chunk source) {
        net.minecraft.server.v1_6_R3.Chunk nmsChunk = ((CraftChunk) source).getHandle();
        ChunkSection[] chunkSections = nmsChunk.i();

        GCRandom gcRandom = new GCRandom(source, config);

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = config.caveBandMax; y >= config.caveBandMin; y--) {
                    if (gcRandom.isInGiantCave(x, y, z)) {
                        // See NMS.Chunk.a() line 368-375
                        ChunkSection cs = chunkSections[y >> 4];
                        if (cs == null) {
                            cs = chunkSections[y >> 4] = new ChunkSection(y >> 4 << 4, !nmsChunk.world.worldProvider.f);
                        }

                        // Create the cave by the block at this coordinate
                        cs.setTypeId(x, y & 15, z, material.getId());

                        // Strip out any TileEntity that may remain
                        nmsChunk.f(x, y, z);
                    }
                }
            }
        }
    }
}
