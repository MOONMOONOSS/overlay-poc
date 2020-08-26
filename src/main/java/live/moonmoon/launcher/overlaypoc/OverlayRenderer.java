package live.moonmoon.launcher.overlaypoc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.GlStateManager;
import live.moonmoon.launcher.overlaypoc.serialization.ClientChatEventSerializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class OverlayRenderer extends AbstractGui {
  private final Minecraft mc = Minecraft.getInstance();
  private final Gson gson = new GsonBuilder()
    .registerTypeAdapter(ClientChatEvent.class, new ClientChatEventSerializer())
    .registerTypeAdapter(ITextComponent.Serializer.class, new ITextComponent.Serializer())
    .create();

  private static final Runnable loop = new RenderLoop();
  private static final Thread loopThread = new Thread(loop);
  private static ResourceLocation loc;

  protected static NativeImage img;
  protected static boolean hasRefreshed = true;

  public static long MESSAGE_ID = 0;

  public OverlayRenderer() {
    loopThread.start();
  }

  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public void hidePersistentChat(RenderGameOverlayEvent.Pre ev) {
    if (ev.getType() == RenderGameOverlayEvent.ElementType.CHAT)
      ev.setCanceled(true);
  }

  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public void onChatReceive(ClientChatReceivedEvent ev) {
    final ITextComponent msg = ev.getMessage();
    final JsonObject obj = (JsonObject) JsonParser.parseString(gson.toJson(msg));
    obj.remove("translate");
    obj.remove("with");

    obj.addProperty("translation", msg.getFormattedText());
    obj.addProperty("id", MESSAGE_ID);

    final String output = gson.toJson(obj);

    System.out.println(output);

    System.out.println(msg.getFormattedText());

    System.out.println(ev.getType().toString());

    Overlay.server.send(output);

    MESSAGE_ID += 1;
  }

  @SubscribeEvent(priority = EventPriority.LOWEST)
  public void renderOverlay(RenderGameOverlayEvent.Post ev) {
    if (img == null) return;
    if (hasRefreshed || loc == null) {
      mc.getTextureManager().deleteTexture(loc);

      loc = mc
        .getTextureManager()
        .getDynamicTextureLocation(
          "overlay-poc-img",
          new DynamicTexture(img)
        );
      hasRefreshed = false;
    }

    mc.getProfiler().startSection("overlay-poc");
    final int width = (int)(mc.mainWindow.getScaledWidth() * 0.75);
    final int height = mc.mainWindow.getScaledHeight() / 2;
    GlStateManager.pushMatrix();

    GlStateManager.enableBlend();
    GlStateManager.disableDepthTest();

    GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
    GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

    mc.getTextureManager().bindTexture(loc);

    blit(0, mc.mainWindow.getScaledHeight() - (int)(height * 1.37), 0f, 0f, width, height, width, height);
    GlStateManager.popMatrix();

    GlStateManager.disableBlend();
    GlStateManager.enableDepthTest();

    GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);

    mc.getTextureManager().bindTexture(new ResourceLocation("minecraft", "textures/gui/icons.png"));

    mc.getProfiler().endSection();
  }
}

class RenderLoop implements Runnable {
  public void run() {
    while (true) {
      byte[] reply = Overlay.client.recv(0);
      final InputStream in = new ByteArrayInputStream(reply);

      try {
        OverlayRenderer.img = NativeImage.read(in);
      } catch (IOException e) {
        e.printStackTrace();
      }

      OverlayRenderer.hasRefreshed = true;
    }
  }
}
