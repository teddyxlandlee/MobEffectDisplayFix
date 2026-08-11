package xland.mcmod.mobeffectdisplayfix;

import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Predicate;

@NotNullByDefault
record MethodRef(@Nullable String owner, String name, @Nullable String desc) {
    MethodRef {
        owner = owner == null ? null : owner.replace('.', '/');
        Objects.requireNonNull(name, "name");
    }

    private boolean matchNameAndDesc(String name, String desc) {
        if (!this.name.equals(name)) return false;
        return this.desc == null || this.desc.equals(desc);
    }

    MethodNode findMethod(Collection<? extends MethodNode> methods) throws NoSuchElementException {
        return methods.stream().filter(m -> matchNameAndDesc(m.name, m.desc)).findAny().orElseThrow();
    }

    MethodInsnNode findInvocation(InsnList instructions) throws NoSuchElementException {
        return findInvocation(instructions, x -> true);
    }

    MethodInsnNode findInvocation(InsnList instructions, Predicate<? super MethodInsnNode> furtherPredicate) throws NoSuchElementException {
        for (AbstractInsnNode instruction : instructions) {
            if (instruction instanceof MethodInsnNode methodInsnNode) {
                if (owner != null && !owner.equals(methodInsnNode.owner)) continue;
                if (matchNameAndDesc(methodInsnNode.name, methodInsnNode.desc) && furtherPredicate.test(methodInsnNode)) {
                    return methodInsnNode;
                }
            }
        }
        throw new NoSuchElementException();
    }

    MethodInsnNode makeInvocation(@MagicConstant(valuesFromClass = Opcodes.class) int opcode, boolean isInterface) {
        String owner = Objects.requireNonNull(this.owner, "owner must be present");
        String desc = Objects.requireNonNull(this.desc, "descriptor must be present");
        return new MethodInsnNode(opcode, owner, name, desc, isInterface);
    }

    MethodRef remap() {
        String owner = Objects.requireNonNull(this.owner, "owner must be present");
        String name = this.name;
        String desc = Objects.requireNonNull(this.desc, "descriptor must be present");

        if (Platform.REMAPPER == Remapper.NOOP) return this; // short-circuit
        name = Platform.REMAPPER.mapMethodName(owner, name, desc);
        owner = Platform.REMAPPER.mapClassName(owner);
        desc = Platform.REMAPPER.mapMethodDesc(desc);
        return new MethodRef(owner, name, desc);
    }

    MethodRef ignoreOwner() {
        return new MethodRef(null, name, desc);
    }

    MethodRef ignoreDescriptor() {
        return new MethodRef(owner, name, null);
    }

    MethodRef retainNameOnly() {
        return new MethodRef(null, name, null);
    }

    @Override
    public String toString() {
        String methodName = desc == null ? name : name + desc;

        if (owner == null) {
            return methodName;
        } else {
            return owner + "." + methodName;
        }
    }

    static class Remapper {
        static final Remapper NOOP = new Remapper();

        final String mapClassName(String name) {
            if (name.charAt(0) == '[') {
                return mapTypeDesc(name);
            } else {
                return mapClassInternalName(name);
            }
        }

        String mapClassInternalName(String name) {
            return name;
        }

        String mapMethodName(String owner, String name, String desc) {
            return name;
        }

        final String mapMethodDesc(String desc) {
            Type type = Type.getMethodType(desc);
            Type[] argumentTypes = type.getArgumentTypes();
            for (int i = argumentTypes.length - 1; i >= 0; i--) {
                argumentTypes[i] = mapType(argumentTypes[i]);
            }
            Type returnType = mapType(type.getReturnType());
            return Type.getMethodDescriptor(returnType, argumentTypes);
        }

        private Type mapType(Type type) {
            return Type.getType(mapTypeDesc(type.getDescriptor()));
        }

        final String mapTypeDesc(String desc) {
            Type type = Type.getType(desc);
            return switch (type.getSort()) {
                case Type.METHOD -> mapMethodDesc(desc);    // shouldn't be here!
                case Type.OBJECT -> 'L' + mapClassInternalName(type.getInternalName()) + ';';
                case Type.ARRAY -> {
                    int dimensions = type.getDimensions();
                    yield "[".repeat(dimensions) + mapTypeDesc(type.getElementType().getDescriptor());
                }
                default -> desc;
            };
        }
    }
}
