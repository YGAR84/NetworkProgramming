package ru.nsu.g.a.lyamin.tcpfilereciver.server;

import ru.nsu.g.a.lyamin.tcpfilereciver.LengthDecoder;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Session implements Runnable
{

    private Socket socket;
    private InputStream input;
    private OutputStream output;
    private ByteReader br;

    Session(Socket _socket) throws IOException
    {
        socket = _socket;
        input = socket.getInputStream();
        output = socket.getOutputStream();
        br = new ByteReader(input);
    }

    @Override
    public void run()
    {
        byte[] nameLengthBytes, nameBytes, fileLengthLengthBytes, fileLengthBytes;
        try
        {
            nameLengthBytes = br.readNBytes(2);
        }
        catch (IOException e)
        {
            System.out.println("Error while reading name length:" + e.getMessage());
            return;
        }

        int nameLength = LengthDecoder.bytesToInt(nameLengthBytes);


        try
        {
            nameBytes = br.readNBytes(nameLength);
        }
        catch (IOException e)
        {
            System.out.println("Error while reading name:" + e.getMessage());
            return;
        }

        FileOutputStream fout;

        String filename = new String(nameBytes, StandardCharsets.UTF_8);

        try
        {
            fout = new FileOutputStream("/uploads/" + filename);
        }
        catch (FileNotFoundException e)
        {
            System.out.println("Error while creating file:" + e.getMessage());
            return;
        }

        try
        {
            fileLengthLengthBytes = br.readNBytes(2);
        }
        catch (IOException e)
        {
            System.out.println("Error while reading length of file length:" + e.getMessage());
            return;
        }

        int fileLengthLength = LengthDecoder.bytesToInt(fileLengthLengthBytes);

        try
        {
            fileLengthBytes = br.readNBytes(fileLengthLength);
        }
        catch (IOException e)
        {
            System.out.println("Error while reading file length:" + e.getMessage());
            return;
        }

        int fileLength = LengthDecoder.bytesToInt(fileLengthBytes);

        FileWriterFromStream fofs = new FileWriterFromStream(socket, br.getRest(), br.getRestLen(), filename);


        try
        {
            fofs.writeNBytesFromInputToFile(input, fout, fileLength);
        }
        catch (IOException e)
        {
            System.out.println("Error while reading and writing file:" + e.getMessage());
        }

        try
        {
            output.write("Success!".getBytes());
        }
        catch (IOException e)
        {
            System.out.println("Error while sending result:" + e.getMessage());
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

}
