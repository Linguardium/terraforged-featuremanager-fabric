/*
 *
 * MIT License
 *
 * Copyright (c) 2020 TerraForged
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.terraforged.feature;

import com.terraforged.feature.biome.BiomeFeature;
import com.terraforged.feature.biome.BiomeFeatures;
import com.terraforged.feature.modifier.FeatureModifierLoader;
import com.terraforged.feature.modifier.FeatureModifiers;
import com.terraforged.feature.template.TemplateManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.util.HashMap;
import java.util.Map;

public class FeatureManager implements FeatureDecorator {

    public static final Logger LOG = LogManager.getLogger("FeatureManager");
    public static final Marker INIT = MarkerManager.getMarker("INIT");

    private final Map<Biome, BiomeFeatures> biomes;

    public FeatureManager(Map<Biome, BiomeFeatures> biomes) {
        this.biomes = biomes;
    }

    @Override
    public FeatureManager getFeatureManager() {
        return this;
    }

    public BiomeFeatures getFeatures(Biome biome) {
        return biomes.getOrDefault(biome, BiomeFeatures.NONE);
    }

    public static FeatureManager create(IWorld world) {
        FeatureModifiers modifiers = FeatureModifierLoader.load();
        return create(world, modifiers);
    }

    public static FeatureManager create(IWorld world, FeatureModifiers modifiers) {
        LOG.debug(INIT, "Initializing FeatureManager");
        int predicates = modifiers.getPredicates().size();
        int replacers = modifiers.getReplacers().size();
        int transformers = modifiers.getTransformers().size();
        LOG.debug(INIT, " Predicates: {}, Replacers: {}, Transformers: {}", predicates, replacers, transformers);

        modifiers.sort();

        LOG.debug(INIT, " Compiling biome feature lists");
        Map<Biome, BiomeFeatures> biomes = new HashMap<>();
        for (Biome biome : Registry.BIOME) {
            BiomeFeatures features = compute(biome, modifiers);
            biomes.put(biome, features);
        }

        LOG.debug(INIT, " Initialization complete");
        return new FeatureManager(biomes);
    }

    public static void registerTemplates() {
        TemplateManager.register();
    }

    private static BiomeFeatures compute(Biome biome, FeatureModifiers modifiers) {
        BiomeFeatures.Builder builder = BiomeFeatures.builder();
        for (GenerationStep.Feature stage : GenerationStep.Feature.values()) {
            for (ConfiguredFeature<?, ?> feature : biome.getFeaturesForStep(stage)) {
                BiomeFeature biomeFeature = modifiers.getFeature(biome, feature);
                builder.add(stage, biomeFeature);
            }
        }
        return builder.build();
    }
}
