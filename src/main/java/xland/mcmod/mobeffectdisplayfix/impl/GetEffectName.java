package xland.mcmod.mobeffectdisplayfix.impl;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

public interface GetEffectName {
    static Component getEffectName(MobEffectInstance mobEffectInstance) {
        return getPotionDescription(mobEffectInstance.getEffect(), mobEffectInstance.getAmplifier());
    }

    private static Component getPotionDescription(Holder<MobEffect> holder, int amplifier) {
        Component text = Component.translatable(holder.value().getDescriptionId());
        return amplifier > 0 ? Component.translatable(
                "potion.withAmplifier",
                text,
                Component.translatable("potion.potency." + amplifier)
        ) : text;
    }
}
