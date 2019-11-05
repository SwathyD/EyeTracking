package eyetracker;

import org.opencv.core.Mat;

import java.util.ArrayList;

public class Adjustment {
    public ArrayList<Pair<Double, Double>> list;
    private boolean[] flag_per_point = new boolean[8];
    private static final int[] middle = {2, 5};
    private static final int[] corners = {0, 7};
    private static final int[] rest = {1, 3, 4, 6};

    public Adjustment(ArrayList<Pair<Double, Double>> l){
        this.list = l;
    }

//    0 - left corner
//    1 - top left
//    2 - top
//    3 - top right
//    4 - bottom left
//    5 - bottom
//    6 - bottom right
//    7 - right corner

    void adjustMiddle(Mat image){
//        top and bottom

        for(int i = 0; i < middle.length; i++){
            if(flag_per_point[middle[i]]) continue;


            boolean flag = false;
            for(int j = 0; j < 2; j++){
                flag = !flag;

                for(int iter = 0; iter < 3 && iter > -3; iter = flag ? iter+1 : iter-1){
                    int intensity1 = (int) image.get((int) (list.get(middle[i]).second + iter), list.get(middle[i]).first.intValue() )[0];
                    int intensity2 = (int) image.get((int) (list.get(middle[i]).second + iter + (flag ? 1 : -1) ), list.get(middle[i]).first.intValue() )[0];

                    if(intensity1 != intensity2){
                        list.set(middle[i], new Pair<>(list.get(middle[i]).first, list.get(middle[i]).second + iter));
                        flag_per_point[middle[i]] = true;
                        break;
                    }

                }

            }
        }
    }

    void adjustCorners(Mat image){

        for(int index = 0; index < corners.length; index++) {
            ArrayList<Pair<Integer, Integer>> intensity_changes = new ArrayList<>();

            int bound_index = index == 0 ? 1 : 3;
            for (int row = list.get(bound_index).second.intValue(); row <= list.get(bound_index+3).second; row++) {
                int col = list.get(bound_index).first.intValue();

//                if (image.get(row, col)[0] == 0) continue;

                while (col < image.cols() && col >= 0 && image.get(row, col)[0] != 0) {
                    if(corners[index] == 0) col--;
                    else col++;
                }

                if (col >= 0 && col < image.cols()) intensity_changes.add(new Pair<>(col, row));
            }

            int min_max = 0;
            for (int i = 1; i < intensity_changes.size(); i++) {
                if(corners[index] == 0)
                {
                    if(intensity_changes.get(i).first < intensity_changes.get(min_max).first) min_max = i;
                }else{
                    if(intensity_changes.get(i).first > intensity_changes.get(min_max).first) min_max = i;
                }
            }

            if(intensity_changes.size() != 0)
                list.set(corners[index], new Pair<>(intensity_changes.get(min_max).first.doubleValue(), intensity_changes.get(min_max).second.doubleValue()));
        }

    }

    Pair<Double, Double> getIntersection(Pair<Double, Double> p1, Pair<Double, Double> p2,
                                           Pair<Double, Double> p3)
    {
        double m1 = (p2.second - p1.second)/(double)(p2.first - p1.first);
        double c1 = (p1.second - m1 * p1.first);

        int y = (int) (m1 * p3.first + c1);

        return new Pair<>(p3.first, (double) y);
    }

    void correctRemaining(){
        Pair<Double, Double> tmp = getIntersection(list.get(0), list.get(2), list.get(1));

        if(list.get(1).second >= tmp.second){
            list.set(1, new Pair<>(tmp.first, tmp.second - 2));
        }

        tmp = getIntersection(list.get(0), list.get(5), list.get(4));

        if(list.get(4).second <= tmp.second){
            list.set(4, new Pair<>(tmp.first, tmp.second + 2));
        }

        tmp = getIntersection(list.get(7), list.get(2), list.get(3));

        if(list.get(3).second >= tmp.second){
            list.set(3, new Pair<>(tmp.first, tmp.second - 2));
        }

        tmp = getIntersection(list.get(7), list.get(5), list.get(6));

        if(list.get(6).second <= tmp.second){
            list.set(6, new Pair<>(tmp.first, tmp.second + 2));
        }
    }

    void adjustRemaining(Mat image){
        // top-left top-right bottom-left bottom-right

        for(int i = 0; i < rest.length; i++)
        {
            if(flag_per_point[rest[i]]){
                continue;
            }

            boolean flag = false;
            int up_index = 0;
            int down_index = 0;

            for(int k = 0; true; k = (flag) ? (++down_index) : (--up_index)){
                if(k == down_index)
                {
                    if( i < 2 ){
                        if( k + list.get(rest[i]).second > list.get(0).second || k + list.get(rest[i]).second > list.get(7).second){
                            break;
                        }
                    }
                    else
                    {
                        if(k + list.get(rest[i]).second > list.get(5).second)
                        {
                            break;
                        }
                    }
                }
                else
                {
                    if( i >= 2 ){
                        if( k + list.get(rest[i]).second < list.get(0).second || k + list.get(rest[i]).second < list.get(7).second){
                            break;
                        }
                    }
                    else
                    {
                        if(k + list.get(rest[i]).second < list.get(2).second)
                        {
                            break;
                        }
                    }
                }
                int intensity1 = (int) image.get( list.get(rest[i]).second.intValue() + k, list.get(rest[i]).first.intValue())[0];
                int intensity2 = (int) image.get( list.get(rest[i]).second.intValue() + k + (flag?(-1):(1)), list.get(rest[i]).first.intValue())[0];

                if(intensity1 != intensity2){
                    list.set( rest[i] , new Pair<>(list.get(rest[i]).first , list.get(rest[i]).second + k));
                    flag_per_point[rest[i]] = true;
                    break;
                }
                flag = !flag;
            }

        }
}



    void apply(Mat image){
        adjustMiddle(image);
        adjustCorners(image);
        adjustRemaining(image);
        correctRemaining();
    }
}
