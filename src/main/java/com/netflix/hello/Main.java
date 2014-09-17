package com.netflix.hello;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.netflix.nfplugin.gov.GovernatorPluginManager;
import java.io.InputStream;
import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {

  private static final HttpHandler HEALTHCHECK = (HttpExchange exchange) -> {
    try (InputStream in = exchange.getRequestBody()) {
      copyToDevNull(in);
    }
    exchange.sendResponseHeaders(200, -1);
  };

  private static void copyToDevNull(InputStream in) throws IOException {
    byte[] buffer = new byte[4096];
    while (in.read(buffer) != -1);
  }

  private HttpServer server;

  public Main(InetSocketAddress addr) throws IOException {
    server = HttpServer.create(addr, 0);
    server.createContext("/healthcheck", HEALTHCHECK);
  }

  public void start() throws IOException {
    GovernatorPluginManager.getInstance().start();
    server.start();
  }

  public void shutdown() {
    server.stop(1);
    GovernatorPluginManager.getInstance().shutdown();
  }

  public static void main(String[] args) throws IOException {
    int port = Integer.parseInt(System.getProperty("netflix.appinfo.port", "7101"));
    final Main app = new Main(new InetSocketAddress(port));
    Runtime.getRuntime().addShutdownHook(new Thread(() -> app.shutdown()));
    app.start();
  }
}
