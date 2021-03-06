package elec332.core.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;

import javax.annotation.Nonnull;

/**
 * Created by Elec332 on 29-12-2016.
 */
public interface ICompatibleInventory extends IInventory {

    @Override
    default public boolean isUsableByPlayer(@Nonnull EntityPlayer player){
        return canBeUsedByPlayer(player);
    }

    @Override
    default public boolean isEmpty(){
        return isInventoryEmpty();
    }

    public boolean canBeUsedByPlayer(@Nonnull EntityPlayer player);

    public boolean isInventoryEmpty();

}
