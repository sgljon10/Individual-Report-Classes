import java.io.*;
import java.util.Random;
import java.util.Arrays;
import java.util.stream.*; 

// 2D tracking of a high energy muon through iron with no magnetic field
class Positron_Tracking
{
    static BufferedReader keyboard = new BufferedReader (new InputStreamReader(System.in)) ;
    static PrintWriter screen = new PrintWriter( System.out, true);
    static Random randGen = new Random();    

    static final double muonmass = 105.6583745; //MeV
    static final double c = 299792458;
    static final double muonmass_ERROR = 0.0000024;
    static final double magnetic_field = 1.45;
    static final double radius = 7.112;   
    static final double magic_momentum = 29.3; // lorenz factor for things to cancel out
    static final double muon_momentum = Math.sqrt(Math.pow(muonmass,2)*(Math.pow(magic_momentum,2)-1)); //muon momnetum that will be measured, all others will be noise
    static final double muon_momentum_ERROR = (muonmass_ERROR/muonmass)*muon_momentum; //Error from mass error only
    static final double muon_energy = magic_momentum*muonmass;
    static final double mean_lifetime = 2.1969811E-6;
    static final double circumference = 2*Math.PI*radius;
    static final double speed = c*Math.sqrt(1-(1/Math.pow(magic_momentum,2)));
    static final double positron_mass = 0.5109989461; //MeV   
    static final double positron_charge = 1.6021766208E-19;

    static double finalX;
    static double finalY;      
    static double [] [] detector_positions = new double [2] [928]; // where [0][] is x and [1][] is y  , this is for each of the 9 detectors    
    static double [] predicted_positron_X;
    static double [] predicted_positron_Y;
    static int nmax = 100000;
    static double [] [] Hit = new double [2] [50000];
    static double [] [] HitEnd = new double [2] [9];
    static double [] positron_X = new double [nmax];
    static double [] positron_Y = new double [nmax];
    static double [] time = new double [10];
    static double [] hit_angle = new double [9];
    static int [] Max_P = new int [9];
    static double [] a_predicted_angle = new double [36];
    static double [] mid_point = new double [2];
    static double [] path_centre = new double [2];
    static double [][] every_path_centre = new double [2][36];
    static boolean hit1;
    static boolean hit2;
    static int H = 0;       
    static double [][][] HitMid = new double [9] [2] [50000];
    static int P = 0;    
    static int F = 0;
    static int s = 0;

    private static void WriteToCircle() throws IOException {
        FileWriter file = new FileWriter("circle.csv");  
        PrintWriter outputFile = new PrintWriter("circle.csv");         
        for (int n = 0; n < 1000; n++) {             
            outputFile.println((n+1) + "," + radius*Math.cos(2*n*Math.PI/999) + "," + radius*Math.sin(2*n*Math.PI/999));
        }    
        outputFile.close();
        screen.println("Data written to disk in file " + "circle.csv");
        return;
    }

    private static double [] [] Detector_hit() {       
        double [] a = {radius-0.085,radius-0.085,radius-0.125,radius-0.125,radius-0.125,radius-0.165,radius-0.165,radius-0.165,radius-0.165}; 
        double [] detector_angle = new double [3];        
        double detector_angle1 = (15*Math.PI)/180;
        double []  width = {0.08,0.08,0.12,0.12,0.12,0.16,0.16,0.16,0.16};

        for (int n = 0; n <= 1; n++) {
            double real_angle = detector_angle1 - ((0.09*n)/a [n]);
            for (int i = 0; i <= 63; i++) {
                detector_positions [0] [i+64*n] = ((a [n])+((i)*(width [n]/63)))*Math.cos(real_angle);
                detector_positions [1] [i+64*n] = ((a [n])+((i)*(width [n]/63)))*Math.sin(real_angle);
            }
        }        
        for (int n = 2; n <= 4; n++) {
            double real_angle = detector_angle1 - ((0.09*n)/a [n]);
            for (int i = 0; i <= 95; i++) {
                detector_positions [0] [i+64*2+96*(n-2)] = ((a [n])+((i)*(width [n]/95)))*Math.cos(real_angle);
                detector_positions [1] [i+64*2+96*(n-2)] = ((a [n])+((i)*(width [n]/95)))*Math.sin(real_angle);
            }
        }  
        for (int n = 5; n <= 8; n++) {
            double real_angle = detector_angle1 - ((0.09*n)/a [n]);
            for (int i = 0; i <= 127; i++) {
                detector_positions [0] [i+64*2+96*3+128*(n-5)] = ((a [n])+((i)*(width [n]/127)))*Math.cos(real_angle);
                detector_positions [1] [i+64*2+96*3+128*(n-5)] = ((a [n])+((i)*(width [n]/127)))*Math.sin(real_angle);
            }
        }  
        return detector_positions;
    }    

