package xland.mcmod.mobeffectdisplayfix;

import dev.architectury.injectables.targets.ArchitecturyTarget;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public final class TheMixinPlugin implements IMixinConfigPlugin {
    private boolean shouldApplyNewMixin;

    @Override
    public void onLoad(String mixinPackage) {
        if (!"fabric".equalsIgnoreCase(ArchitecturyTarget.getCurrentTarget())) {
            int mcVersion;  // (major << 4 | minor)
            try {
                String[] split = forgeLikeGetMcVersion().split("\\.", 3);
                mcVersion = (Integer.parseInt(split[1]) << 4) | Integer.parseInt(split[2]);
            } catch (Exception e) {
                mcVersion = (19 << 4) | 5;  // fallback to "1.19.5"
            }

            shouldApplyNewMixin = mcVersion >= ((21 << 4) | 2);
        }
    }

    private static String forgeLikeGetMcVersion() {
        var lookup = MethodHandles.lookup();
        Class<?> fmlLoaderClass = null;

        // NeoForge
        try {
            fmlLoaderClass = lookup.findClass("net.neoforged.fml.loading.FMLLoader");
        } catch (ClassNotFoundException | IllegalAccessException ignore) {
        }
        // MinecraftForge
        if (fmlLoaderClass == null) {
            try {
                fmlLoaderClass = lookup.findClass("net.minecraftforge.fml.loading.FMLLoader");
            } catch (ClassNotFoundException | IllegalAccessException ignore) {
            }
        }
        if (fmlLoaderClass == null) {
            throw new IllegalStateException("Not a forge-like environment");
        }

        try {
            Object versionInfo = lookup.unreflect(fmlLoaderClass.getMethod("versionInfo")).invoke();
            var handle = lookup.findVirtual(versionInfo.getClass(), "mcVersion", MethodType.methodType(String.class)).bindTo(versionInfo);
            return (String) handle.invokeExact();
        } catch (Throwable t) {
            throw new IllegalStateException("Failed to fetch versionInfo", t);
        }
    }

    @Override
    public String getRefMapperConfig() {
        return null;    // already provided
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return !shouldApplyNewMixin;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return shouldApplyNewMixin ? Collections.singletonList("MixinNewEffectsWidgetContainer") : null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
