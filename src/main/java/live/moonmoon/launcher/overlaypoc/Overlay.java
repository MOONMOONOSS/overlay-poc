package live.moonmoon.launcher.overlaypoc;

import org.lwjgl.Sys;
import org.zeromq.SocketType;
import org.zeromq.ZContext;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.Mod;
import org.zeromq.ZMQ;

@Mod(
  modid = Overlay.MOD_ID,
  name = Overlay.MOD_NAME,
  version = Overlay.VERSION
)
public class Overlay {
  public static final String MOD_ID = "overlaypoc";
  public static final String MOD_NAME = "OverlayPoc";
  public static final String VERSION = "1.0-SNAPSHOT";

  @Mod.Instance(MOD_ID)
  public static Overlay INSTANCE;

  private ZContext ctx;
  private ZMQ.Socket socket;

  /**
   * This is the second initialization event. Register custom recipes
   */
  @Mod.EventHandler
  public void init(FMLInitializationEvent event) {
    System.out.println("Overlay Proof of Concept is starting...");

    ctx = new ZContext();
    System.out.println("Connecting to Electron server");

    socket = ctx.createSocket(SocketType.PULL);
    socket.connect("tcp://127.0.0.1:27015");

    byte[] reply = socket.recv(0);
    System.out.printf("Received %s%n", new String(reply));
  }
}