    private static void WriteToPositron() throws IOException {
        FileWriter file = new FileWriter("positron.csv");  
        PrintWriter outputFile = new PrintWriter("positron.csv");

        for (int n = 0; n < nmax; n++) {             
            outputFile.println((n+1) + "," + positron_X [n] + "," + positron_Y [n]);
        }

        outputFile.close();
        screen.println("Data written to disk in file " + "positron.csv");
        return;
    }

    private static void WriteToDetectors() throws IOException {
        FileWriter file = new FileWriter("detector.csv");  
        PrintWriter outputFile = new PrintWriter("detector.csv");

        Detector_hit();

        for (int n = 0; n < 928; n++) {             
            outputFile.println((n+1) + "," + detector_positions [0][n] + "," + detector_positions [1][n]);
        }

        outputFile.close();
        screen.println("Data written to disk in file " + "detector.csv");
        return;
    }

    private static void WriteToHits() throws IOException {
        FileWriter file = new FileWriter("HITS.csv");  
        PrintWriter outputFile = new PrintWriter("HITS.csv");

        outputFile.println(1 + "," + "x Axis" + "," + "y Axis" + "," + "Time since creation");
        outputFile.println(1 + "," + HitEnd [0][0] + "," + HitEnd [1][0] + "," + time [0]);
        for (int n=1; n < 9; n++) {
            outputFile.println((n+1) + "," + HitEnd [0][n] + "," + HitEnd [1][n] + "," + time [n]); 
        }

        outputFile.close();
        screen.println("Data written to disk in file " + "HITS.csv");
        return;
    }

