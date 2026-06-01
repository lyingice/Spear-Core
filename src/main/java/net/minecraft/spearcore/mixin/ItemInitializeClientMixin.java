package net.minecraft.spearcore.mixin;

import net.minecraft.spearcore.client.SpearClientExtensions;
import net.minecraft.spearcore.item.SpearItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(Item.class)
public class ItemInitializeClientMixin {

    @Inject(method = "initializeClient", at = @At("HEAD"), cancellable = true)
    public void onInitializeClient(Consumer<IClientItemExtensions> consumer, CallbackInfo ci) {
        Item self = (Item) (Object) this;
        if (self instanceof SpearItem) {
            consumer.accept(new SpearClientExtensions());
            ci.cancel();
        }
    }
}