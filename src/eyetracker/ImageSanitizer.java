package eyetracker;

import java.util.ArrayList;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

public class ImageSanitizer {
    static void convertFrame(Mat frame){
        Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2GRAY);
    }
    
    static ArrayList<Mat> detectEyes(Mat frame, Mat leftEyeMat, Mat rightEyeMat){
        CascadeClassifier faceClassifier = new CascadeClassifier();
        faceClassifier.load("C:\\Users\\om\\Desktop\\github_repos\\EyeTracking\\src\\opencv_java\\haarcascade_frontalface_default.xml");

        CascadeClassifier eyeClassifier = new CascadeClassifier();
        eyeClassifier.load("C:\\Users\\om\\Desktop\\github_repos\\EyeTracking\\src\\opencv_java\\haarcascade_eye_tree_eyeglasses.xml");

        MatOfRect faceDetections = new MatOfRect();
        faceClassifier.detectMultiScale(frame, faceDetections);

        Rect rect = faceDetections.toArray()[0];

        Rect R = new Rect(rect.y, rect.x, rect.y + rect.height, rect.x + rect.width); //Create a rect 
        Mat ROI = new Mat(frame, R);
        
        MatOfRect eyesDetections = new MatOfRect();
        eyeClassifier.detectMultiScale(ROI, eyesDetections);
        
        Rect leftEye = null;
        Rect rightEye = null;
        
        for (Rect r : eyesDetections.toArray()) {
            if (r.y + r.height < rect.height / 2) {
                Imgproc.rectangle(
                        ROI, // where to draw the box
                        new Point(r.x, r.y), // bottom left
                        new Point(r.x + r.width, r.y + r.height), // top right
                        new Scalar(0, 0, 255),
                        3 // RGB colour
                );
                float eyecenter = r.x + r.width / 2;
                if (eyecenter < rect.width * 0.5) {
                    leftEye = new Rect(r.x, r.y, r.width, r.height);
                    leftEyeMat = new Mat(ROI, leftEye);
                } else {
                    rightEye = new Rect(r.x, r.y, r.width, r.height);
                    rightEyeMat = new Mat(ROI, rightEye);
                }
            }
        }
        ROI.release();
        ArrayList<Mat> eyes = new ArrayList<Mat>();
        eyes.add(leftEyeMat);
        eyes.add(rightEyeMat);
        return eyes;
    }
    
    
}
