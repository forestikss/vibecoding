package ru.etc1337.api.events.impl.game;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import ru.etc1337.api.events.Event;

@Getter
@AllArgsConstructor
public class EventPlace extends Event {
    private final Block block;
    private final BlockPos pos;
}
