package ru.nsu.g.a.lyamin.tcpfilereciver.client;

import ru.nsu.g.a.lyamin.tcpfilereciver.LengthDecoder;
import ru.nsu.g.a.lyamin.tcpfilereciver.server.ByteReader;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;

public class Client
{
    private int serverPort;
    private InetAddress serverAddress;
    private String filepath;
    private FileInputStream source;
    private File f;
    private OutputStream out;
    private InputStream in;

    private void Init(String[] args) throws Exception
    {
        if(args.length < 3)
        {
            throw new Exception("Error, has no params");
        }
        serverPort = Integer.getInteger(args[0]);
        serverAddress = InetAddress.getByName(args[1]);
        filepath = args[2];
    }

    public Client(String[] args) {

        try
        {
            Init(args);
        }
        catch (Exception e)
        {
            System.out.println("Init error:" + e.getMessage());
            return;
        }

        try
        {
            f = new File(filepath);
            source = new FileInputStream(f);
        }
        catch (FileNotFoundException e)
        {
            System.out.println(e.getMessage());
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

        try
        {
            out = socket.getOutputStream();
            in = socket.getInputStream();
        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());
            return;
        }

        try{
            sendFileName();
            sendFileSize();
            sendFile();
        }
        catch(IOException e){
            e.printStackTrace();
        }

        try
        {
            System.out.println(recvFinishMess());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }


    }

    private String getFileName(){
        String[] substr;
        String delim = "//";
        substr = filepath.split(delim);
        return substr[substr.length - 1];
    }

    private void sendFileName() throws IOException
    {
        String fileName = getFileName();
        byte[] len = LengthDecoder.intToBytes(fileName.length());
        out.write(len);
        byte[] name = fileName.getBytes();
        out.write(name);
    }

    private void sendFileSize() throws IOException
    {
        long fileLength = f.length();
        String lenStr = String.valueOf(fileLength);

        byte[] len = LengthDecoder.intToBytes(lenStr.length());
        out.write(len);
        byte[] size = lenStr.getBytes();
        out.write(size);
    }

    private void sendFile() throws IOException
    {
        byte[] buffer = new byte[1024];
        int length;
        while((length = source.read(buffer)) != 0 && source.getChannel().isOpen()){
            out.write(buffer, 0, length);
        }
    }

    private String recvFinishMess() throws IOException
    {
        ByteReader br = new ByteReader(in);
        byte[] finishMessLength = br.readNBytes(2);
        byte[] finishMess = br.readNBytes(LengthDecoder.bytesToInt(finishMessLength));
        return Arrays.toString(finishMess);
    }

}
