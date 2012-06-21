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
        Scanner in1 = new Scanner(new FileReader("debugSimple.txt"));
        Scanner in2 = new Scanner(new FileReader("debugThreaded.txt"));
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
        System.out.println("average " + averageDifference);
        System.out.println("Sum " + fullSum);
    }
    
}
