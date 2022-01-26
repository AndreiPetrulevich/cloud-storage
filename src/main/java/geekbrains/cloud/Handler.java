package geekbrains.cloud;

import java.io.*;
import java.net.Socket;
import java.util.Optional;
import java.util.UUID;

public class Handler implements Runnable, Closeable {
    private final Socket socket;
    private static final int SIZE = 2048;

    public Handler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (DataInputStream is = new DataInputStream(socket.getInputStream());
             DataOutputStream os = new DataOutputStream(socket.getOutputStream())) {
            while (true) {
                int rawValue = is.readInt();
                switch (MessageType.fromRawValue(rawValue)) {
                    case TEXT -> {
                        String msg = is.readUTF();
                        System.out.println("Received text message:" + msg);
                        os.writeInt(MessageType.TEXT.getRawValue());
                        os.writeUTF(msg);
                        os.flush();
                    }
                    case FILE -> {
                        String filename = is.readUTF();
                        System.out.println(filename);
                        long totalBytes = is.readLong();

                        UUID fileUUID = UUID.randomUUID();
                        Optional<String> extension = getExtension(filename);

                        FileOutputStream fileInput = new FileOutputStream(fileUUID.toString() + extension.orElse(""));

                        int readBytes = 0;
                        while (readBytes < totalBytes) {
                            int bytesLeft = (int)totalBytes - readBytes;
                            int size = Integer.min(bytesLeft, SIZE);
                            byte[] buffer = is.readNBytes(size);
                            readBytes += buffer.length;

                            fileInput.write(buffer);
                        }

                        os.writeInt(MessageType.TEXT.getRawValue());
                        os.writeUTF("File sent: " + filename);
                        os.flush();
                    }
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public Optional<String> getExtension(String filename) {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(filename.lastIndexOf(".")));
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }
}
