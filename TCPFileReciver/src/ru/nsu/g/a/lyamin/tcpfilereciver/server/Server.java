package ru.nsu.g.a.lyamin.tcpfilereciver.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server
{

    public static void main(String[] args)
    {
        if(args.length < 1)
        {
            System.err.println("Error, has no params");
            return;
        }


        int serverPort = Integer.parseInt(args[0]);
        Server s = new Server(serverPort);
    }

    private Server(int serverPort){

        try(ServerSocket ss = new ServerSocket(serverPort))
        {
            System.out.println("Listening " + InetAddress.getLocalHost().getHostAddress() + ":" + ss.getLocalPort());
            while(true)
            {
                try(Socket socket = ss.accept())
                {
                    Session s = new Session(socket);
                    s.run();
                }
                catch(IOException e)
                {
                    System.out.println(e.getMessage());
                }
            }
        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());
        }

    }

}
