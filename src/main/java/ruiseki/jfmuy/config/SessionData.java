package ruiseki.jfmuy.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.NetworkManager;
import net.minecraft.server.MinecraftServer;

import org.jetbrains.annotations.Nullable;

import cpw.mods.fml.client.FMLClientHandler;

public class SessionData {

    private static boolean jfmuyOnServer = false;
    private static boolean joinedWorld = false;
    @Nullable
    private static String worldUid = null;

    private SessionData() {

    }

    public static boolean isJfmuyOnServer() {
        return jfmuyOnServer;
    }

    public static void onConnectedToServer(boolean jeiOnServer) {
        SessionData.jfmuyOnServer = jeiOnServer;
        SessionData.joinedWorld = false;
        SessionData.worldUid = null;
    }

    public static String getWorldUid() {
        if (worldUid == null) {
            FMLClientHandler fmlClientHandler = FMLClientHandler.instance();
            final NetworkManager networkManager = fmlClientHandler.getClientToServerNetworkManager();
            if (networkManager == null) {
                worldUid = "default";
            } else if (networkManager.isLocalChannel()) {
                final MinecraftServer minecraftServer = fmlClientHandler.getServer();
                if (minecraftServer != null) {
                    worldUid = minecraftServer.getFolderName();
                }
            } else {
                final ServerData serverData = Minecraft.getMinecraft()
                    .func_147104_D();
                if (serverData != null) {
                    worldUid = serverData.serverIP + ' ' + serverData.serverName;
                }
            }

            if (worldUid == null) {
                worldUid = "default";
            }
            worldUid = "world" + Integer.toString(worldUid.hashCode());
        }
        return worldUid;
    }

    public static boolean hasJoinedWorld() {
        return joinedWorld;
    }

    public static void setJoinedWorld() {
        SessionData.joinedWorld = true;
    }
}
