package eyetracker;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.*;

class Circle{
    Point center;
    float radius;
    
    Circle(){
        center = new Point(0, 0);
        radius = 0;
    }

    Circle(Point center, float radius){
        this.center = center;
        this.radius = radius;
    }
}

public class Iris {

    ArrayList<Pair<Double, Double>> bounding_points;
    int size;

    public Iris( ArrayList<Pair<Double, Double>> bounding_points, int size) {
        this.size = size;
        this.bounding_points = bounding_points;
    }

    Circle detectWhileCalibration(Mat bin_eye, Mat eye) throws Exception {
        int y_start = bounding_points.get(1).second.intValue();
        int index = 1;

        if(bounding_points.get(2).second.intValue() > y_start){
            y_start = bounding_points.get(2).second.intValue();
            index = 2;
        }
        if(bounding_points.get(3).second.intValue() > y_start){
            y_start = bounding_points.get(3).second.intValue();
            index = 3;
        }

        y_start = ((int) findNearestWhite(bin_eye, new Point(bounding_points.get(index).first, y_start), false).y);

        int y_end = bounding_points.get(4).second.intValue();
        index = 4;

        if(bounding_points.get(5).second.intValue() < y_end){
            y_end = bounding_points.get(5).second.intValue();
            index = 5;
        }
        if(bounding_points.get(6).second.intValue() < y_end){
            y_end = bounding_points.get(6).second.intValue();
            index = 6;
        }

        y_end = ((int) findNearestWhite(bin_eye, new Point(bounding_points.get(index).first, y_end), true).y);

        ArrayList<Integer> seq_list = new ArrayList<>();
        ArrayList<Integer> c_x_list = new ArrayList<>();

        for(int row = y_start + size; row <= y_end; row++){
            int col_left = bounding_points.get(1).first.intValue();
            int intensity = ((int) bin_eye.get(row, col_left)[0]);

            while(intensity != 0){
                col_left++;
                intensity = ((int) bin_eye.get(row, col_left)[0]);
            }

            int col_right = bounding_points.get(3).first.intValue();
            intensity = ((int) bin_eye.get(row, col_right)[0]);

            while(intensity != 0){
                col_right--;
                intensity = ((int) bin_eye.get(row, col_right)[0]);
            }

            c_x_list.add(col_left);
            seq_list.add(col_right - col_left);
        }

        int max, min, start, end;
        max = min = seq_list.get(0);
        start = end = 0;
        ArrayList<Integer> max_list = new ArrayList();
        max_list.add(0);
        boolean correct_seq = false;

        for( int i = 1; i < seq_list.size(); i++)
        {
            if(seq_list.get(i) >= min && seq_list.get(i) < max) {}
            else if(seq_list.get(i) == min - 1)
                min--;

            else if(seq_list.get(i) == max)
                max_list.add(i);

            else if(seq_list.get(i) == max + 1)
            {
                max_list.clear();
                max++;
                max_list.add(i);
            }

            else{
                // search the list for value between max and min and then update max and min with new list

                int iter = i+1;

                while(iter < seq_list.size() && !(seq_list.get(iter) >= min && seq_list.get(iter) <= max)){
                    iter++;
                }

                if(iter == seq_list.size()){
                    if(eye.rows()/50*2+2 <= end - start + 1){
                        correct_seq = true;
                        break;
                    }else{
                        end = start = end + 1;
                        max = min = seq_list.get(start);
                        max_list.clear();
                        max_list.add(start);
                    }
                }else{

                    for(int j = end; j <= iter; j++){
                        if(seq_list.get(j) > max) {
                            max_list.clear();
                            max = seq_list.get(j);
                        }
                        else if(seq_list.get(j) < min) min = seq_list.get(j);

                        if(seq_list.get(j) == max){
                            max_list.add(j);
                        }
                    }

                    end = iter;
                    i = iter + 1;
                }
                continue;
            }

            end++;

        }

      //  if(start == end){
        //    throw new Exception("Frame should be dropped");
        //}
        if(!correct_seq)
        {
            max_list.clear();
            max = Collections.max(seq_list);
            for(int i = 0; i < seq_list.size(); i++)
            {
                if(max == seq_list.get(i)){
                    max_list.add(i);
                }
            }
        }

        index = y_start + size + max_list.get(max_list.size()/2 - ((max_list.size()>1) ? 1 : 0));
        int c_x = c_x_list.get(max_list.get(max_list.size()/2 - ((max_list.size()>1) ? 1 : 0)));

        // System.out.println(index + " " + c_x);
        // System.out.println(seq_list);
        // System.out.println(max_list);
        // System.out.println(max_list.size()/2 - ((max_list.size()>1) ? 1 : 0));
        // System.out.println(max);

        Circle circ = new Circle(new Point(c_x+max/2, index), max/2 + 1);
        Imgproc.circle(eye, circ.center, (int)circ.radius, new Scalar(255));
        return circ;
    }

    Point findNearestWhite(Mat image, Point seed, boolean up){
        int x = ((int) seed.x);
        int y = ((int) seed.y);

        if(image.get(y, x)[0] == 255) return seed;

        while((image.get(y, x)[0] != 255) && (y > 0 && y < image.rows())){
            ArrayList<ArrayList<Integer>> list = EyeTracker.getBlackList(y, image);

            int i;
            for(i = 0; i < list.size(); i++){
                if(x >= list.get(i).get(0) && x <= list.get(i).get(1)){
                    break;
                }
            }

            if(i < list.size()) {
                int end = (x - list.get(i).get(0) < list.get(i).get(1) - x) ? 0 : 1;
                end = list.get(i).get(end);

                boolean left = (end > bounding_points.get(0).first) && (end < bounding_points.get(2).first);
                boolean right = (end < bounding_points.get(7).first) && (end > bounding_points.get(2).first);

                if (left || right) {
                    x = end;
                    if(up) y--;
                    else y++;

                    break;
                }
            }

            y = up ? y-1 : y+1;
        }

        return new Point(x, y);
    }

}
