package de.neincraft.neincraftplugin.util;

import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class BlockBatchChangedEvent extends BlockEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private boolean cancelled = false;
    private final List<Block> changingBlocks;

    public BlockBatchChangedEvent(@NotNull Block sourceBlock, List<Block> changingBlocks) {
        super(sourceBlock);
        this.changingBlocks = new ArrayList<>(changingBlocks);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public List<Block> getChangingBlocks() {
        return changingBlocks;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }
}
