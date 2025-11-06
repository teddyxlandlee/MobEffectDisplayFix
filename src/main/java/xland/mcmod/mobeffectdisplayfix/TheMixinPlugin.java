package xland.mcmod.mobeffectdisplayfix;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public final class TheMixinPlugin implements IMixinConfigPlugin {
    static void hook(ClassNode classNode) {
        MethodNode mixinTarget = Platform.CURRENT.mixinTarget().findMethod(classNode.methods);
        MethodInsnNode redirectSource = Platform.CURRENT.redirectSource().findInvocation(mixinTarget.instructions);
        InsnList replaced = new InsnList();

        if (redirectSource.getOpcode() != Opcodes.INVOKESTATIC) {
            // we don't need the thisRef
            replaced.add(new InsnNode(Opcodes.SWAP));
            replaced.add(new InsnNode(Opcodes.POP));
        }

        replaced.add(new InsnNode(Opcodes.DUP));
        replaced.add(Platform.CURRENT.getEffect().makeInvocation(Opcodes.INVOKEVIRTUAL, false));
        replaced.add(Platform.CURRENT.getAmplifier().makeInvocation(Opcodes.INVOKEVIRTUAL, false));
        replaced.add(Platform.CURRENT.redirectTarget().makeInvocation(Opcodes.INVOKESTATIC, false));

        mixinTarget.instructions.insert(redirectSource, replaced);
        mixinTarget.instructions.remove(redirectSource);
    }

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return Platform.isMojMapped ? null : "intermediary-refmap.json";
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        if (mixinClassName.endsWith("Stub")) {
            hook(targetClass);
        }
    }
}
