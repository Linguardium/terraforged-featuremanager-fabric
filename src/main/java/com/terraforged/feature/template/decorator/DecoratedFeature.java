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

package com.terraforged.feature.template.decorator;

import com.terraforged.feature.template.type.FeatureType;
import com.terraforged.feature.template.type.TypedFeature;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorConfig;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;

import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class DecoratedFeature<T extends Feature<DefaultFeatureConfig> & TypedFeature, W extends DecoratorWorld> extends Feature<DefaultFeatureConfig>
        implements TypedFeature {

    private final T feature;
    private final List<Decorator<W>> decorators;
    private final Function<IWorld, W> worldFactory;

    public DecoratedFeature(T feature, List<Decorator<W>> decorators, Function<IWorld, W> factory) {
        super(DefaultFeatureConfig::deserialize);
        this.worldFactory = factory;
        this.feature = feature;
        this.decorators = decorators;
    }

    public T getFeature() {
        return feature;
    }

    public List<Decorator<W>> getDecorators() {
        return decorators;
    }

    public W wrap(IWorld world) {
        return worldFactory.apply(world);
    }

    @Override
    public FeatureType getType() {
        return feature.getType();
    }

    @Override
    public boolean generate(IWorld world, ChunkGenerator<? extends ChunkGeneratorConfig> generator, Random random, BlockPos pos,
            DefaultFeatureConfig config) {
        W featureWorld = worldFactory.apply(world);
        if (placeFeature(featureWorld, generator, random, pos, config)) {
            decorate(featureWorld, random);
            return true;
        }
        return false;
    }

    public boolean placeFeature(W world, ChunkGenerator<? extends ChunkGeneratorConfig> generator, Random rand, BlockPos pos,
            DefaultFeatureConfig config) {
        return feature.generate(world, generator, rand, pos, config);
    }

    public void decorate(W world, Random random) {
        for (Decorator<W> decorator : decorators) {
            decorator.apply(world, random);
        }
    }
}
