import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static ServerSocket servSoc;
    public static ConnectWithCompanionApp first_screen;
    public static JFrame frame;

    public static void main(String[] args) throws Exception {
        frame = new JFrame("EyeTracking");
        frame.setUndecorated(true);

        first_screen = new ConnectWithCompanionApp();

        frame.setContentPane(first_screen.panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);

        startServer();

        frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
    }

    public static void startServer() throws Exception{
        servSoc = new ServerSocket(0);
        first_screen.setPortNo(servSoc.getLocalPort());
        first_screen.setIPAddress(InetAddress.getLocalHost().getHostAddress());

        Socket soc = servSoc.accept();
        InputStream is = soc.getInputStream();

        boolean isStopped = false;

        while(!isStopped){
            int size = is.read();

            System.out.println(size);

            byte[] img = new byte[size];
            is.read(img, 0, size);
        }

        soc.close();
        servSoc.close();
    }

}