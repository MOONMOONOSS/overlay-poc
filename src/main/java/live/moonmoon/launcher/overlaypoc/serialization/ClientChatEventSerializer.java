package live.moonmoon.launcher.overlaypoc.serialization;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatEvent;

import java.lang.reflect.Type;

public class ClientChatEventSerializer implements JsonSerializer<ClientChatEvent> {
  public JsonElement serialize(ClientChatEvent src, Type typeOfSrc, JsonSerializationContext ctx) {
    JsonObject obj = new JsonObject();
    obj.addProperty("msg", src.getMessage());
    obj.addProperty("player", Minecraft.getMinecraft().player.getDisplayNameString());

    return obj;
  }
}
