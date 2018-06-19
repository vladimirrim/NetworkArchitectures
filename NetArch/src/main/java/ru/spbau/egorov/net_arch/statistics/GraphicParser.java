package ru.spbau.egorov.net_arch.statistics;


import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class GraphicParser {
    public static void main(String[] args) {
        Path nonblocking[] = new Path[3];
        Path multi[] = new Path[3];
        Path multiWithWrite[] = new Path[3];
        for(int i=0;i<3;i++){
            nonblocking[i] = Paths.get(args[i]);
            multi[i] = Paths.get(args[i+3]);
            multiWithWrite[i] = Paths.get(args[i+6]);
        }
        try {
            Scanner dis1[] = new Scanner[3];
            Scanner dis2[] = new Scanner[3];
            Scanner dis3[] = new Scanner[3];
            for(int i=0;i<3;i++){
                dis1[i] = new Scanner(Files.newInputStream(nonblocking[i]));
                dis2[i] = new Scanner(Files.newInputStream(multi[i]));
                dis3[i] = new Scanner(Files.newInputStream(multiWithWrite[i]));
            }
            for(int i=0;i<3;i++) {
                dis1[i].nextLine();
                dis2[i].nextLine();
                dis3[i].nextLine();
            }
            int delaySize,countSize,clientsSize;

            delaySize = dis1[0].nextInt();
            countSize = dis1[1].nextInt();
            clientsSize = dis1[2].nextInt();

            for(int i=0;i<3;i++) {
                dis2[i].nextInt();
                dis3[i].nextInt();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
