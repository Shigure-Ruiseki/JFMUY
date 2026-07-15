package ruiseki.jfmuy.gui.textures;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.Stitcher;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;

import org.apache.commons.io.IOUtils;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ruiseki.jfmuy.Reference;
import ruiseki.jfmuy.util.Log;

@SideOnly(Side.CLIENT)
public class JFMUYTextureMap extends TextureMap {

    private final ResourceLocation location;
    private final String basePathName;

    public JFMUYTextureMap(String basePathIn) {
        super(2, basePathIn);
        this.basePathName = basePathIn;
        this.location = new ResourceLocation(Reference.MOD_ID, basePathIn);
    }

    public ResourceLocation getLocation() {
        return location;
    }

    @Override
    public void loadTexture(IResourceManager resourceManager) throws IOException {
        this.initMissingImage();
        this.deleteGlTexture();
        this.loadTextureAtlas(resourceManager);
    }

    @Override
    public void loadTextureAtlas(IResourceManager resourceManager) {
        int maxTextureSize = Minecraft.getGLMaximumTextureSize();
        Stitcher stitcher = new Stitcher(maxTextureSize, maxTextureSize, true, 0, 0);

        this.mapUploadedSprites.clear();
        this.listAnimatedSprites.clear();

        Iterator iterator = this.mapRegisteredSprites.entrySet()
            .iterator();

        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            final TextureAtlasSprite sprite = (TextureAtlasSprite) entry.getValue();
            ResourceLocation resourcelocation = this.completeResourceLocation(sprite);
            IResource iresource = null;
            InputStream inputstream = null;

            try {
                iresource = resourceManager.getResource(resourcelocation);
                inputstream = iresource.getInputStream();

                BufferedImage bufferedimage = ImageIO.read(inputstream);
                AnimationMetadataSection animationmetadatasection = (AnimationMetadataSection) iresource
                    .getMetadata("animation");

                BufferedImage[] abufferedimage = new BufferedImage[1];
                abufferedimage[0] = bufferedimage;

                sprite.loadSprite(abufferedimage, animationmetadatasection, false);

            } catch (RuntimeException runtimeexception) {
                FMLClientHandler.instance()
                    .trackBrokenTexture(resourcelocation, runtimeexception.getMessage());
                continue;
            } catch (IOException ioexception) {
                FMLClientHandler.instance()
                    .trackMissingTexture(resourcelocation);
                continue;
            } finally {
                // Sửa lỗi 2: Đóng gói luồng InputStream thô của IResource để IOUtils nhận diện đúng kiểu dữ liệu
                // Closeable
                IOUtils.closeQuietly(inputstream);
            }

            stitcher.addSprite(sprite);
        }

        stitcher.addSprite(this.missingImage);

        try {
            stitcher.doStitch();
        } catch (Exception e) {
            Log.get()
                .error("Failed to stitch texture map!", e);
        }

        Log.get()
            .info(
                "Created: {}x{} {}-atlas",
                stitcher.getCurrentWidth(),
                stitcher.getCurrentHeight(),
                this.basePathName);

        TextureUtil.allocateTexture(this.getGlTextureId(), stitcher.getCurrentWidth(), stitcher.getCurrentHeight());

        HashMap<String, TextureAtlasSprite> backupMap = new HashMap<String, TextureAtlasSprite>(
            this.mapRegisteredSprites);

        Iterator stitchSlotsIterator = stitcher.getStichSlots()
            .iterator();

        while (stitchSlotsIterator.hasNext()) {
            final TextureAtlasSprite currentSprite = (TextureAtlasSprite) stitchSlotsIterator.next();
            String name = currentSprite.getIconName();
            backupMap.remove(name);
            this.mapUploadedSprites.put(name, currentSprite);

            try {
                TextureUtil.uploadTextureSub(
                    this.getGlTextureId(),
                    currentSprite.getFrameTextureData(0)[0],
                    currentSprite.getIconWidth(),
                    currentSprite.getIconHeight(),
                    currentSprite.getOriginX(),
                    currentSprite.getOriginY(),
                    false, // blur
                    false, // clamp
                    false // mipmap
                );
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Stitching texture atlas");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Texture being stitched together");
                crashreportcategory.addCrashSection("Atlas path", this.basePathName);
                crashreportcategory.addCrashSection("Sprite", currentSprite);
                throw new ReportedException(crashreport);
            }

            if (currentSprite.hasAnimationMetadata()) {
                this.listAnimatedSprites.add(currentSprite);
            }
        }

        Iterator fallbackIterator = backupMap.values()
            .iterator();
        while (fallbackIterator.hasNext()) {
            TextureAtlasSprite fallbackSprite = (TextureAtlasSprite) fallbackIterator.next();
            fallbackSprite.copyFrom(this.missingImage);
        }
    }

    private ResourceLocation completeResourceLocation(TextureAtlasSprite sprite) {
        ResourceLocation baseResource = new ResourceLocation(sprite.getIconName());
        return new ResourceLocation(
            baseResource.getResourceDomain(),
            String.format("%s/%s%s", this.basePathName, baseResource.getResourcePath(), ".png"));
    }

    public TextureAtlasSprite registerSprite(ResourceLocation location) {
        return (TextureAtlasSprite) this.registerIcon(location.toString());
    }
}
