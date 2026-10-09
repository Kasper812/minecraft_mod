package com.atsuishio.portlib.nbt;

import net.minecraft.core.NonNullList;

import net.minecraft.world.ContainerHelper;

import net.minecraft.util.ProblemReporter;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public final class LegacyNbt {
    private LegacyNbt() {
    }

    public static CompoundTag tagOf(ValueInput input) {
        if (input instanceof TagValueInput tagInput) {
            return tagInput.input;
        }
        return new CompoundTag();
    }

    public static CompoundTag tagOf(ValueOutput output) {
        if (output instanceof TagValueOutput tagOutput) {
            return tagOutput.output;
        }
        return new CompoundTag();
    }

    public static HolderLookup.Provider lookup(ValueInput input) {
        return input.lookup();
    }

    public static HolderLookup.Provider lookupOrEmpty(HolderLookup.@Nullable Provider provider) {
        return provider != null ? provider : RegistryAccess.EMPTY;
    }

    public static void putUUID(CompoundTag tag, String key, UUID uuid) {
        tag.store(key, UUIDUtil.CODEC, uuid);
    }

    public static @Nullable UUID getUUID(CompoundTag tag, String key) {
        return tag.read(key, UUIDUtil.CODEC).orElse(null);
    }

    public static boolean hasUUID(CompoundTag tag, String key) {
        return tag.read(key, UUIDUtil.CODEC).isPresent();
    }

    public static Tag saveItem(ItemStack stack, HolderLookup.Provider provider) {
        return ItemStack.CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), stack).getOrThrow();
    }

    public static Tag saveItemOptional(ItemStack stack, HolderLookup.Provider provider) {
        return stack.isEmpty() ? new CompoundTag() : saveItem(stack, provider);
    }

    public static ItemStack parseItem(HolderLookup.Provider provider, Tag tag) {
        return ItemStack.CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), tag).result().orElse(ItemStack.EMPTY);
    }

    public static ItemStack parseItemOptional(HolderLookup.Provider provider, @Nullable Tag tag) {
        if (tag == null || tag instanceof CompoundTag compound && compound.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return parseItem(provider, tag);
    }

    public static ValueInput inputOf(CompoundTag tag, HolderLookup.Provider provider) {
        return TagValueInput.create(ProblemReporter.DISCARDING, provider, tag);
    }

    public static void loadAllItems(CompoundTag tag, NonNullList<ItemStack> items, HolderLookup.Provider provider) {
        ContainerHelper.loadAllItems(inputOf(tag, provider), items);
    }

    public static CompoundTag saveAllItems(CompoundTag tag, NonNullList<ItemStack> items, HolderLookup.Provider provider) {
        return saveAllItems(tag, items, true, provider);
    }

    public static CompoundTag saveAllItems(CompoundTag tag, NonNullList<ItemStack> items, boolean alsoWhenEmpty, HolderLookup.Provider provider) {
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, provider);
        ContainerHelper.saveAllItems(output, items, alsoWhenEmpty);
        tag.merge(output.buildResult());
        return tag;
    }

    public static net.minecraft.nbt.IntArrayTag createUUID(UUID uuid) {
        return new net.minecraft.nbt.IntArrayTag(net.minecraft.core.UUIDUtil.uuidToIntArray(uuid));
    }

    public static UUID loadUUID(net.minecraft.nbt.Tag tag) {
        return net.minecraft.core.UUIDUtil.uuidFromIntArray(((net.minecraft.nbt.IntArrayTag) tag).getAsIntArray());
    }
}
