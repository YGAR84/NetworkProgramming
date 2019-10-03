package ru.nsu.g.a.lyamin.tcpfilereciver.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server
{
    private int serverPort;
    private ExecutorService pool = Executors.newCachedThreadPool();

    private void Init(String[] args) throws Exception
    {
        if(args.length < 2)
        {
            throw new Exception("Error, has no params");
        }
        serverPort = Integer.getInteger(args[0]);
    }

    public Server(String[] args){
        try
        {
            Init(args);
        }
        catch (Exception e)
        {
            System.out.println("Init error:" + e.getMessage());
            return;
        }


        ServerSocket sc;
        try
        {
            sc = new ServerSocket(serverPort);
        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());
            return;
        }

        while(true)
        {
            try(Socket socket = sc.accept())
            {
                pool.submit(new Session(socket));
            }
            catch(IOException e){
                System.out.println(e.getMessage());
            }
        }
    }

    private void printSpeed(){

    }

}
