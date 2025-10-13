package me.junioraww.tails.utils;

import com.google.gson.Gson;
import me.junioraww.tails.Main;
import me.junioraww.tails.network.Request;
import me.junioraww.tails.network.Response;
import org.bukkit.Bukkit;

import javax.net.ssl.*;
import java.io.*;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TLSClient {
    private SSLSocket socket;
    private BufferedReader in;
    private PrintWriter out;
    private final Gson gson = new Gson();

    // Очередь для long-polling ответов
    private final BlockingQueue<String> responseQueue = new LinkedBlockingQueue<>();

    // TODO поддержка нескольких address (для round-robin или fallback'а)

    public void init() {
        try {
            String key = Main.getPlugin().getConfig().getString("ca-key");
            String[] address = Main.getPlugin().getConfig().getString("address").split(":");
            String host = address[0];
            int port = Integer.parseInt(address[1]);

            // Клиентское хранилище (ключ + сертификат)
            KeyStore ks = KeyStore.getInstance("PKCS12");
            try (FileInputStream fis = new FileInputStream("client.p12")) {
                ks.load(fis, key.toCharArray());
            }
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, key.toCharArray());

            // Доверяем CA
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Certificate caCert;
            try (FileInputStream fis = new FileInputStream("ca.crt")) {
                caCert = cf.generateCertificate(fis);
            }
            KeyStore ts = KeyStore.getInstance(KeyStore.getDefaultType());
            ts.load(null, null);
            ts.setCertificateEntry("ca", caCert);

            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(ts);

            // SSLContext
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

            SSLSocketFactory factory = ctx.getSocketFactory();
            socket = (SSLSocket) factory.createSocket(host, port);
            socket.setSoTimeout(0);

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Поток чтения
            Thread readerThread = new Thread(this::readLoop);
            readerThread.setDaemon(true);
            readerThread.start();

            // Поток отправки сообщений (пинг/логика)
            Thread writerThread = new Thread(this::writeLoop);
            writerThread.setDaemon(true);
            writerThread.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        if (socket.isConnected()) {
            try {
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void readLoop() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                if ("PING".equals(line)) {
                    send("PONG");
                } else if ("PONG".equals(line)) {

                } else {
                    responseQueue.offer(line);
                }
            }
        } catch (IOException e) {
            System.err.println("Read thread exception: " + e.getMessage());
        } finally {
            close();
        }
    }

    private void writeLoop() {
        // TODO do something?
        /*try {
            while (!socket.isClosed()) {
                send("PING"); // пинг каждые 2 секунды
                Thread.sleep(2000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            close();
        }*/
    }

    private synchronized void send(String msg) {
        if (out != null) {
            out.println(msg);
            out.flush();
        }
    }

    private synchronized void close() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Response sendRequest(Request request) throws InterruptedException {
        send(request.toString());

        // Блокирует поток до ответа
        String responseStr = responseQueue.take();

        return Response.fromString(responseStr);
    }
}