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

import net.minecraft.server.v1_6_R2.ChunkSection;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_6_R2.CraftChunk;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.noise.*;

import java.util.Random;

/**
 */
public class GiantCavePopulator extends BlockPopulator {

    public Plugin plugin;
    private Config config;
// Note: Smaller frequencies yield slower change (more stretched out)
//   Larger amplitudes yield greater influence on final void
// Frequency
    private final double f1xz;
    private final double f1y;
// Density
    private final int amplitude1 = 100;
    private final double subtractForLessThanCutoff;
// Second pass - small noise
    private final double f2xz = 0.25;
    private final double f2y = 0.05;
    private final int amplitude2 = 2;
// Third pass - vertical noise
    private final double f3xz = 0.025;
    private final double f3y = 0.005;
    private final int amplitude3 = 20;
// Position
    private final int caveBandBuffer;
// Material
    private final byte materialId;

    public GiantCavePopulator(Plugin plugin, Config config) {
        this.plugin = plugin;
        this.config = config;
        subtractForLessThanCutoff = amplitude1 - config.cutoff;
        materialId = (byte) (config.debugMode ? Material.STONE.getId() : Material.AIR.getId()); // Stone in debug, air in release
        f1xz = 1.0 / config.sxz;
        f1y = 1.0 / config.sy;
        if (config.caveBandMax - config.caveBandMin > 128) {
            caveBandBuffer = 32;
        } else {
            caveBandBuffer = 16;
        }
    }

    @Override
    public void populate(final World world, final Random random, final Chunk source) {
        net.minecraft.server.v1_6_R2.Chunk nmsChunk = ((CraftChunk) source).getHandle();
        ChunkSection[] chunkSections = nmsChunk.i();
        boolean flag = false;

        NoiseGenerator noiseGen1 = new SimplexNoiseGenerator(world);
        NoiseGenerator noiseGen2 = new SimplexNoiseGenerator((long) noiseGen1.noise(source.getX(), source.getZ()));
        NoiseGenerator noiseGen3 = new SimplexNoiseGenerator((long) noiseGen1.noise(source.getX(), source.getZ()));

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = config.caveBandMax; y >= config.caveBandMin; y--) {
                    double xx = (source.getX() << 4) | (x & 0xF);
                    double yy = y;
                    double zz = (source.getZ() << 4) | (z & 0xF);
                    if ((noiseGen1.noise(xx * f1xz, yy * f1y, zz * f1xz) * amplitude1)
                            + (noiseGen2.noise(xx * f2xz, yy * f2y, zz * f2xz) * amplitude2)
                            - (noiseGen3.noise(xx * f3xz, yy * f3y, zz * f3xz) * amplitude3)
                            - linearCutoffCoefficient(y) > config.cutoff) {

                        // See NMS.Chunk.a() line 368-375
                        ChunkSection cs = chunkSections[y >> 4];
                        if (cs == null) {
                            cs = chunkSections[y >> 4] = new ChunkSection(y >> 4 << 4, !nmsChunk.world.worldProvider.f);
                            flag = true;
                        }

                        // Set the target block to materialId.
                        int idAbove = 0;
                        if (y < 254) {
                            idAbove = nmsChunk.getTypeId(x, y + 1, z);
                        }

                        cs.setTypeId(x, y & 15, z, materialId);
                        if (idAbove == Material.STATIONARY_WATER.getId() || idAbove == Material.WATER.getId()) { // Should we be water.
                            cs.setTypeId(x, y & 15, z, Material.WATER.getId());
                        } else if (idAbove == Material.STATIONARY_LAVA.getId() || idAbove == Material.LAVA.getId()) { // Should we be lava.
                            cs.setTypeId(x, y & 15, z, Material.LAVA.getId());
                        /*} else {
                            cs.setTypeId(x, y & 15, z, materialId);*/
                        }

                        // Strip out any TileEntity that may remain
                        nmsChunk.f(x, y, z);
                    }
                }
            }
        }

        if (flag) {
            nmsChunk.initLighting();
        }
    }

    private double linearCutoffCoefficient(int y) {
        // Out of bounds
        if (y < config.caveBandMin || y > config.caveBandMax) {
            return subtractForLessThanCutoff;
            // Bottom layer distortion
        } else if (y >= config.caveBandMin && y <= config.caveBandMin + caveBandBuffer) {
            double yy = y - config.caveBandMin;
            return (-subtractForLessThanCutoff / (double) caveBandBuffer) * yy + subtractForLessThanCutoff;
            // Top layer distortion
        } else if (y <= config.caveBandMax && y >= config.caveBandMax - caveBandBuffer) {
            double yy = y - config.caveBandMax + caveBandBuffer;
            return (subtractForLessThanCutoff / (double) caveBandBuffer) * yy;
            // In bounds, no distortion
        } else {
            return 0;
        }
    }
}
