package ru.spbau.egorov.net_arch.statistics;


import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

public class GraphicParser extends Application {
    private static String[] args;

    public static void main(String[] args) {
        GraphicParser.args = args;
        launch(args);
    }

    private static void showChart(ArrayList<Integer> xValues, ArrayList<Integer> yValues1, ArrayList<Integer> yValues2, ArrayList<Integer> yValues3, String label, String xLabel, String yLabel) {
        Stage stage = new Stage();
        stage.setTitle(label);
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel(xLabel);
        yAxis.setLabel(yLabel);
        final LineChart<Number, Number> lineChart =
                new LineChart<>(xAxis, yAxis);

        lineChart.setTitle(label);

        XYChart.Series series1 = new XYChart.Series();
        series1.setName("Nonblocking");
        XYChart.Series series2 = new XYChart.Series();
        series2.setName("MultiThread");
        XYChart.Series series3 = new XYChart.Series();
        series3.setName("MultiThreadWithExtraWriteThread");

        for (int i = 0; i < xValues.size(); i++) {
            series1.getData().add(new XYChart.Data<>(xValues.get(i), yValues1.get(i)));
            series2.getData().add(new XYChart.Data<>(xValues.get(i), yValues2.get(i)));
            series3.getData().add(new XYChart.Data<>(xValues.get(i), yValues3.get(i)));
        }

        Scene scene = new Scene(lineChart, 800, 600);
        lineChart.getData().addAll(series1, series2, series3);
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void start(Stage primaryStage) {
        Path nonblocking[] = new Path[3];
        Path multi[] = new Path[3];
        Path multiWithWrite[] = new Path[3];
        for (int i = 0; i < 3; i++) {
            nonblocking[i] = Paths.get(args[i]);
            multi[i] = Paths.get(args[i + 3]);
            multiWithWrite[i] = Paths.get(args[i + 6]);
        }
        try {
            Scanner dis[][] = new Scanner[3][3];
            for (int i = 0; i < 3; i++) {
                dis[0][i] = new Scanner(Files.newInputStream(nonblocking[i]));
                dis[1][i] = new Scanner(Files.newInputStream(multi[i]));
                dis[2][i] = new Scanner(Files.newInputStream(multiWithWrite[i]));
            }
            for (int j = 0; j < 10; j++)
                for (int i = 0; i < 3; i++) {
                    for (int arch = 0; arch < 3; arch++) {
                        dis[arch][i].nextLine();
                    }
                }
            int delaySize, arraySize, clientsSize;

            delaySize = dis[0][0].nextInt();
            arraySize = dis[0][1].nextInt();
            clientsSize = dis[0][2].nextInt();

            for (int i = 0; i < 3; i++) {
                dis[1][i].nextInt();
                dis[2][i].nextInt();
            }
            {
                ArrayList<Integer> xValues[] = new ArrayList[3];
                ArrayList<Integer> yValues[][] = new ArrayList[3][3];
                for (int i = 0; i < 3; i++) {
                    xValues[i] = new ArrayList<>();
                    for (int j = 0; j < 3; j++) {
                        yValues[i][j] = new ArrayList<>();
                        yValues[i][j] = new ArrayList<>();
                        yValues[i][j] = new ArrayList<>();
                    }
                }
                for (int i = 0; i < delaySize; i++) {
                    for (int arch = 0; arch < 3; arch++) {
                        xValues[arch].add(dis[arch][0].nextInt());
                        for (int metric = 0; metric < 3; metric++)
                            yValues[arch][metric].add(dis[arch][0].nextInt());
                    }
                }
                showChart(xValues[0], yValues[0][0], yValues[1][0], yValues[2][0], "Metric: Sorting time", "number of clients", "time, ms");
                showChart(xValues[0], yValues[0][1], yValues[1][1], yValues[2][1], "Metric: Server processing time", "delay time", "time, ms");
                showChart(xValues[0], yValues[0][2], yValues[1][2], yValues[2][2], "Metric: Client processing time", "delay time", "time, ms");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
