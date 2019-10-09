package ru.nsu.g.a.lyamin.tcpfilereciver.client;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client
{
    private FileInputStream source;
    private File f;
    private DataOutputStream output;
    private DataInputStream input;

    public static void main(String[] args)
    {
        if(args.length < 3)
        {
            System.err.println("Error, has no params");
            return;
        }

        int serverPort = Integer.parseInt(args[1]);

        InetAddress serverAddress;
        try { serverAddress = InetAddress.getByName(args[0]); }
        catch (UnknownHostException e) { System.err.println(e.getMessage()); return; }
        String filepath = args[2];

        Client c = new Client(serverAddress, serverPort, filepath);
    }

    private Client(InetAddress serverAddress, int serverPort, String filepath) {


        f = new File(filepath);
        try (Socket socket = new Socket(serverAddress, serverPort);
             FileInputStream s = new FileInputStream(f))
        {
            source = s;

            System.out.println("Connected to " + socket.getInetAddress() + ":" + socket.getPort());

            output = new DataOutputStream(socket.getOutputStream());
            input = new DataInputStream(socket.getInputStream());

            sendFileName();
            sendFileSize();
            sendFile();

            System.out.println(recvFinishMess());
        }
        catch (IOException|NullPointerException e)
        {
            System.out.println(e.getMessage());
        }

    }

    private String getFileName()
    {
        return f.getName();
    }

    private void sendFileName() throws IOException
    {
        String fileName = getFileName();
        output.writeInt(fileName.length());
        output.write(fileName.getBytes());
    }

    private void sendFileSize() throws IOException
    {
        output.writeLong(f.length());
    }

    private void sendFile() throws IOException
    {
        byte[] buffer = new byte[1024];
        int length;
        while((length = source.read(buffer, 0, 1024)) > 0){
            output.write(buffer, 0, length);
        }
    }

    private String recvFinishMess() throws IOException
    {
        int finishMessLength = input.readInt();
        byte[] finishMessByte = input.readNBytes(finishMessLength);
        return new String(finishMessByte);
    }

}
