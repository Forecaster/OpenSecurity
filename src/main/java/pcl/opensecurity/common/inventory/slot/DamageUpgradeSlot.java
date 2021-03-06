package pcl.opensecurity.common.inventory.slot;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import pcl.opensecurity.common.items.ItemDamageUpgrade;
import pcl.opensecurity.common.tileentity.TileEntityEnergyTurret;
import java.util.Arrays;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class DamageUpgradeSlot extends BaseSlot<TileEntityEnergyTurret> implements ISlotTooltip {

    public DamageUpgradeSlot(TileEntityEnergyTurret tileEntity, IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(tileEntity, itemHandler, index, xPosition, yPosition);
    }

    @Override
    public boolean isItemValid(@Nonnull ItemStack stack) {
        return stack.getItem() instanceof ItemDamageUpgrade;
    }

    @Override
    public List<String> getTooltip(){
        return new ArrayList<>(Arrays.asList("Accepted Items:", "Damage Upgrade"));
    }
}
