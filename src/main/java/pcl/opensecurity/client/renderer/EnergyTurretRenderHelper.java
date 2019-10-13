package pcl.opensecurity.client.renderer;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import pcl.opensecurity.common.blocks.BlockEnergyTurret;
import pcl.opensecurity.common.tileentity.TileEntityEnergyTurret;

public class EnergyTurretRenderHelper extends TileEntityItemStackRenderer {
    private final TileEntityItemStackRenderer parentRenderer;

    public EnergyTurretRenderHelper(TileEntityItemStackRenderer parent){
        this.parentRenderer = parent;
    }

    private TileEntityEnergyTurret turrettRender = new TileEntityEnergyTurret();
    @Override
    public void renderByItem(ItemStack itemStack) {
        Block block = Block.getBlockFromItem(itemStack.getItem());

        if (block.equals(BlockEnergyTurret.DEFAULTITEM)) {
            TileEntityRendererDispatcher.instance.render(this.turrettRender, 0.0D, 0.0D, 0.0D, 0.0F);
        }else {
            parentRenderer.renderByItem(itemStack);
        }
    }
}