    public static void main (String [] args) throws IOException
    {

        double angle = 22;
        double average_predicted_angle = 0;
        finalX = radius*Math.cos((angle*Math.PI)/180);
        finalY = radius*Math.sin((angle*Math.PI)/180); //decided to decay at 16 degrees

        double positron_energy = muon_energy*0.7; //for now, this will be chnaged
        double positron_momentum = Math.sqrt(Math.pow(positron_energy,2) - Math.pow(positron_mass,2));
        double positron_lorentz = positron_energy/positron_mass;
        double positron_path_radius = ((positron_momentum*positron_charge*1E6)/c)/(magnetic_field*positron_charge); //without use of lorentz as lorentz is very high
        double positron_radiusdifference = radius - positron_path_radius;
        double positron_pathcentre_x = positron_radiusdifference*Math.cos((angle*Math.PI)/180);
        double positron_pathcentre_y = positron_radiusdifference*Math.sin((angle*Math.PI)/180);

        Detector_hit();       

    
        for (int n=0; n < nmax; n++) {
            positron_X [n] = positron_path_radius*Math.cos((((double)n*15/nmax)*Math.PI)/180) + positron_pathcentre_x;
            positron_Y [n] = positron_path_radius*Math.sin((((double)n*15/nmax)*Math.PI)/180) + positron_pathcentre_y;
            for (int L = 0; L < 928; L++) {            
                if (positron_X [n] <= (detector_positions [0] [L] + 0.0006) && positron_X [n] >= (detector_positions [0] [L] - 0.0006)){
                    hit1 = true;                             
                }
                else {
                    hit1 = false;
                }
                if (positron_Y [n] <= (detector_positions [1] [L] + 0.0006) && positron_Y [n] >= (detector_positions [1] [L] - 0.0006)){
                    hit2 = true;                                
                }
                else {
                    hit2 = false;
                }
                if (hit1 == true && hit2 == true){
                    Hit [0][H] = positron_X [n];
                    Hit [1][H] = positron_Y [n];                
                    if (L < 64){
                        s = 0;
                    }
                    if (L >= 64 && L <= 127){
                        s = 1;
                    }
                    if (L >= 128 && L <= 223){
                        s = 2;
                    }
                    if (L >= 224 && L <= 319){
                        s = 3;
                    }
                    if (L >= 320 && L <= 415){
                        s = 4;
                    }
                    if (L >= 416 && L <= 543){
                        s = 5;
                    }
                    if (L >= 544 && L <= 671){
                        s = 6;
                    }
                    if (L >= 672 && L <= 799){
                        s = 7;
                    }
                    if (L >= 800){
                        s = 8;
                    }
                    if (H == 0){
                        HitMid [s][0][0] = Hit [0][0];
                        HitMid [s][1][0] = Hit [1][0];
                    }
                    if (H > 0){                    
                        if (Hit [0] [H-1] != Hit [0][H] && Hit [1][H-1] != Hit [1][H]) {  
                            if (F != s){                         
                                P = 0;                         
                            }
                            F = s;
                            HitMid [s][0][P] = Hit [0][H];
                            HitMid [s][1][P] = Hit [1][H];
                            if (Max_P [s] < P){
                                Max_P [s] = P;                         
                            }               

                            P++;                     
                        }
                    }                
                    H++;                
                }            
            }            
        }

        double [][] sum = new double [2][9];    
        for (int n=0; n < 9; n++) {
            sum [0][n] = 0;
            sum [1][n] = 0;
            for (int i=0; i < Max_P [n]; i++) {
                sum [0][n] = sum [0][n] + HitMid [n][0][i];
                sum [1][n] = sum [1][n] + HitMid [n][1][i];
            }        
            HitEnd [0][n] = sum [0][n] / Max_P [n];
            HitEnd [1][n] = sum [1][n] / Max_P [n];
        }

        screen.println("The positron energy is " + (float)positron_energy + " MeV");

        //using energy and time that the detectors detected I can work back and find the time and place it decayed from
        //positron_pathradius can be found from the energy and so the angle at which it decayed can be found

        int l = 0;     
        int o = 0;
        int p = 0;
        double total = 0;
        for (int n=0; n < 36; n++) {
            if (n <= 7){          
                o = n;
            }
            if (n >= 8 && n <= 14) {
                l = 1;
                o = n - 8;
                p = 8;
            }
            if (n >= 15 && n <= 20) {
                l = 2;
                o = n - 15;
                p = 15;
            }      
            if (n >= 21 && n <= 25) {
                l = 3;
                o = n - 21;
                p = 21;
            }    
            if (n >= 26 && n <= 29) {
                l = 4;
                o = n - 26;
                p = 26;
            }   
            if (n >= 30 && n <= 32) {
                l = 5;
                o = n - 30;
                p = 30;
            }    
            if (n >= 33 && n <= 34) {
                l = 6;
                o = n - 33;
                p = 33;
            }         
            if (n == 35) {
                l = 7;
                o = n - 35;
                p = 35;
            } 

            double AB = Math.sqrt(Math.pow(HitEnd [0] [0+l] - HitEnd [0] [8-o],2) + Math.pow(HitEnd [1] [0+l] - HitEnd [1] [8-o],2));                
            double a_mid_point = Math.sqrt(Math.pow(positron_path_radius,2) - Math.pow(AB/2,2));
            double m_mid_point = (HitEnd [0] [0+l] - HitEnd [0] [8-o])/(HitEnd [1] [8-o] - HitEnd [1] [0+l]);

            mid_point [0] = (HitEnd [0] [0+l] + HitEnd [0] [8-o])/2;
            mid_point [1] = (HitEnd [1] [0+l] + HitEnd [1] [8-o])/2;    

            path_centre [0] = mid_point [0] - a_mid_point / Math.sqrt(1 + Math.pow(m_mid_point,2));
            path_centre [1] = mid_point [1] - (m_mid_point*a_mid_point) / Math.sqrt(1 + Math.pow(m_mid_point,2));

            every_path_centre [0][o+p] = path_centre [0];
            every_path_centre [1][o+p] = path_centre [1];

            a_predicted_angle [o+p] = Math.atan2(path_centre [1]/positron_radiusdifference,path_centre [0]/positron_radiusdifference)*180/Math.PI;

        }

        average_predicted_angle = DoubleStream.of(a_predicted_angle).sum()/36; 
        double average_every_path_centre_x = DoubleStream.of(every_path_centre [0]).sum()/36; 
        double average_every_path_centre_y = DoubleStream.of(every_path_centre [1]).sum()/36; 
        screen.println("The angle the muon decayed at is predicted to be " + (float)average_predicted_angle + ("\u00B0"));

        double positron_speed = c*(positron_momentum/positron_energy);

        for (int n=0; n < 9; n++) {
            hit_angle [n] = Math.acos((HitEnd[0][n] - positron_pathcentre_x)/positron_path_radius);
            double distance_from_decay = Math.abs( positron_path_radius * (hit_angle [n] - 22) );
            time [n] = distance_from_decay / positron_speed;
        }

        WriteToDetectors();
        WriteToPositron();
        WriteToHits();
        WriteToCircle();
    }

}