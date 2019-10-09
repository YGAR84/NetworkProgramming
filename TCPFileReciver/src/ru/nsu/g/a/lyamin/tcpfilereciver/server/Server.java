package ru.nsu.g.a.lyamin.tcpfilereciver.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server
{

    //private ExecutorService service = Executors.newFixedThreadPool(10);

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
            System.out.println("|    Address    |     Speed   |Instant Speed|Percents|  File name");
            while(true)
            {
                try(Socket socket = ss.accept())
                {
                    Session s = new Session(socket);
                    s.run();
//                    new Thread(new Session(socket)).start();
//                    service.submit(s);
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
