package net.minecraft.spearcore.mixin;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.spearcore.item.SpearItem;
import net.minecraft.world.entity.monster.Monster;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 复刻 J 库 AbstractZombieModelMixin：
 * 僵尸类模型默认会在 AbstractZombieModel.setupAnim 末尾套用双臂前伸的僵尸攻击姿势，
 * 这会覆盖/干扰 HumanoidModelMixin 中的长矛持握和蓄力姿势。
 * 当僵尸类主手持矛时，直接调用 HumanoidModel.setupAnim 并取消 AbstractZombieModel 后续逻辑，
 * 让长矛动画按普通人形模型流程生效。
 */
@Mixin(net.minecraft.client.model.AbstractZombieModel.class)
public abstract class AbstractZombieModelMixin<T extends Monster> extends HumanoidModel<T> {

    public AbstractZombieModelMixin(ModelPart root) {
        super(root);
    }

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/monster/Monster;FFFFF)V", at = @At("HEAD"), cancellable = true)
    private void spearcore$setupSpearZombieAnim(T entity, float limbSwing, float limbSwingAmount,
                                                float ageInTicks, float netHeadYaw, float headPitch,
                                                CallbackInfo ci) {
        if (entity.getMainHandItem().getItem() instanceof SpearItem) {
            super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            ci.cancel();
        }
    }
}
