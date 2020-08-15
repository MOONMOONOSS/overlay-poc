package live.moonmoon.launcher.overlaypoc;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

public class OverlayRenderer {
  private final Minecraft mc = Minecraft.getMinecraft();
  private static final Runnable loop = new RenderLoop();
  private static final Thread loopThread = new Thread(loop);

  protected static BufferedImage imgBuff = new BufferedImage(1280, 720, TYPE_INT_ARGB);

  public OverlayRenderer() {
    loopThread.start();
  }

  @SubscribeEvent(priority = EventPriority.LOW)
  public void renderOverlay(RenderGameOverlayEvent.Pre ev) {
    final ScaledResolution scaled = new ScaledResolution(mc);
    ev.setCanceled(true);

    mc.profiler.startSection("overlay-poc");
    GlStateManager.pushMatrix();
    GlStateManager.enableBlend();

    final ResourceLocation loc = mc
      .getTextureManager()
      .getDynamicTextureLocation(
        "overlay-poc-img",
        new DynamicTexture(imgBuff)
      );

    mc.getTextureManager().bindTexture(loc);

    mc.ingameGUI.drawTexturedModalRect(0, 0, 0, 0, scaled.getScaledWidth(), scaled.getScaledHeight());

    GlStateManager.disableBlend();
    GlStateManager.popMatrix();

    mc.getTextureManager().deleteTexture(loc);

    mc.profiler.endSection();
    ev.setCanceled(true);
  }
}

class RenderLoop implements Runnable {
  public void run() {
    while (true) {
      byte[] reply = Overlay.socket.recv(0);
      final InputStream in = new ByteArrayInputStream(reply);

      try {
        OverlayRenderer.imgBuff = ImageIO.read(in);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
