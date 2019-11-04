//package eyetracker;
//
//import org.opencv.core.*;
//import org.opencv.core.Point;
//import org.opencv.imgcodecs.Imgcodecs;
//import org.opencv.imgproc.Imgproc;
//import org.opencv.objdetect.CascadeClassifier;
//
//import java.util.ArrayList;
//
//class Pair<T1, T2> {
//
//    public T1 first;
//    public T2 second;
//
//    public Pair(T1 first, T2 second) {
//        this.first = first;
//        this.second = second;
//    }
//}
//
//abstract class OurData {
//
//    static Pair<Double, Double> top = new Pair<>(51.25, 35.0);
//    static Pair<Double, Double> bottom = new Pair<>(51.25, 65.66);
//
//    static Pair<Double, Double> top_left = new Pair<>(29.25, 45.75);
//    static Pair<Double, Double> top_right = new Pair<>(73.5, 42.5);
//
//    static Pair<Double, Double> bottom_left = new Pair<>(29.25, 61.10);
//    static Pair<Double, Double> bottom_right = new Pair<>(73.5, 58.70);
//
//    static Pair<Double, Double> left_corner = new Pair<>(15.75, 60.25);
//    static Pair<Double, Double> right_corner = new Pair<>(83.75, 46.75);
//}
//
//public class CombinedXYPlotDemo1 {
//
//    static int no = 10;
//
//    public static void main(final String[] args) throws Exception {
//
//        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
//
//        Mat frame;
//
//        frame = Imgcodecs.imread("C:\\Users\\dhanesh\\Desktop\\projects\\eyetracker\\pic_ip" + CombinedXYPlotDemo1.no + ".jpg");
//
//        CascadeClassifier faceClassifier = new CascadeClassifier();
//        faceClassifier.load("C:\\Users\\dhanesh\\Downloads\\opencv\\build\\etc\\haarcascades\\haarcascade_frontalface_default.xml");
//
//        CascadeClassifier eyeClassifier = new CascadeClassifier();
//        eyeClassifier.load("C:\\Users\\dhanesh\\Downloads\\opencv\\build\\etc\\haarcascades\\haarcascade_eye_tree_eyeglasses.xml");
//
//        Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2GRAY);
//
//        MatOfRect faceDetections = new MatOfRect();
//        faceClassifier.detectMultiScale(frame, faceDetections);
//
//        Rect rect = faceDetections.toArray()[0];
//
//        Rect R = new Rect(rect.y, rect.x, rect.y + rect.height, rect.x + rect.width); //Create a rect 
//        Mat ROI = new Mat(frame, R);
//
//        MatOfRect eyesDetections = new MatOfRect();
//        eyeClassifier.detectMultiScale(ROI, eyesDetections);
//        Rect leftEye = null;
//        Mat leftEyeMat = null;
//        Rect rightEye = null;
//        Mat rightEyeMat = null;
//        for (Rect r : eyesDetections.toArray()) {
//            if (r.y + r.height < rect.height / 2) {
//                Imgproc.rectangle(
//                        ROI, // where to draw the box
//                        new Point(r.x, r.y), // bottom left
//                        new Point(r.x + r.width, r.y + r.height), // top right
//                        new Scalar(0, 0, 255),
//                        3 // RGB colour
//                );
//                float eyecenter = r.x + r.width / 2;  
//                if (eyecenter < rect.width * 0.5){
//                    leftEye = new Rect(r.x, r.y, r.width, r.height);
//                    leftEyeMat = new Mat(ROI, leftEye);
//                }
//                else{
//                    rightEye = new Rect(r.x, r.y, r.width, r.height);
//                    rightEyeMat = new Mat(ROI, rightEye);
//                }
//            }
//        }
//        Imgcodecs.imwrite("C:\\Users\\dhanesh\\Desktop\\projects\\eye tracking\\pic_op" + CombinedXYPlotDemo1.no + "_left.jpg", leftEyeMat);
//        Imgcodecs.imwrite("C:\\Users\\dhanesh\\Desktop\\projects\\eye tracking\\pic_op" + CombinedXYPlotDemo1.no + "_right.jpg", rightEyeMat);
//
//        Mat eye = rightEyeMat;
////        eye = eye.colRange(rect.x + 1, rect.x + rect.width);
//        Mat bin = eye.clone();
//        Mat norm = eye.clone();
//        ROI.release();
//        frame.release();
//
//        ArrayList< Pair<Double, Double>> bounding_points = new ArrayList<>();
//        bounding_points.add(new Pair<>(OurData.left_corner.first / 100 * eye.rows(), OurData.left_corner.second / 100 * eye.cols()));
//        bounding_points.add(new Pair<>(OurData.top_left.first / 100 * eye.rows(), OurData.top_left.second / 100 * eye.cols()));
//        bounding_points.add(new Pair<>(OurData.top.first / 100 * eye.rows(), OurData.top.second / 100 * eye.cols()));
//        bounding_points.add(new Pair<>(OurData.top_right.first / 100 * eye.rows(), OurData.top_right.second / 100 * eye.cols()));
//        bounding_points.add(new Pair<>(OurData.bottom_left.first / 100 * eye.rows(), OurData.bottom_left.second / 100 * eye.cols()));
//        bounding_points.add(new Pair<>(OurData.bottom.first / 100 * eye.rows(), OurData.bottom.second / 100 * eye.cols()));
//        bounding_points.add(new Pair<>(OurData.bottom_right.first / 100 * eye.rows(), OurData.bottom_right.second / 100 * eye.cols()));
//        bounding_points.add(new Pair<>(OurData.right_corner.first / 100 * eye.rows(), OurData.right_corner.second / 100 * eye.cols()));
//
//        int index = 0;
//        int prev_up = 0;
//        int prev_down = 0;
//        int x = bounding_points.get(0).first.intValue();
//        double y_cut_start = bounding_points.get(0).second;
//        double y_cut_end = y_cut_start + 1;
//
//        //This counts the points with the same group intensity (there are 51 intensity groups, since 255/5, i.e. we rae grouping 5 consequent values into one)
//        int[] count = new int[52];
//
//        for (int i = 0; i < 52; i++) {
//            count[i] = 0;
//        }
//
//        while (index != 7) {
//            index++;
//            int up_index = index;
//            int down_index = index + 3;
//            if (index == 4) {
//                index = 7;
//                up_index = 7;
//                down_index = 7;
//            }
//
//            double length1 = bounding_points.get(up_index).first - bounding_points.get(prev_up).first;
//            double y_d1 = bounding_points.get(up_index).second - bounding_points.get(prev_up).second;
//
//            double length2 = bounding_points.get(down_index).first - bounding_points.get(prev_down).first;
//            double y_d2 = bounding_points.get(down_index).second - bounding_points.get(prev_down).second;
//
//            double y_incr1 = y_d1 / length1;
//            double y_incr2 = y_d2 / length2;
//
//            for (; x <= bounding_points.get(up_index).first.intValue(); x++) {
//                for (double y_start = y_cut_start; y_start <= y_cut_end; y_start++) {
//                    int intensity = (int) eye.get(((int) y_start), x)[0];
//
//                    count[intensity / 5]++;
//                }
//
//                y_cut_start += y_incr1;
//                y_cut_end += y_incr2;
//            }
//
//            prev_up = up_index;
//            prev_down = down_index;
//        }
//
//        int up_sum = 0, down_sum = 0;
//
//        for (int i = 0; i < 52; i++) {
//            down_sum += count[i];
//        }
//
//        int i = 0;
//
//        while (up_sum <= down_sum) {
//            up_sum += count[i];
//            down_sum -= count[i];
//            i++;
//        }
//
//        Imgproc.threshold(bin, bin, (i) * 5, 255, Imgproc.THRESH_BINARY);
//        Imgcodecs.imwrite("C:\\Users\\dhanesh\\Desktop\\projects\\eye tracking\\pic_op" + CombinedXYPlotDemo1.no + "_eye_bin.jpg", bin);
//
//        Adjustment test = new Adjustment(bounding_points);
//        test.apply(bin);
//
//        int shape_size = eye.rows() / 100 + 2;
//        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(shape_size, shape_size));
//        Imgproc.erode(bin, bin, element);
//        Imgproc.dilate(bin, bin, element);
//
//        Iris iris_test = new Iris(bounding_points, shape_size / 2);
//        Circle iris = iris_test.detectWhileCalibration(bin, eye);
//
//        for (int k = 0; k < 8; k++) {
//            norm.put(test.list.get(k).second.intValue(), test.list.get(k).first.intValue(), 0);
//        }
//
//        int x_length = bounding_points.get(7).first.intValue() - bounding_points.get(0).first.intValue();
//        System.out.println("X length: "+x_length+", Point x: "+ (iris.center.x-bounding_points.get(0).first.intValue()));
//        
//        int y_length = bounding_points.get(5).second.intValue() - bounding_points.get(2).second.intValue();
//        System.out.println("Y length: "+y_length+", Point y: "+ (iris.center.y-bounding_points.get(2).second.intValue()));
//        
//        Circle upCircle = null;
//        Circle downCircle = null;
//        Circle leftCircle = null;
//        Circle rightCircle = null;
//        
//        Point upDeviation = new Point(iris.center.x-upCircle.center.x,iris.center.y-upCircle.center.y);
//        Point downDeviation = new Point(downCircle.center.x - iris.center.x, downCircle.center.y - iris.center.y);
//        Point leftDeviation = new Point(iris.center.x-leftCircle.center.x,iris.center.y-leftCircle.center.y);
//        Point rightDeviation = new Point(rightCircle.center.x - iris.center.x, rightCircle.center.y - iris.center.y);
//        
//        
//        Imgcodecs.imwrite("C:\\Users\\dhanesh\\Desktop\\projects\\eye tracking\\pic_op" + CombinedXYPlotDemo1.no + "_eye_norm.jpg", norm);
//        Imgcodecs.imwrite("C:\\Users\\dhanesh\\Desktop\\projects\\eye tracking\\pic_op" + CombinedXYPlotDemo1.no + "_eye_binary.jpg", bin);
//        Imgcodecs.imwrite("C:\\Users\\dhanesh\\Desktop\\projects\\eye tracking\\pic_op" + CombinedXYPlotDemo1.no + "_eye.jpg", eye);
//    }
//
//    public static ArrayList<ArrayList<Integer>> getBlackList(int row, Mat m) {
//        ArrayList<ArrayList<Integer>> list = new ArrayList<>();
//
//        for (int i = 1; i < m.cols(); i++) {
//            if (m.get(row, i)[0] == 0) {
//                list.add(new ArrayList<>());
//                list.get(list.size() - 1).add(i);
//
//                do {
//                    i++;
//                } while (i < m.cols() && m.get(row, i)[0] != 255);
//
//                list.get(list.size() - 1).add(i - 1);
//            }
//        }
//
//        return list;
//    }
//
//    public static ArrayList<Integer> getCorneaSegment(int c_x, int c_y, Mat m) {
//        ArrayList<ArrayList<Integer>> list = getBlackList(c_y, m);
//
//        for (int i = 0; i < list.size(); i++) {
//            if (c_x >= list.get(i).get(0) && c_x <= list.get(i).get(1)) {
//                return list.get(i);
//            }
//        }
//
//        int min = -1;
//        int index = 0;
//
//        for (int i = 0; i < list.size(); i++) {
//            if (c_x < list.get(i).get(0)) {
//                int temp = list.get(i).get(0) - c_x;
//                if (min > temp || min == -1) {
//                    min = temp;
//                    index = i;
//                }
//            } else {
//                int temp = c_x - list.get(i).get(1);
//                if (min > temp || min == -1) {
//                    min = temp;
//                    index = i;
//                }
//            }
//        }
//
//        return list.get(index);
//    }
//
//    public static ArrayList<Integer> getCorneaCenterAndRadius(int row, int start_x, int end_x, Mat m) {
//        ArrayList<ArrayList<Integer>> ans = new ArrayList<>();
//
//        for (int start1 = start_x; start1 <= end_x; start1++) {
//            m.put(row, start1, 128);
//        }
//        Imgcodecs.imwrite("C:\\Users\\dhanesh\\Desktop\\projects\\eye tracking\\pic_op" + CombinedXYPlotDemo1.no + "_eye_bin.jpg", m);
//
//        for (int i = 0; i < 2; i++) {
//
//            int temp_y = row;
//            int prev_segment_length = end_x - start_x;
//            int prev_pixel_offset = prev_segment_length * 10 / 100;
//            int prev_start_x = start_x;
//            int prev_end_x = end_x;
//
//            temp_y = (i == 1) ? (temp_y - 1) : temp_y + 1;
//
//            while (true) {
//                ArrayList<Integer> bounds = getCorneaSegment((prev_start_x + prev_end_x) / 2, temp_y, m);
//
//                if (bounds.get(1) - bounds.get(0) > prev_segment_length + prev_pixel_offset
//                        || bounds.get(1) - bounds.get(0) < prev_segment_length - prev_pixel_offset
//                        || bounds.get(1) < prev_start_x
//                        || bounds.get(0) > prev_end_x
//                        || (bounds.get(0) > prev_start_x && bounds.get(1) > prev_end_x)
//                        || (bounds.get(0) < prev_start_x && bounds.get(1) < prev_end_x)) {
//                    break;
//                }
//
//                prev_start_x = bounds.get(0);
//                prev_end_x = bounds.get(1);
//                prev_segment_length = prev_end_x - prev_start_x;
//                prev_pixel_offset = prev_segment_length * 10 / 100;
//
//                for (int start = prev_start_x; start <= prev_end_x; start++) {
//                    m.put(temp_y, start, 128);
//                }
//
//                Imgcodecs.imwrite("C:\\Users\\dhanesh\\Desktop\\projects\\eye tracking\\pic_op" + CombinedXYPlotDemo1.no + "_eye_bin.jpg", m);
//
//                temp_y = (i == 1) ? (temp_y - 1) : temp_y + 1;
//            }
//
//            ans.add(new ArrayList<>());
//            ans.get(ans.size() - 1).add(prev_start_x);
//            ans.get(ans.size() - 1).add(prev_end_x);
//        }
//
//        return null;
//    }
//
//}
