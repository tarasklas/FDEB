/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.debug;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

/**
 *
 * @author megaterik
 */
public class compare {
    
    public static void main(String[] args) throws FileNotFoundException
    {
        diff("debugSimple.txt", "debugBarnesHut.txt");
        diff("debugSimple.txt", "debugWithoutBundling.txt");
    }
    
    public static void diff(String f1, String f2) throws FileNotFoundException
    {
        System.err.println("Difference between " + f1 + " and " + f2);
        Scanner in1 = new Scanner(new FileReader(f1));
        Scanner in2 = new Scanner(new FileReader(f2));
        int total = 0;
        double averageDifference = 0;
        double fullDifference = 0;
        double fullSum = 0;
        while (in1.hasNext())
        {
            double v1 = Double.parseDouble(in1.next());
            double v2 = Double.parseDouble(in2.next());
            fullDifference += Math.abs(v1 - v2);
            fullSum += Math.abs(v1) + Math.abs(v2);
            total++;
        }
        averageDifference = fullDifference / total;
        System.out.println("difference " + fullDifference);
        System.out.println("average " + averageDifference + " " + fullSum / total);
        System.out.println("Sum " + fullSum);
    }
    
}
