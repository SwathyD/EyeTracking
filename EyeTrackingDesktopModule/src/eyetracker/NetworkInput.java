package eyetracker;

import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import org.opencv.core.Mat;

public class NetworkInput extends CameraAdapter {
    ServerSocket soc;
    CameraAdapter controller;
    Queue<Mat> frameBuffer = new LinkedList<>();
  
    NetworkInput(){
        storeFrames();
    }

    public void storeFrames(){
        //fetching from the network code overhere
        
        Mat frame = null;
        if(frameBuffer.size() > 5){
            frameBuffer.clear();
        }
        frameBuffer.add(frame);
        
    }
    
    public Mat obtainFrames(){
        return frameBuffer.remove();
    }
}
