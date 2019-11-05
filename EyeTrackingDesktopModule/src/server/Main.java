package server;

import eyetracker.EyeTracker;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static ServerSocket servSoc;
    public static ConnectWithCompanionApp first_screen;
    public static MainScreen main_screen;
    public static JFrame frame;
    private static int rotationAngle;

    public static void main(String[] args) throws Exception {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        frame = new JFrame("EyeTracking");
        frame.setUndecorated(true);

        first_screen = new ConnectWithCompanionApp();
        main_screen = new MainScreen();

        main_screen.rotate90Button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rotationAngle += 90;
                rotationAngle %= 360;
            }
        });

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

        gotoMainScreen();

        InputStream is = soc.getInputStream();
        DataInputStream dis = new DataInputStream(is);

        boolean isStopped = false;

        while(!isStopped){
            int size = dis.readInt();

            byte[] img = new byte[size];
            for(int i = 0; i < size; i++){
                img[i] = dis.readByte();
            }

            ByteArrayInputStream bis = new ByteArrayInputStream(img);
            BufferedImage buffImage = rotate(ImageIO.read(bis), rotationAngle);

            Mat mat = Imgcodecs.imdecode(new MatOfByte(img), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);

            for(int x = 0; x < rotationAngle/90; x++){
                Core.rotate(mat, mat, Core.ROTATE_90_CLOCKWISE);
            }
            EyeTracker eyetracker = new EyeTracker(mat);
            Mat output = eyetracker.getPrediction();

            if(output != null){
//                Imgcodecs.imwrite("C:\\Users\\om\\Desktop\\github_repos\\EyeTracking\\interim\\pic_op.bmp", output);

                MatOfByte buff = new MatOfByte();
                Imgcodecs.imencode(".jpg", output, buff);

                bis = new ByteArrayInputStream(buff.toArray());
                buffImage = ImageIO.read(bis);
            }

            main_screen.outputLabel.setIcon(new ImageIcon(buffImage));
        }

        dis.close();
        soc.close();
        servSoc.close();
    }

    public static BufferedImage rotate(BufferedImage bimg, double angle) {

        int w = bimg.getWidth();
        int h = bimg.getHeight();

        BufferedImage rotated = new BufferedImage(w, h, bimg.getType());
        Graphics2D graphic = rotated.createGraphics();
        graphic.rotate(Math.toRadians(angle), w/2, h/2);
        graphic.drawImage(bimg, null, 0, 0);
        graphic.dispose();
        return rotated;
    }
    public static void gotoMainScreen(){
        frame.setContentPane(main_screen.panel1);
    }
}