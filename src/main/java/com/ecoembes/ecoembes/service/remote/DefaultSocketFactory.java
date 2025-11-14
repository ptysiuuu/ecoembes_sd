package com.ecoembes.ecoembes.service.remote;

import java.io.IOException;
import java.net.Socket;

public class DefaultSocketFactory implements SocketFactory {
    @Override
    public Socket createSocket(String host, int port) throws IOException {
        return new Socket(host, port);
    }
}
