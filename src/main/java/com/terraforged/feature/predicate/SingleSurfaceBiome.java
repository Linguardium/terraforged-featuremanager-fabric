package com.terraforged.feature.predicate;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeContainer;
import net.minecraft.world.chunk.IChunk;

public class SingleSurfaceBiome extends CachedPredicate {

    public static final FeaturePredicate INSTANCE = new SingleSurfaceBiome();

    @Override
    public boolean doTest(IChunk chunk, Biome biome) {
        BiomeContainer biomes = chunk.func_225549_i_();
        if (biomes == null) {
            return false;
        }
        for (int dz = 0; dz < 16; dz++) {
            for (int dx = 0; dx < 16; dx++) {
                if (biomes.func_225526_b_(dx, 70, dz) != biome) {
                    return false;
                }
            }
        }
        return true;
    }
}
