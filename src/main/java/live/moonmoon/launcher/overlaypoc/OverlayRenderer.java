package live.moonmoon.launcher.overlaypoc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import live.moonmoon.launcher.overlaypoc.serialization.ClientChatEventSerializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ClientChatEvent;
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
  private final Gson gson = new GsonBuilder()
    .registerTypeAdapter(ClientChatEvent.class, new ClientChatEventSerializer())
    .create();
  private static ResourceLocation loc;

  protected static BufferedImage imgBuff;
  protected static boolean hasRefreshed = true;

  public OverlayRenderer() {
    zLevel = 0f;
    loopThread.start();
  }

  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public void hidePersistentChat(RenderGameOverlayEvent.Pre ev) {
    if (ev.getType() == RenderGameOverlayEvent.ElementType.CHAT)
      ev.setCanceled(true);
  }

  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public void beforeChatSend(ClientChatEvent ev) {
    // Cancel all message send events that aren't Minecraft commands
    if (!ev.getMessage().startsWith("/")) {
      final String output = gson.toJson(ev);
      Overlay.server.send(output);

      ev.setCanceled(true);
    }
  }

  @SubscribeEvent(priority = EventPriority.LOWEST)
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
    final ScaledResolution res = new ScaledResolution(mc);
    final int width = (int)(res.getScaledWidth() * 0.75);
    final int height = res.getScaledHeight() / 2;
    GlStateManager.pushMatrix();

    GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

    GlStateManager.enableBlend();

    mc.getTextureManager().bindTexture(loc);

//    drawModalRectWithCustomSizedTexture(0, res.getScaledHeight() - (int)(height * 1.37), 0f, 0f, width, height, res.getScaledWidth(), res.getScaledHeight());
    drawModalRectWithCustomSizedTexture(0, res.getScaledHeight() - (int)(height * 1.37), 0f, 0f, width, height, width, height);
    GlStateManager.popMatrix();

    GlStateManager.disableBlend();

    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

    mc.renderEngine.bindTexture(new ResourceLocation("minecraft", "textures/gui/icons.png"));

    mc.profiler.endSection();
  }
}

class RenderLoop implements Runnable {
  public void run() {
    while (true) {
      byte[] reply = Overlay.client.recv(0);
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
