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
public class compareResults {

    public static void main(String[] args) throws FileNotFoundException {
        Scanner in1 = new Scanner(new FileReader("/home/megaterik/programming/gsoc/fdeb/FDEB/debugFolder/0.txt"));
        Scanner in2 = new Scanner(new FileReader("/home/megaterik/programming/gsoc/fdeb/FDEB/debugFolder/1.txt"));
        double total = 0;
        double diff = 0;
        while (in1.hasNextDouble()) {
            Double v1 = in1.nextDouble();
            Double v2 = in2.nextDouble();
            //System.err.println(v1 + " " + v2);
            total += Math.abs(v1) + Math.abs(v2);
            diff += Math.abs(v1 - v2);
        }
        System.err.println(total + " " + diff + " " + diff / total);
    }
}
