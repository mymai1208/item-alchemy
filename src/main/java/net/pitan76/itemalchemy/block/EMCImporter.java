package net.pitan76.itemalchemy.block;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.pitan76.itemalchemy.tile.EMCImporterTile;
import net.pitan76.itemalchemy.tile.Tiles;
import net.pitan76.mcpitanlib.api.block.CompatibleBlockSettings;
import net.pitan76.mcpitanlib.api.block.ExtendBlock;
import net.pitan76.mcpitanlib.api.block.ExtendBlockEntityProvider;
import net.pitan76.mcpitanlib.api.event.block.BlockUseEvent;
import net.pitan76.mcpitanlib.api.event.block.StateReplacedEvent;
import net.pitan76.mcpitanlib.api.util.TextUtil;
import net.pitan76.mcpitanlib.core.serialization.CompatMapCodec;
import org.jetbrains.annotations.Nullable;

public class EMCImporter extends ExtendBlock implements ExtendBlockEntityProvider, IUseableWrench {
    private static final Text TITLE = TextUtil.translatable("container.itemalchemy.emc_importer");

    protected CompatMapCodec<? extends Block> CODEC = CompatMapCodec.createCodecOfExtendBlock(EMCImporter::new);

    @Override
    public CompatMapCodec<? extends Block> getCompatCodec() {
        return CODEC;
    }

    public EMCImporter(CompatibleBlockSettings settings) {
        super(settings);
    }

    public EMCImporter() {
        this(CompatibleBlockSettings.copy(Blocks.STONE).mapColor(MapColor.YELLOW).strength(2f, 7.0f));
    }

    @Override
    public void onStateReplaced(StateReplacedEvent e) {
        super.onStateReplaced(e);
    }

    @Override
    public ActionResult onRightClick(BlockUseEvent e) {
        if (e.isClient()) return ActionResult.SUCCESS;

        BlockEntity blockEntity = e.getBlockEntity();
        if (blockEntity instanceof EMCImporterTile) {
            EMCImporterTile tile = (EMCImporterTile)blockEntity;

            if (!tile.hasTeam()) {
                boolean isSuccess = tile.setTeam(e.player);
                if (!isSuccess) {
                    e.player.sendMessage(TextUtil.translatable("message.itemalchemy.failed_to_set_team"));
                    return ActionResult.FAIL;
                }
            }

            e.player.openExtendedMenu(tile);
            return ActionResult.CONSUME;
        }

        return ActionResult.PASS;
    }

    @Nullable
    @Override
    public Text getScreenTitle() {
        return TITLE;
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityType<T> getBlockEntityType() {
        return (BlockEntityType<T>) Tiles.EMC_IMPORTER.getOrNull();
    }

    @Override
    public boolean isTick() {
        return true;
    }
}
