package xland.mcmod.mobeffectdisplayfix.mixins;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xland.mcmod.mobeffectdisplayfix.impl.GetEffectName;

@Pseudo
@Mixin(targets = "net.minecraft.client.gui.screens.inventory.EffectsInInventory")
@SuppressWarnings("all")
abstract class MixinNewEffectsWidgetContainer {
    @Inject(at = @At("HEAD"), cancellable = true, method = "getEffectName", remap = false)
    private void redirectGetEffectName(MobEffectInstance mobEffectInstance, CallbackInfoReturnable<Component> cir) {
        cir.setReturnValue(GetEffectName.getEffectName(mobEffectInstance));
    }
}
