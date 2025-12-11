package xland.mcmod.mobeffectdisplayfix;

import net.fabricmc.loader.api.*;
import net.fabricmc.loader.api.metadata.version.VersionPredicate;

import java.util.Locale;

record Platform(MethodRef redirectTarget, MethodRef getEffect, MethodRef getAmplifier, MethodRef redirectSource) {
    Platform {
        redirectTarget = redirectTarget.remap();
        getEffect = getEffect.remap();
        getAmplifier = getAmplifier.remap();
        redirectSource = redirectSource.remap().retainNameOnly();
    }

    static final boolean isMojMapped;
    static final MethodRef.Remapper REMAPPER;

    static {
        if (!isFabricEnv()) {
            isMojMapped = true; // forge-like environments are fully moj-mapped since 1.20.6
            REMAPPER = MethodRef.Remapper.NOOP;
        } else {
            final class FabricPlatformRemapper extends MethodRef.Remapper {
                private static VersionPredicate predicateOf(String value) {
                    try {
                        return VersionPredicate.parse(value);
                    } catch (VersionParsingException e) {
                        throw new IllegalArgumentException("Invalid predicate: " + value, e);
                    }
                }

                private static final String UNOBFUSCATED_BUILD = "unobfuscated";

                static final boolean isMojMapped;

                static {
                    final VersionPredicate pre25w44a = predicateOf("<=1.21.11-alpha.25.44.a");
                    // in the hope that Mojang does not release obfuscated drops as 26.0 or something
                    final VersionPredicate postMountsOfMayhem = predicateOf(">=26");

                    final Version mcVersion = FabricLoader.getInstance().getModContainer("minecraft").orElseThrow().getMetadata().getVersion();

                    isMojMapped = !pre25w44a.test(mcVersion) && (
                            postMountsOfMayhem.test(mcVersion) ||
                                    mcVersion instanceof SemanticVersion semMcVersion &&
                                            semMcVersion.getBuildKey().orElse("").toLowerCase(Locale.ROOT).contains(UNOBFUSCATED_BUILD)
                    );
                }

                final MappingResolver mappingResolver = FabricLoader.getInstance().getMappingResolver();
                private static final String INTERMEDIARY_NAMESPACE = "intermediary";

                private static String toBinary(String internalName) {
                    return internalName.replace('/', '.');
                }

                @Override
                String mapClassInternalName(String name) {
                    return mappingResolver.mapClassName(INTERMEDIARY_NAMESPACE, toBinary(name)).replace('.', '/');
                }

                @Override
                String mapMethodName(String owner, String name, String desc) {
                    return mappingResolver.mapMethodName(INTERMEDIARY_NAMESPACE, toBinary(owner), name, desc);
                }
            }

            isMojMapped = FabricPlatformRemapper.isMojMapped;
            REMAPPER = isMojMapped ? MethodRef.Remapper.NOOP : new FabricPlatformRemapper();
        }
    }

    private static boolean isFabricEnv() {
        try {
            Class.forName("net.fabricmc.api.EnvType");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    static final Platform CURRENT;

    static {
        CURRENT = isMojMapped ? new Platform(
            /*redirectTarget*/new MethodRef("net.minecraft.world.item.alchemy.PotionContents", "getPotionDescription", "(Lnet/minecraft/core/Holder;I)Lnet/minecraft/network/chat/MutableComponent;"),
            /*getEffect*/new MethodRef("net.minecraft.world.effect.MobEffectInstance", "getEffect", "()Lnet/minecraft/core/Holder;"),
            /*getAmplifier*/new MethodRef("net.minecraft.world.effect.MobEffectInstance", "getAmplifier", "()I"),
            /*redirectSource*/new MethodRef("net.minecraft.client.gui.screens.inventory.EffectsInInventory", "getEffectName", "(Lnet/minecraft/world/effect/MobEffectInstance;)Lnet/minecraft/network/chat/Component;")
        ) : new Platform(
            /*redirectTarget*/new MethodRef("net.minecraft.class_1844", "method_66698", "(Lnet/minecraft/class_6880;I)Lnet/minecraft/class_5250;"),
            /*getEffect*/new MethodRef("net.minecraft.class_1293", "method_5579", "()Lnet/minecraft/class_6880;"),
            /*getAmplifier*/new MethodRef("net.minecraft.class_1293", "method_5578", "()I"),
            /*redirectSource*/new MethodRef("net.minecraft.class_485", "method_38933", "(Lnet/minecraft/class_1293;)Lnet/minecraft/class_2561;")
        );
    }
}
