package live.moonmoon.launcher.overlaypoc.serialization;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import live.moonmoon.launcher.overlaypoc.OverlayRenderer;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatEvent;

import java.lang.reflect.Type;

public class ClientChatEventSerializer implements JsonSerializer<ClientChatEvent> {
  public JsonElement serialize(ClientChatEvent src, Type typeOfSrc, JsonSerializationContext ctx) {
    JsonObject obj = new JsonObject();
    obj.addProperty("msg", src.getMessage());
    obj.addProperty("player", Minecraft.getMinecraft().player.getDisplayNameString());
    obj.addProperty("id", OverlayRenderer.MESSAGE_ID);

    // Increment the counter after we're done with it.
    OverlayRenderer.MESSAGE_ID += 1;

    return obj;
  }
}
