package net.minecraft.spearcore.mixin;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.spearcore.item.SpearItem;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public class LocalPlayerFovMixin {

    /** 1.20.1 的 AttributeModifier 用 UUID 作 ID（1.21 才换成 ResourceLocation）。 */
    private static final java.util.UUID SPEAR_SPEED_ID =
            java.util.UUID.fromString("3f1c8a54-9d27-4b6e-8f01-6a2d5c7e9b34");

    @Inject(method = "getFieldOfViewModifier", at = @At("HEAD"), cancellable = true)
    public void onGetFieldOfViewModifier(CallbackInfoReturnable<Float> cir) {
        AbstractClientPlayer self = (AbstractClientPlayer) (Object) this;
        if (self.isUsingItem() && !self.getUseItem().isEmpty() && self.getUseItem().getItem() instanceof SpearItem) {
            float f = 1.0F;
            if (self.getAbilities().flying) {
                f *= 1.1F;
            }
            float speed = getSpeedWithoutSpearModifier(self);
            f *= (speed / self.getAbilities().getWalkingSpeed() + 1.0F) / 2.0F;
            if (self.getAbilities().getWalkingSpeed() == 0.0F || Float.isNaN(f) || Float.isInfinite(f)) {
                f = 1.0F;
            }
            cir.setReturnValue(f);
        }
    }

    private static float getSpeedWithoutSpearModifier(AbstractClientPlayer player) {
        AttributeInstance attr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attr == null) return (float) player.getAbilities().getWalkingSpeed();
        AttributeModifier spearMod = attr.getModifier(SPEAR_SPEED_ID);
        if (spearMod != null) {
            attr.removeModifier(SPEAR_SPEED_ID);
            float value = (float) attr.getValue();
            attr.addTransientModifier(spearMod);
            return value;
        }
        return (float) attr.getValue();
    }
}
