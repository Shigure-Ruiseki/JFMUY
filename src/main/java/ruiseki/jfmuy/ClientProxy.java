package ruiseki.jfmuy;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;

import ruiseki.jfmuy.api.IModPlugin;
import ruiseki.jfmuy.config.KeyBindings;
import ruiseki.jfmuy.startup.JFMUYStarter;
import ruiseki.okcore.client.key.IKeyRegistry;
import ruiseki.okcore.init.ModBase;
import ruiseki.okcore.proxy.ClientProxyComponent;

public class ClientProxy extends ClientProxyComponent {

    private List<IModPlugin> plugins = new ArrayList<>();
    private final JFMUYStarter starter = new JFMUYStarter();

    public ClientProxy() {
        super(new CommonProxy());
    }

    @Override
    public ModBase getMod() {
        return JFMUY.instance;
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
