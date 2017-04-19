import java.io.*;
import java.util.Random;
import java.util.Arrays; 

class Accelerator
{
    static BufferedReader keyboard = new BufferedReader (new InputStreamReader(System.in)) ;
    static PrintWriter screen = new PrintWriter( System.out, true);
    static Random randGen = new Random(); 
    
    static final int number_of_muons = 2; 
    static int nmax = 200;
     
    static final double muonmass = 105.6583745; //MeV
    static final double c = 299792458;    
    static final double magnetic_field = 1.45;
    static final double radius = 7.112; 
    static final double magic_momentum = 29.3; // lorenz factor for things to cancel out
    static final double muon_momentum = Math.sqrt(Math.pow(muonmass,2)*(Math.pow(magic_momentum,2)-1)); //muon momnetum that will be measured, all others will be noise    
    static final double muon_energy = magic_momentum*muonmass;
    static final double mean_lifetime = 2.1969811E-6;
    static final double circumference = 2*Math.PI*radius;
    static final double speed = c*Math.sqrt(1-(1/Math.pow(magic_momentum,2)));
    static final double electron_mass = 0.5109989461; //MeV   
    static final double electron_charge = 1.6021766208E-19; 
    static double [] finalX = new double [number_of_muons];
    static double [] finalY = new double [number_of_muons];
    static double [] finaltime = new double [number_of_muons];
    static double [][] electron_X = new double [number_of_muons] [nmax];
    static double [][] electron_Y = new double [number_of_muons] [nmax]; 
    
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
    
    private static void WriteToDecay() throws IOException {
        FileWriter file = new FileWriter("decay.csv");  
        PrintWriter outputFile = new PrintWriter("decay.csv");         
        for (int n = 0; n < number_of_muons; n++) {             
        outputFile.println((n+1) + "," + finalX [n] + "," + finalY [n] + "," + finaltime [n]);
        }    
       outputFile.close();
       screen.println("Data written to disk in file " + "decay.csv");
       return;
    }
        
    private static void WriteToElectron() throws IOException {
        FileWriter file = new FileWriter("Electron.csv");  
        PrintWriter outputFile = new PrintWriter("Electron.csv");
       for (int i =0; i < number_of_muons; i++) { 
        for (int n = 0; n < nmax; n++) {             
        outputFile.println((n+1) + "," + electron_X [i][n] + "," + electron_Y [i][n]);
        }
       }
       outputFile.close();
       screen.println("Data written to disk in file " + "Electron.csv");
       return;
    }

      public static void main (String [] args) throws IOException
    {
    for (int i =0; i < number_of_muons; i++) {         
        double lifetime = -mean_lifetime*Math.log(randGen.nextDouble());  
        double distance_travelled = speed*lifetime;    
        double loops = distance_travelled/circumference;
        screen.println("The amounts of loops muon " + (i+1) + " did is " + loops);
        finalX [i] = radius*Math.cos(distance_travelled/radius);
        finalY [i] = radius*Math.sin(distance_travelled/radius);
        finaltime [i] = lifetime;
        double electron_energy = muon_energy *0.7; //for now, this will be chnaged
        double electron_momentum = Math.sqrt(Math.pow(electron_energy,2) - Math.pow(electron_mass,2));        
        double electron_path_radius = ((electron_momentum*electron_charge*1E6)/c)/(magnetic_field*electron_charge); //without use of lorentz as lorentz is very high
        double electron_radiusdifference = radius - electron_path_radius;
        double electron_pathcentre_x = electron_radiusdifference*Math.cos(distance_travelled/radius);
        double electron_pathcentre_y = electron_radiusdifference*Math.sin(distance_travelled/radius);       
        for (int n=0; n < nmax; n++) {
            electron_X [i][n] = electron_path_radius*Math.cos((((double)n*360/(double)nmax)*Math.PI)/180) + electron_pathcentre_x;
            electron_Y [i][n] = electron_path_radius*Math.sin((((double)n*360/(double)nmax)*Math.PI)/180) + electron_pathcentre_y;
        }
    }    
    WriteToDecay(); 
    WriteToCircle();
    WriteToElectron();  
    }
    
}