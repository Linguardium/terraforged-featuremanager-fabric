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

package com.terraforged.feature.template.decorator.tree;

import com.terraforged.feature.template.decorator.BoundsRecorder;
import com.terraforged.feature.template.decorator.DecoratorWorld;
import net.minecraft.block.BlockState;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.IWorld;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TreeBuffer extends BoundsRecorder implements DecoratorWorld {

    private Set<BlockPos> logs = null;
    private Set<BlockPos> leaves = null;

    private List<BlockPos> logList = null;
    private List<BlockPos> leafList = null;

    public TreeBuffer(IWorld delegate) {
        super(delegate);
    }

    @Override
    public void setDelegate(IWorld world) {
        super.setDelegate(world);
    }

    @Override
    public boolean setBlockState(BlockPos pos, BlockState state, int flags) {
        recordState(pos, state);
        return super.setBlockState(pos, state, flags);
    }

    @Override
    public void translate(BlockPos offset) {
        super.translate(offset);

        logList = getLogPositions().stream()
                .map(pos -> pos.add(offset))
                .sorted(Comparator.comparingInt(Vec3i::getY))
                .collect(Collectors.toList());

        leafList = getLeafPositions().stream()
                .map(pos -> pos.add(offset))
                .sorted(Comparator.comparingInt(Vec3i::getY))
                .collect(Collectors.toList());
    }

    public Set<BlockPos> getLogPositions() {
        return logs == null ? Collections.emptySet() : logs;
    }

    public Set<BlockPos> getLeafPositions() {
        return leaves == null ? Collections.emptySet() : leaves;
    }

    public List<BlockPos> getLogs() {
        if (logList == null) {
            logList = getLogPositions().stream().sorted(Comparator.comparingInt(Vec3i::getY)).collect(Collectors.toList());
        }
        return logList;
    }

    public List<BlockPos> getLeaves() {
        if (leafList == null) {
            leafList = getLeafPositions().stream().sorted(Comparator.comparingInt(Vec3i::getY)).collect(Collectors.toList());
        }
        return leafList;
    }

    private void recordState(BlockPos pos, BlockState state) {
        if (BlockTags.LEAVES.contains(state.getBlock())) {
            addLeaves(pos);
            return;
        }

        if (BlockTags.LOGS.contains(state.getBlock())) {
            addLog(pos);
        }
    }

    private void addLog(BlockPos pos) {
        if (logs == null) {
            logs = new HashSet<>();
        }
        logs.add(pos);
    }

    private void addLeaves(BlockPos pos) {
        if (leaves == null) {
            leaves = new HashSet<>();
        }
        leaves.add(pos);
    }
}
