package ru.spbau.egorov.net_arch.controller;

import javafx.scene.control.TextArea;
import ru.spbau.egorov.net_arch.network.Server;
import ru.spbau.egorov.net_arch.network.ServerFactory;

public class ServerMenuController {
    public TextArea hostName;
    public TextArea port;

    public void runNonblocking() {

        Server server = ServerFactory.newNonBlockingServer(hostName.getText(), Integer.parseInt(port.getText()));
        new Thread(server::start).start();
    }

    public void runMultiThread() {

        Server server = ServerFactory.newMultiThreadServer(hostName.getText(), Integer.parseInt(port.getText()));
        new Thread(server::start).start();
    }

    public void rumMultiThreadWithWriteThread() {

        Server server = ServerFactory.newMultiThreadWriteThreadServer(hostName.getText(), Integer.parseInt(port.getText()));
        new Thread(server::start).start();
    }
}
