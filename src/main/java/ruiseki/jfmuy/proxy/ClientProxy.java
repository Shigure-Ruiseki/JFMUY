package ruiseki.jfmuy.proxy;

import net.minecraft.client.Minecraft;

import ruiseki.jfmuy.JFMUY;
import ruiseki.jfmuy.config.KeyBindings;
import ruiseki.okcore.client.key.IKeyRegistry;
import ruiseki.okcore.init.ModBase;
import ruiseki.okcore.proxy.ClientProxyComponent;

public class ClientProxy extends ClientProxyComponent {

    public ClientProxy() {
        super(new CommonProxy());
    }

    @Override
    public ModBase getMod() {
        return JFMUY._instance;
    }

    @Override
    public void registerKeyBindings(IKeyRegistry keyRegistry) {
        super.registerKeyBindings(keyRegistry);
        KeyBindings.init();
    }

    public static boolean isCreative() {
        return Minecraft.getMinecraft().thePlayer != null
            && Minecraft.getMinecraft().thePlayer.capabilities.isCreativeMode;
    }
}
