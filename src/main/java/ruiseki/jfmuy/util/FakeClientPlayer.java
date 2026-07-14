package ruiseki.jfmuy.util;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IChatComponent;

import org.jetbrains.annotations.Nullable;

import com.mojang.authlib.GameProfile;

public class FakeClientPlayer extends EntityPlayer {

    @Nullable
    public static FakeClientPlayer INSTANCE;

    public static FakeClientPlayer getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FakeClientPlayer();
        }
        return INSTANCE;
    }

    private FakeClientPlayer() {
        super(FakeClientWorld.getInstance(), new GameProfile(new UUID(0, 0), "JFMUY_Fake"));
    }

    @Override
    public void addChatMessage(IChatComponent message) {

    }

    @Override
    public boolean canCommandSenderUseCommand(int permissionLevel, String command) {
        return false;
    }

    @Override
    public ChunkCoordinates getPlayerCoordinates() {
        return null;
    }
}
