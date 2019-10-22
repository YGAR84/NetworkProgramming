package ru.nsu.g.a.lyamin.chat_tree;

import ru.nsu.g.a.lyamin.chat_tree.client.ChatTreeNode;

import java.io.IOException;
import java.net.InetAddress;

public class ClientMain
{
    public static void main(String[] args)
    {
        if (args.length == 3)
        {
            int port = Integer.parseInt(args[1]);
            int loss = Integer.parseInt(args[2]);
            try
            {
                System.out.println(args[0] + " " + port + " " + loss);
                new ChatTreeNode(args[0], port, loss);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else if (args.length == 5)
        {
            int port = Integer.parseInt(args[1]);
            int loss = Integer.parseInt(args[2]);
            try
            {
                InetAddress ip = InetAddress.getByName(args[3]);
                int connPort = Integer.parseInt(args[4]);

                System.out.println(args[0]+ " " + port + " " + loss);
                new ChatTreeNode(args[0], port, loss, ip, connPort);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

        }
        else
        {
            System.out.println("Args are invalid");
        }
    }
}
