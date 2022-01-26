package geekbrains.cloud;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.UUID;

public class Handler implements Runnable, Closeable {
    private final Socket socket;

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

                        FileOutputStream fileInput = new FileOutputStream(fileUUID.toString());

                        long readBytes = 0;
                        while (readBytes < totalBytes) {
                            byte[] buffer = is.readNBytes(8190);
                            readBytes += buffer.length;
                            if(buffer.length > 0) {
                                fileInput.write(buffer);
                            }
                        }
                        os.writeInt(MessageType.TEXT.getRawValue());
                        os.writeUTF("File sent: " + filename);
                    }
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void close() throws IOException {
        socket.close();
    }
}
