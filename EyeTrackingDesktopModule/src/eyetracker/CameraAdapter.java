package eyetracker;

import org.opencv.core.Mat;

abstract class CameraAdapter {
    abstract Mat obtainFrames();
}
