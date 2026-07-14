package ruiseki.jfmuy.util;

import net.minecraft.entity.Entity;
import net.minecraft.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.SaveHandlerMP;

import org.jetbrains.annotations.Nullable;

public class FakeClientWorld extends World {

    private static final WorldSettings worldSettings = new WorldSettings(
        0,
        WorldSettings.GameType.SURVIVAL,
        false,
        false,
        WorldType.DEFAULT);
    private static final ISaveHandler saveHandler = new SaveHandlerMP();

    private static final WorldProvider worldProvider = new WorldProvider() {

        @Override
        public String getDimensionName() {
            return "overworld";
        }

        @Override
        public String getSaveFolder() {
            return null;
        }
    };

    @Nullable
    private static FakeClientWorld INSTANCE;

    public static FakeClientWorld getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FakeClientWorld();
        }
        return INSTANCE;
    }

    private FakeClientWorld() {
        super(saveHandler, "jei_fake", worldProvider, worldSettings, new Profiler());

        this.provider.registerWorld(this);

        this.mapStorage = new MapStorage(saveHandler);
    }

    @Override
    protected IChunkProvider createChunkProvider() {
        return null;
    }

    @Override
    protected int func_152379_p() {
        return 0;
    }

    @Override
    public Entity getEntityByID(int p_73045_1_) {
        return null;
    }
}
