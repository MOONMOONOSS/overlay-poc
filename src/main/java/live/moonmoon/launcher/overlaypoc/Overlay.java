package live.moonmoon.launcher.overlaypoc;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

@Mod(
  modid = Overlay.MOD_ID,
  name = Overlay.MOD_NAME,
  version = Overlay.VERSION,
  clientSideOnly = true,
  acceptedMinecraftVersions = "[1.12]"
)
public class Overlay {
  public static final String MOD_ID = "overlaypoc";
  public static final String MOD_NAME = "OverlayPoc";
  public static final String VERSION = "1.0-SNAPSHOT";

  @Mod.Instance(MOD_ID)
  public static Overlay INSTANCE;

  protected static ZContext ctx;
  protected static ZMQ.Socket client;
  protected static ZMQ.Socket server;

  /**
   * This is the second initialization event. Register custom recipes
   */
  @Mod.EventHandler
  public void init(FMLInitializationEvent event) {
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
