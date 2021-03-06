package ru.spbau.egorov.net_arch.controller;

import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import ru.spbau.egorov.net_arch.network.Client;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class ClientMenuController extends Controller{
    public TextArea arrSize;
    public TextArea timeDelay;
    public TextArea hostName;
    public TextArea changingParameter;
    public TextArea minVal;
    public TextArea clientsCount;
    public TextArea archType;
    public TextArea port;
    public TextArea increment;
    public TextArea maxVal;
    public TextArea requestsCount;

    private int serverProcessingTime = 0;
    private int clientProcessingTime = 0;
    private int sortingTime = 0;
    private int iteration;

    public void runClient() {
        ArrayList<Integer> xValues = new ArrayList<>();
        ArrayList<Integer> yValues1 = new ArrayList<>();
        ArrayList<Integer> yValues2 = new ArrayList<>();
        ArrayList<Integer> yValues3 = new ArrayList<>();
        int clientsNumber = Integer.parseInt(clientsCount.getText());
        int requestsNumber = Integer.parseInt(requestsCount.getText());
        for (iteration = Integer.parseInt(minVal.getText()); iteration <= Integer.parseInt(maxVal.getText()); iteration += Integer.parseInt(increment.getText())) {
            ArrayList<Thread> threads = new ArrayList<>();

            int arraySize = Integer.parseInt(arrSize.getText());


            for (int i = 0; i < (changingParameter.getText().equals("CLIENTS_COUNT") ? iteration : clientsNumber); i++) {
                Client client = new Client(hostName.getText(), Integer.parseInt(port.getText()));
                Thread thread = new Thread(
                        (() -> {
                            client.sendArray(changingParameter.getText().equals("ARRAY_SIZE") ? iteration : arraySize,
                                    requestsNumber,
                                    changingParameter.getText().equals("REQUEST_DELAY") ? iteration : Integer.parseInt(timeDelay.getText()));
                            clientProcessingTime += client.getClientProcessingTime();
                            serverProcessingTime += client.getServerProcessingTime();
                            sortingTime += client.getServerSortingTime();
                        }));
                threads.add(thread);
            }
            for (Thread thread : threads) {
                thread.start();
            }
            for (Thread thread : threads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    showInfoDialog(e.getLocalizedMessage());
                    return;
                }
            }
            xValues.add(iteration);

            yValues1.add(sortingTime / (changingParameter.getText().equals("CLIENTS_COUNT") ? iteration : clientsNumber) / requestsNumber);
            yValues3.add(clientProcessingTime / (changingParameter.getText().equals("CLIENTS_COUNT") ? iteration : clientsNumber) / requestsNumber);
            yValues2.add(serverProcessingTime / (changingParameter.getText().equals("CLIENTS_COUNT") ? iteration : clientsNumber) / requestsNumber);
        }
        String label = "", yLabel = "Time, ms", xLabel = "";

        if (archType.getText().equals("NONBLOCKING")) {
            label = "Nonblocking architecture";
        }
        if (archType.getText().equals("MULTI")) {
            label = "MultiThread architecture";
        }
        if (archType.getText().equals("MULTI_WITH_WRITE")) {
            label = "MultiThreadWithWriteThread architecture";
        }
        if (changingParameter.getText().equals("CLIENTS_COUNT")) {
            xLabel = "Clients number";
        }
        if (changingParameter.getText().equals("REQUEST_DELAY")) {
            xLabel = "Request delay";
        }
        if (changingParameter.getText().equals("ARRAY_SIZE")) {
            xLabel = "array size";
        }

        showChart(xValues, yValues1, yValues2, yValues3, label, xLabel, yLabel);
        Path path = Paths.get("./statistics.txt");
        try {
            DataOutputStream dos = new DataOutputStream(Files.newOutputStream(path));
            String sep = System.getProperty("line.separator");
            dos.write(("ClientsCount: " + clientsCount.getText() + sep).getBytes());
            dos.write(("ArraySize: " + arrSize.getText() + sep).getBytes());
            dos.write(("ArchType: " + archType.getText() + sep).getBytes());
            dos.write(("changingParameter: " + changingParameter.getText() + sep).getBytes());
            dos.write(("timeDelay: " + timeDelay.getText() + sep).getBytes());
            dos.write(("requestCount: " + requestsCount.getText() + sep).getBytes());
            dos.write(("minVal: " + minVal.getText() + sep).getBytes());
            dos.write(("maxVal: " + maxVal.getText() + sep).getBytes());
            dos.write(("increment: " + increment.getText() + sep).getBytes());
            dos.write(("xValues\tSORTING_TIME\tSERVER_TIME\tCLIENT_TIME" + sep).getBytes());
            dos.write((String.valueOf(xValues.size()) + sep).getBytes());
            for (int i = 0; i < xValues.size(); i++) {
                dos.write((String.valueOf(xValues.get(i)) + "\t" +
                        String.valueOf(yValues1.get(i)) + "\t" +
                        String.valueOf(yValues2.get(i)) + "\t" + String.valueOf(yValues3.get(i)) + sep).getBytes());
            }
        } catch (IOException e) {
            showInfoDialog(e.getLocalizedMessage());
        }
    }

    private void showChart(ArrayList<Integer> xValues, ArrayList<Integer> yValues1, ArrayList<Integer> yValues2, ArrayList<Integer> yValues3, String label, String xLabel, String yLabel) {
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
        series1.setName("Sorting time");
        XYChart.Series series2 = new XYChart.Series();
        series2.setName("Server processing time");
        XYChart.Series series3 = new XYChart.Series();
        series3.setName("Client processing time");

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
}
