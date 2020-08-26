package live.moonmoon.launcher.overlaypoc;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

@Mod(ModInfo.MODID)
public class Overlay {
  protected static ZContext ctx;
  protected static ZMQ.Socket client;
  protected static ZMQ.Socket server;

  public Overlay() {
    FMLJavaModLoadingContext.get().getModEventBus().addListener(this::init);
  }

  /**
   * This is the second initialization event. Register custom recipes
   */
  public void init(final FMLClientSetupEvent event) {
    System.out.println("Overlay Proof of Concept is starting...");

    ctx = new ZContext();
    System.out.println("Connecting to Electron server");

    client = ctx.createSocket(SocketType.PULL);
    client.connect("tcp://127.0.0.1:27015");

    server = ctx.createSocket(SocketType.PUB);
    server.bind("tcp://127.0.0.1:27016");

    MinecraftForge.EVENT_BUS.register(new OverlayRenderer());
  }
}
