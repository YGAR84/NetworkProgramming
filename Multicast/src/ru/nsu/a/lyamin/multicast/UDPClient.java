package ru.nsu.a.lyamin.multicast;

import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

class UDPClient
{

    private Map<InetAddress, Long> table = new HashMap<>();
    private long reciveTimeout = 3000;
    private int socketTimeout = 300;
    private boolean flag = false;


    private String msgAlive = "alive";
    private String msgDead  = "dead";

    UDPClient(String group, int port) throws IOException
    {
        MulticastSocket ms;

        ms = new MulticastSocket(port);

        InetAddress groupIP = InetAddress.getByName(group);

        ms.joinGroup(groupIP);

        ms.setSoTimeout(socketTimeout);

        Random random = new Random();
        int uniqNum = random.nextInt();
        while (uniqNum < 0) uniqNum += 1000000000;
        uniqNum %= 1000000000;

        msgAlive += uniqNum;
        msgDead += uniqNum;
        System.out.println("My message:" + msgAlive);
        System.out.println("My message:" + msgDead);

        while(true)
        {
            DatagramPacket dp = new DatagramPacket(msgAlive.getBytes(), msgAlive.length(), groupIP, port);
            ms.send(dp);

            long start = System.currentTimeMillis();
            while(System.currentTimeMillis() - start < reciveTimeout)
            {
                DatagramPacket rdp = new DatagramPacket(new byte[1024], 1024);
                try {
                    ms.receive(rdp);
                }
                catch(SocketTimeoutException e) { continue; }
                ProcessIp(rdp);
                PrintTable();
            }
            CheckTable();
            PrintTable();
        }

        //DatagramPacket finalDP = new DatagramPacket(msgDead.getBytes(), msgDead.length(), groupIP, port);
        //ms.send(finalDP);
    }

    private void ProcessIp(DatagramPacket dp)
    {
        if(!table.containsKey(dp.getAddress())){
            flag = true;
        }
        table.put(dp.getAddress(), System.currentTimeMillis());
    }

    private void CheckTable(){
        HashMap<InetAddress, Long> feedbackMap = new HashMap<>(table);

        long time = System.currentTimeMillis();
        for(Map.Entry<InetAddress, Long> v : feedbackMap.entrySet())
        {
            if(time - v.getValue() > reciveTimeout * 3)
            {
                table.remove(v.getKey());
                flag = true;
            }
        }
    }

    private void PrintTable(){
        long time = System.currentTimeMillis();
        if (flag) {
            System.out.println("_________________________________");
            System.out.println("|                                |");
            for (Map.Entry<InetAddress, Long> v: table.entrySet())
            {
                String output = String.format("|%-32s|", v.getKey().toString() + ":" + (time - v.getValue()));
                System.out.println(output);
            }
            System.out.println("|                                |");
            System.out.println("_________________________________\n");

            flag = false;
        }
    }
}
