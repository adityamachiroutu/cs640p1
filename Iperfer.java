import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Iperfer {

    private static final int MINIMUM_PORT = 1024;
    private static final int MAXIMUM_PORT = 65535;
    private static final int CHUNK_SIZE = 1000;
    private static final int BITS_PER_BYTE = 8;
    private static final double BYTES_PER_KILOBYTE = 1000.0;
    private static final double KILOBYTES_PER_MEGABYTE = 1000.0;
    private static final double MILLISECONDS_PER_SECOND = 1000.0;

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Error: missing or additional arguments");
            return;
        }

        String mode = args[0];

        if (mode.equals("-c")) {
            runClient(args);
        } else if (mode.equals("-s")) {
            runServer(args);
        } else {
            System.out.println("Error: missing or additional arguments");
        }
    }

    private static void runClient(String[] args) {
        if (args.length != 7) {
            System.out.println("Error: missing or additional arguments");
            return;
        }

        if (!args[1].equals("-h") || !args[3].equals("-p") || !args[5].equals("-t")) {
            System.out.println("Error: missing or additional arguments");
            return;
        }

        String serverHostname = args[2];
        int serverPort = parsePort(args[4]);
        if (serverPort == -1) {
            return;
        }

        int durationInSeconds = Integer.parseInt(args[6]);

        sendData(serverHostname, serverPort, durationInSeconds);
    }

    private static void runServer(String[] args) {
        if (args.length != 3) {
            System.out.println("Error: missing or additional arguments");
            return;
        }

        if (!args[1].equals("-p")) {
            System.out.println("Error: missing or additional arguments");
            return;
        }

        int listenPort = parsePort(args[2]);
        if (listenPort == -1) {
            return;
        }

        receiveData(listenPort);
    }

    private static int parsePort(String portString) {
        int port = Integer.parseInt(portString);

        if (port < MINIMUM_PORT || port > MAXIMUM_PORT) {
            System.out.println("Error: port number must be in the range 1024 to 65535");
            return -1;
        }

        return port;
    }

    private static void sendData(String serverHostname, int serverPort, int durationInSeconds) {
        try {
            Socket clientSocket = new Socket(serverHostname, serverPort);
            OutputStream outputStream = clientSocket.getOutputStream();

            byte[] dataBuffer = new byte[CHUNK_SIZE];
            long totalBytesSent = 0;

            long startTimeMillis = System.currentTimeMillis();
            long endTimeMillis = startTimeMillis + (durationInSeconds * (long) MILLISECONDS_PER_SECOND);

            while (System.currentTimeMillis() < endTimeMillis) {
                outputStream.write(dataBuffer);
                totalBytesSent += CHUNK_SIZE;
            }

            outputStream.close();
            clientSocket.close();

            long actualElapsedMillis = System.currentTimeMillis() - startTimeMillis;
            double actualElapsedSeconds = actualElapsedMillis / MILLISECONDS_PER_SECOND;

            long totalKilobytesSent = (long) (totalBytesSent / BYTES_PER_KILOBYTE);
            double totalMegabits = (totalBytesSent * BITS_PER_BYTE) / (BYTES_PER_KILOBYTE * KILOBYTES_PER_MEGABYTE);
            double rateInMegabitsPerSecond = totalMegabits / actualElapsedSeconds;

            System.out.println("sent=" + totalKilobytesSent + " KB rate=" + String.format("%.3f", rateInMegabitsPerSecond) + " Mbps");

        } catch (Exception exception) {
            System.out.println("Error: could not connect to server");
        }
    }

    private static void receiveData(int listenPort) {
        try {
            ServerSocket serverSocket = new ServerSocket(listenPort);
            Socket clientConnection = serverSocket.accept();
            InputStream inputStream = clientConnection.getInputStream();

            byte[] receiveBuffer = new byte[CHUNK_SIZE];
            long totalBytesReceived = 0;
            int bytesReadThisChunk;

            long firstByteTimeMillis = 0;
            boolean hasReceivedFirstByte = false;

            while ((bytesReadThisChunk = inputStream.read(receiveBuffer)) != -1) {
                if (!hasReceivedFirstByte) {
                    firstByteTimeMillis = System.currentTimeMillis();
                    hasReceivedFirstByte = true;
                }
                totalBytesReceived += bytesReadThisChunk;
            }

            long lastByteTimeMillis = System.currentTimeMillis();

            inputStream.close();
            clientConnection.close();
            serverSocket.close();

            double elapsedSeconds = (lastByteTimeMillis - firstByteTimeMillis) / MILLISECONDS_PER_SECOND;

            long totalKilobytesReceived = (long) (totalBytesReceived / BYTES_PER_KILOBYTE);
            double totalMegabits = (totalBytesReceived * BITS_PER_BYTE) / (BYTES_PER_KILOBYTE * KILOBYTES_PER_MEGABYTE);
            double rateInMegabitsPerSecond = totalMegabits / elapsedSeconds;

            System.out.println("received=" + totalKilobytesReceived + " KB rate=" + String.format("%.3f", rateInMegabitsPerSecond) + " Mbps");

        } catch (Exception exception) {
            System.out.println("Error: could not start server");
        }
    }
}
