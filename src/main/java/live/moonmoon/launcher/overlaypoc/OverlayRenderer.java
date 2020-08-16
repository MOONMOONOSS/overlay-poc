package live.moonmoon.launcher.overlaypoc;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
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

public class OverlayRenderer extends Gui {
  private final Minecraft mc = Minecraft.getMinecraft();
  private static final Runnable loop = new RenderLoop();
  private static final Thread loopThread = new Thread(loop);

  protected static BufferedImage imgBuff;
  protected static boolean hasRefreshed = true;
  protected static ResourceLocation loc;

  public OverlayRenderer() {
    loopThread.start();
  }

  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public void renderOverlay(RenderGameOverlayEvent.Post ev) {
    if (imgBuff == null) return;
    if (hasRefreshed || loc == null) {
      mc.getTextureManager().deleteTexture(loc);

      loc = mc
        .getTextureManager()
        .getDynamicTextureLocation(
          "overlay-poc-img",
          new DynamicTexture(imgBuff)
        );
      hasRefreshed = false;
    }

    mc.profiler.startSection("overlay-poc");

    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    GlStateManager.enableBlend();

    mc.getTextureManager().bindTexture(loc);

    mc.ingameGUI.drawTexturedModalRect(0, 0, 0, 0, mc.displayWidth, mc.displayHeight);

    GlStateManager.disableBlend();

    mc.profiler.endSection();
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

      OverlayRenderer.hasRefreshed = true;
    }
  }
}
