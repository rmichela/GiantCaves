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

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.plugin.Plugin;

import java.util.Random;

public class GiantCavePopulator extends BlockPopulator {

    private final BlockFace[] faces = { BlockFace.UP, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST };

    private final Config config;
    private final Plugin plugin;

    // Material
    private final Material material;
    private final BlockToucher toucher;

    public GiantCavePopulator(Plugin plugin, Config config) {
        this.config = config;
        this.plugin = plugin;
        material = Material.AIR;
        toucher = new BlockToucher(plugin);
    }

    @Override
    public void populate(final World world, final Random random, final Chunk source) {
        GCRandom gcRandom = new GCRandom(source, config);

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = config.caveBandMax; y >= config.caveBandMin; y--) {
                    if (gcRandom.isInGiantCave(x, y, z)) {
                        Block block = source.getBlock(x, y, z);
                        Block blockUp1 = block.getRelative(BlockFace.UP);
                        Block blockUp2 = blockUp1.getRelative(BlockFace.UP);
                        Block blockUp3 = blockUp2.getRelative(BlockFace.UP);
                        if (isHoldingBackOcean(block) || isHoldingBackOcean(blockUp1)) {
                            continue;
                        } else if (isHoldingBackOcean(blockUp2) || isHoldingBackOcean(blockUp3)) {
                            // Support the ocean with stone to keep the bottom from falling out
                            if (block.getType().hasGravity()) { // sand or gravel
                                block.setType(Material.STONE);
                                blockUp1.setType(Material.STONE);
                            }
                        } else {
                            block.setType(material);

                            // Mark adjacent blocks for update, iff they are not in the cave
                            for (BlockFace direction : faces) {
                                Block b = block.getRelative(direction);
                                if (isWater(b) || isLava(b)) {
                                    if (!gcRandom.isInGiantCave(b.getX(), b.getY(), b.getZ())) {
                                        toucher.touch(b);
                                    }
                                }
                            }

                            if (config.debugMode) {
                                block = source.getBlock(x, 192, z);
                                block.setType(Material.GLASS);
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isHoldingBackOcean(Block block) {
        return isSurfaceWater(block) || isNextToSurfaceWater(block);
    }

    private boolean isNextToSurfaceWater(Block block) {
        for (BlockFace face : faces) {
            Block adjacent = block.getRelative(face);
            // Don't look at neighboring chunks to prevent runaway chunk generation
            if (block.getChunk() == adjacent.getChunk()) {
                if (isSurfaceWater(adjacent)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isSurfaceWater(Block block) {
        // Walk the column of blocks above block looking sea level
        while (isWater(block)) {
            if (block.getY() >= block.getWorld().getSeaLevel() - 1) {
                return true;
            } else {
                block = block.getRelative(BlockFace.UP);
            }
        }
        return false;
    }

    private boolean isWater(Block block) {
        Material material = block.getType();
        return material == Material.WATER;
    }

    private boolean isLava(Block block) {
        Material material = block.getType();
        return material == Material.LAVA;
    }
}
