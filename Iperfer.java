import java.io.*;
import java.net.*;

public class Iperfer {

    private static final int BUFFER_SIZE = 1000;

    public static void main(String[] args) {
        if (args.length < 1) {
            errorArgs();
        }

        try {
            if (args[0].equals("-c")) {
                runClient(args);
            } else if (args[0].equals("-s")) {
                runServer(args);
            } else {
                errorArgs();
            }
        } catch (Exception e) {
            // Any unexpected error → treat as argument error per spec
            errorArgs();
        }
    }

    /* ================= CLIENT ================= */

    private static void runClient(String[] args) throws Exception {
        if (args.length != 7 ||
            !args[1].equals("-h") ||
            !args[3].equals("-p") ||
            !args[5].equals("-t")) {
            errorArgs();
        }

        String hostname = args[2];
        int port = Integer.parseInt(args[4]);
        int time = Integer.parseInt(args[6]);

        checkPort(port);

        Socket socket = new Socket(hostname, port);
        OutputStream out = socket.getOutputStream();

        byte[] buffer = new byte[BUFFER_SIZE];
        long bytesSent = 0;

        long startTime = System.currentTimeMillis();
        long endTime = startTime + time * 1000L;

        while (System.currentTimeMillis() < endTime) {
            out.write(buffer);
            bytesSent += BUFFER_SIZE;
        }

        socket.close();

        printResult("sent", bytesSent, time);
    }

    /* ================= SERVER ================= */

    private static void runServer(String[] args) throws Exception {
        if (args.length != 3 || !args[1].equals("-p")) {
            errorArgs();
        }

        int port = Integer.parseInt(args[2]);
        checkPort(port);

        ServerSocket serverSocket = new ServerSocket(port);
        Socket clientSocket = serverSocket.accept();

        InputStream in = clientSocket.getInputStream();
        byte[] buffer = new byte[BUFFER_SIZE];

        long bytesReceived = 0;
        long startTime = 0;
        long endTime = 0;

        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            if (startTime == 0) {
                startTime = System.currentTimeMillis();
            }
            bytesReceived += bytesRead;
            endTime = System.currentTimeMillis();
        }

        clientSocket.close();
        serverSocket.close();

        double elapsedSeconds = (endTime - startTime) / 1000.0;
        printResult("received", bytesReceived, elapsedSeconds);
    }

    /* ================= HELPERS ================= */

    private static void printResult(String label, long bytes, double seconds) {
        double kb = bytes / 1000.0;
        double mbps = (bytes * 8.0) / (seconds * 1_000_000);

        System.out.printf(
            "%s=%.0f KB rate=%.3f Mbps%n",
            label, kb, mbps
        );
    }

    private static void checkPort(int port) {
        if (port < 1024 || port > 65535) {
            System.out.println("Error: port number must be in the range 1024 to 65535");
            System.exit(1);
        }
    }

    private static void errorArgs() {
        System.out.println("Error: missing or additional arguments");
        System.exit(1);
    }
}
