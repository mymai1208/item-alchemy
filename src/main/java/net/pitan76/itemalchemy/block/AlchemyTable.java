package net.pitan76.itemalchemy.block;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.util.ActionResult;
import net.minecraft.util.shape.VoxelShape;
import net.pitan76.itemalchemy.EMCManager;
import net.pitan76.itemalchemy.gui.AlchemyTableScreenHandlerFactory;
import net.pitan76.mcpitanlib.api.block.CompatibleBlockSettings;
import net.pitan76.mcpitanlib.api.block.ExtendBlock;
import net.pitan76.mcpitanlib.api.entity.Player;
import net.pitan76.mcpitanlib.api.event.block.BlockUseEvent;
import net.pitan76.mcpitanlib.api.event.block.OutlineShapeEvent;
import net.pitan76.mcpitanlib.api.util.VoxelShapeUtil;
import net.pitan76.mcpitanlib.core.serialization.CompatMapCodec;

public class AlchemyTable extends ExtendBlock {
    public AlchemyTable(CompatibleBlockSettings settings) {
        super(settings);
    }

    public AlchemyTable() {
        this(CompatibleBlockSettings.copy(Blocks.STONE).mapColor(MapColor.BLACK).strength(1.5f, 7.0f));
    }

    @Override
    public ActionResult onRightClick(BlockUseEvent e) {
        if (e.isClient()) return ActionResult.SUCCESS;

        if (e.player.isServerPlayerEntity())
            EMCManager.syncS2C(e.player);

        Player player = e.player;
        player.openGuiScreen(new AlchemyTableScreenHandlerFactory());
        return ActionResult.CONSUME;
    }

    @Override
    public VoxelShape getOutlineShape(OutlineShapeEvent e) {
        return VoxelShapeUtil.cuboid(0, 0, 0, 1, 0.125, 1);
    }

    @Override
    public CompatMapCodec<? extends Block> getCompatCodec() {
        return CompatMapCodec.createCodecOfExtendBlock(AlchemyTable::new);
    }
}
