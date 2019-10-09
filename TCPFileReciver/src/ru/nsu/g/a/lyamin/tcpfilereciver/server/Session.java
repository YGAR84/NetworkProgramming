package ru.nsu.g.a.lyamin.tcpfilereciver.server;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

public class Session implements Runnable
{

    private Socket socket;
    private DataInputStream input;
    private DataOutputStream output;
    private static final long timeout = 3000;

    Session(Socket _socket) throws IOException
    {
        socket = _socket;
        input = new DataInputStream(socket.getInputStream());
        output = new DataOutputStream(socket.getOutputStream());
    }

    @Override
    public void run()
    {
        System.out.println("connected");

        String filename;
        try
        {
            filename = readFileName();
        }
        catch (IOException e)
        {
            System.out.println("Error while reading file name:" + e.getMessage());
            e.printStackTrace();
            return;
        }

        System.out.println(filename);

        FileOutputStream fout;

        File f = createNotExistedFile(filename);

        System.out.println(f.getName());
        try
        {
            fout = new FileOutputStream(f);
        }
        catch (FileNotFoundException e)
        {
            System.out.println("Error while creating file:" + e.getMessage());
            return;
        }

        long fileLength;

        try
        {
            fileLength = input.readLong();
        }
        catch (IOException e)
        {
            System.out.println("Error while reading file length:" + e.getMessage());
            return;
        }

        System.out.println(fileLength);


        try
        {
            recvFile(fileLength, fout);
        }
        catch (IOException e)
        {
            System.out.println("Error while reciving file:"+ e.getMessage());

            try
            {
                sendFinalMessage("Failed!");
            }
            catch (IOException e1)
            {
                System.out.println("Error while sending final message"+ e1.getMessage());
            }
            return;
        }

        try
        {
            sendFinalMessage("Success!");
        }
        catch (IOException e)
        {
            System.out.println("Error while sending final message"+ e.getMessage());
        }


        try
        {
            socket.close();
        }
        catch (IOException e)
        {
            System.out.println("Error while closing socket:" + e.getMessage());
        }

        try
        {
            fout.close();
        }
        catch (IOException e)
        {
            System.out.println("Error while closing file:" + e.getMessage());
        }

    }


    private String getSpeedMess(double speed)
    {
        if(speed <= 1024)                    { return String.format("%-5.2f  B/s|", speed); }
        else if(speed <= 1024 * 1024)        { return String.format("%-5.2f KB/s|", speed/1024); }
        else if(speed <= 1024 * 1024 * 1024) { return String.format("%-5.2f MB/s|", speed/1024/1024);}
        else                                 { return String.format("%-5.2f GB/s|", speed/1024/1024/1024);}
    }

    private void proccedSpeed(long fileLength, long readAll, long readNow, long startTime, long prevShowTime)
    {
        long timeNow = System.currentTimeMillis();
        double speed = (double)(readAll)/(timeNow - startTime)/1000;
        double instantSpeed = (double)(readNow)/(timeNow - prevShowTime)/1000;
        String mess = String.format("|%-20s|", socket.getInetAddress());
        mess += getSpeedMess(speed);
        mess += getSpeedMess(instantSpeed);
        mess += String.format("%3.2f %%|", (double)readAll/fileLength);

        System.out.println(mess);
    }

    private void recvFile(long length, FileOutputStream fout) throws IOException
    {
        int buffSize = 1024;
        byte[] buffer = new byte[buffSize];

        long readAll = 0;
        long startTime = System.currentTimeMillis();

        while(readAll < length)
        {

            long readWithOneIter = 0;

            long currStartTime = System.currentTimeMillis()
                    ,currEndTime = currStartTime;

            try
            {

                do
                {
                    socket.setSoTimeout((int)timeout - (int)(currEndTime - currStartTime));

                    long readNow;
                    if(length - readAll >= buffSize)
                    {
                        readNow = input.read(buffer);
                    }
                    else
                    {
                        readNow = input.read(buffer, 0, (int)(length - readAll));
                    }


                    fout.write(buffer, 0, (int)readNow);
                    readWithOneIter += readNow;
                    readAll += readNow;
                }
                while(currEndTime - currStartTime < timeout && readAll < length);

            }
            catch (SocketTimeoutException e) { }

            proccedSpeed(length, readAll, readWithOneIter, startTime, currStartTime);

        }

    }

    private String readFileName() throws IOException
    {
        int nameLength;
        nameLength = input.readInt();
        System.out.println(nameLength);

        byte[] nameBytes = new byte[nameLength];

        input.readNBytes(nameBytes, 0, nameLength);

        return new String(nameBytes, StandardCharsets.UTF_8);
    }
    private File createNotExistedFile(String filename)
    {
        String prefix = filename;
        String postfix = "";
        int pos = filename.lastIndexOf('.');
        if(pos != -1)
        {
            prefix = filename.substring(0, pos);
            postfix = filename.substring(pos);
        }

        File f;

        int i = 0;
        while(true)
        {
            String newFilename = prefix + postfix;
            if(i != 0)
            {
                newFilename = prefix + "(" + i + ")" + postfix;
            }
            f = new File("uploads/" + newFilename);

            if(!f.exists())
            {
                return f;
            }
            ++i;
        }
    }
    private void sendFinalMessage(String mess) throws IOException
    {
        int len = mess.length();
        output.writeInt(len);
        output.write(mess.getBytes());
    }

}
