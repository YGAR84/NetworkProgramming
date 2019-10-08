package ru.nsu.g.a.lyamin.tcpfilereciver.client;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

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

        try
        {
            f = new File(filepath);
            source = new FileInputStream(f);
        }
        catch (FileNotFoundException e)
        {
            System.out.println("Error while open file" + e.getMessage());
            return;
        }

        Socket socket;

        try
        {
            socket = new Socket(serverAddress, serverPort);
        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());
            return;
        }
        System.out.println("Connected");

        try
        {
            output = new DataOutputStream(socket.getOutputStream());
            input = new DataInputStream(socket.getInputStream());
        }
        catch (IOException e)
        {
            System.out.println("Error while open streams" + e.getMessage());
            return;
        }

        try{ sendFileName(); }
        catch(IOException e)
        {
            System.out.println("Error while send file name" + e.getMessage());
            return;
        }

        try{ sendFileSize(); }
        catch(IOException e)
        {
            System.out.println("Error while send file name" + e.getMessage());
        }

        try { sendFile(); }
        catch(IOException e)
        {
            System.out.println("Error while send file name" + e.getMessage());
        }

        try { System.out.println(recvFinishMess()); }
        catch (IOException e)
        {
            System.out.println("Error while recive final message" + e.getMessage());
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
        System.out.println(fileName.length());
        byte[] name = fileName.getBytes();
        System.out.println("send FILE NAME");
        output.write(name);
    }

    private void sendFileSize() throws IOException
    {
        long fileLength = f.length();
        output.writeLong(fileLength);
    }

    private void sendFile() throws IOException
    {
        byte[] buffer = new byte[1024];
        int length;
        while((length = source.read(buffer)) != 0){
            output.write(buffer, 0, length);
        }
    }

    private String recvFinishMess() throws IOException
    {
        int finishMessLength = input.readInt();
        byte[] finishMessByte = input.readNBytes(finishMessLength);
        return Arrays.toString(finishMessByte);
    }

}
