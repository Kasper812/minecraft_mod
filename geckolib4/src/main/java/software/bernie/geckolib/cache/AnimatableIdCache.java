package software.bernie.geckolib.cache;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import software.bernie.geckolib.GeckoLibConstants;

public final class AnimatableIdCache extends SavedData {
	private static final Codec<AnimatableIdCache> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.LONG.optionalFieldOf("last_id", 0L).forGetter(cache -> cache.lastId)
	).apply(i, AnimatableIdCache::new));
	private static final SavedDataType<AnimatableIdCache> TYPE = new SavedDataType<>(GeckoLibConstants.id("geckolib_id_cache"), AnimatableIdCache::new, CODEC, null);
	private long lastId;

	private AnimatableIdCache() {
	}

	private AnimatableIdCache(long lastId) {
		this.lastId = lastId;
	}

	public static long getFreeId(ServerLevel level) {
		return getCache(level).getNextId();
	}

	private long getNextId() {
		setDirty();

		return ++this.lastId;
	}

	private static AnimatableIdCache getCache(ServerLevel level) {
		return level.getServer().overworld().getDataStorage().computeIfAbsent(TYPE);
	}
}
