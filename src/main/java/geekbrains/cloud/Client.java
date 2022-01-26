package geekbrains.cloud;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;

public class Client {
    Socket socket;
    DataInputStream is;
    DataOutputStream os;
    Function<String, Void> onMessageReceived;

    public Client() {
        try {
            this.socket = new Socket("localhost", 8190);
            is = new DataInputStream(socket.getInputStream());
            os = new DataOutputStream(socket.getOutputStream());

            start();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void start() {
        try {
            var readThread = new Thread(() -> {
                while (true) {
                    try {
                        int rawValue = is.readInt();
                        switch (MessageType.fromRawValue(rawValue)) {
                            case TEXT -> {
                                String msg = is.readUTF();
                                if (onMessageReceived != null) {
                                    onMessageReceived.apply(msg);
                                }
                            }
                            case FILE -> {
                                System.out.println("FILE message type not yet supported");
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            readThread.setDaemon(true);
            readThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String msg) {
        try {
            os.writeInt(MessageType.TEXT.getRawValue());
            os.writeUTF(msg);
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void uploadFile (List<File> files) {
        if (files.size() == 0) return;

        try {
            for(File file : files) {
                String name = file.getName();
                if(name.length() > MessageType.MAX_FILENAME_LENGTH) {
                    throw new Exception("Too long filename");
                }
                os.writeInt(MessageType.FILE.getRawValue());
                os.writeUTF(name);

                FileInputStream fileInput = new FileInputStream(file);

                Path filePath = file.toPath();
                long size = Files.size(filePath);

                if ((int)(size) < Integer.MAX_VALUE) {
                    os.writeLong(size);
                    byte[] buffer = Files.readAllBytes(filePath);
                    while (fileInput.read(buffer) != -1) {
                        os.write(buffer);
                    }
                    os.flush();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
