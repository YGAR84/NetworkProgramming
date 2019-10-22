package ru.nsu.g.a.lyamin.chat_tree.client;

import ru.nsu.g.a.lyamin.chat_tree.client_info.ClientINFO;
import ru.nsu.g.a.lyamin.chat_tree.message.Message;
import ru.nsu.g.a.lyamin.chat_tree.message.MessageDecoder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.*;

import static java.lang.System.currentTimeMillis;


public class ChatTreeNode
{
    private String name;
    private int loss;
    private int port;

    private DatagramSocket socket;

    private ClientINFO me;
    private ClientINFO alternative = null;
    private Map<ClientINFO, ClientINFO> neighbors = new HashMap<>();
    private Map<UUID, Message> messageMap = new HashMap<>();
    private Map<UUID, Long> recentMessagesUUIDs = new HashMap<>();

    private final Random rand = new Random(currentTimeMillis());

    private int disconnectRetriesValue = 10;
    private long timeout = 1000;

    private byte[] buffer = new byte[1024];

    private void commonInit(String _name, int _port, int _loss) throws IOException
    {
        name = _name;
        loss = _loss;
        port = _port;
        socket = new DatagramSocket(port);
        me = new ClientINFO(InetAddress.getLoopbackAddress(), port);
    }

    public ChatTreeNode(String _name, int _port, int _loss) throws IOException
    {
        commonInit(_name, _port, _loss);
        begin();
    }

    public ChatTreeNode(String _name, int _port, int _loss, InetAddress conn_ip, int conn_port) throws IOException
    {
        commonInit(_name, _port, _loss);
        connectMessage(new ClientINFO(conn_ip, conn_port));
        begin();
    }


    private void begin() throws IOException
    {

        try(BufferedReader in = new BufferedReader(new InputStreamReader(System.in)))
        {
            while (true)
            {

                checkMessageMap();
                checkRecentMessages();

                sendMessages();

                long timeStart = currentTimeMillis();
                socket.setSoTimeout((int)timeout);

                while(currentTimeMillis() - timeStart < timeout)
                {
                    try
                    {

                        if(in.ready())
                        {
                            String message = in.readLine();
                            if(!procceedMessage(message)) { return; }

                        }

                        DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
                        socket.receive(dp);

                        if(!messageLost())
                        {
                            procceedDP(dp);
                        }

                    }
                    catch(SocketTimeoutException ignored) {}
                }

            }
        }
        finally
        {
            System.out.println("I'm over");
            socket.close();
        }
    }

    private void checkMessageMap()
    {
        Set<ClientINFO> forDelete = new HashSet<>();
        for(Map.Entry<UUID, Message> entry : messageMap.entrySet())
        {
            if(entry.getValue().getRetries() >= disconnectRetriesValue)
            {
                forDelete.add(entry.getValue().getClientINFO());
            }
        }

        for(ClientINFO ci : forDelete)
        {
            disconnectFromClient(ci);
        }
    }

    private void checkRecentMessages()
    {
        long timeNow = currentTimeMillis();
        recentMessagesUUIDs.entrySet().removeIf(entry -> timeNow - entry.getValue() > timeout * disconnectRetriesValue);
    }

    private boolean messageLost()
    {
        return rand.nextInt(100) < loss;
    }

    private void procceedDP(DatagramPacket dp)
    {
        Message mess;
        try
        {
            mess = MessageDecoder.decodeMess(dp);
        }
        catch (IOException | ClassNotFoundException e)
        {
            System.out.println("Can not decode message");
            return;
        }

        switch(mess.getMESS_TYPE())
        {
            case MessageDecoder.MESS_HEALTH:
            {
                if(!neighbors.containsKey(mess.getClientINFO())) return;
                if(!recentMessagesUUIDs.containsKey(mess.getMessID()))
                {
                    recentMessagesUUIDs.put(mess.getMessID(), currentTimeMillis());
                }

                sendAcceptMess(mess);
                break;
            }
            case MessageDecoder.MESS_CONNECT:
            {

                if(!recentMessagesUUIDs.containsKey(mess.getMessID()))
                {

                    ClientINFO ci = mess.getClientINFO();
                    if(!neighbors.containsKey(ci))
                    {
                        neighbors.put(ci, null);
                        addHealthCheckMess(mess);
                    }
                    if(alternative == null)
                    {

                        alternative = ci;
                    }
                    sendAlternative();

                    recentMessagesUUIDs.put(mess.getMessID(), currentTimeMillis());
                }

                sendAcceptMess(mess);
                break;
            }
            case MessageDecoder.MESS_MESS:
            {

                if(!neighbors.containsKey(mess.getClientINFO())) return;
                if(!recentMessagesUUIDs.containsKey(mess.getMessID()))
                {
                    for(Map.Entry<ClientINFO, ClientINFO> n : neighbors.entrySet())
                    {
                        if(!n.getKey().equals(mess.getClientINFO()))
                        {
                            UUID newUUID = UUID.randomUUID();
                            Message newMess = new Message(MessageDecoder.MESS_MESS, newUUID,
                                    mess.getMess(), n.getKey().getIp(), n.getKey().getPort());
                            sendMessage(newMess);
                        }
                    }

                    recentMessagesUUIDs.put(mess.getMessID(), System.currentTimeMillis());
                    System.out.println(Message.decodeString(mess.getMess()));
                }

                sendAcceptMess(mess);
                break;
            }
            case MessageDecoder.MESS_ACCEPT:
            {

                if(!recentMessagesUUIDs.containsKey(mess.getMessID()) && messageMap.containsKey(mess.getMessID()))
                {
                    if(messageMap.get(mess.getMessID()).getMESS_TYPE() == MessageDecoder.MESS_HEALTH)
                    {
                        addHealthCheckMess(mess);
                    }
                    else if(messageMap.get(mess.getMessID()).getMESS_TYPE() == MessageDecoder.MESS_CONNECT)
                    {

                        if(neighbors.containsKey(mess.getClientINFO())) return;

                        neighbors.put(mess.getClientINFO(), null);
                        addHealthCheckMess(mess);
                        if(alternative == null)
                        {
                            alternative = mess.getClientINFO();
                        }

                        sendAlternative();
                    }
                    messageMap.remove(mess.getMessID());
                    recentMessagesUUIDs.put(mess.getMessID(), currentTimeMillis());
                }

                break;
            }
            case MessageDecoder.MESS_ALTERNATIVE:
            {

                if(!recentMessagesUUIDs.containsKey(mess.getMessID()))
                {
                    try
                    {
                        if(!neighbors.containsKey(mess.getClientINFO())) { return; }

                        ClientINFO newAlter = Message.decodeIp(mess.getMess());

                        if(!newAlter.equals(me))
                        {
                            neighbors.put(mess.getClientINFO(), newAlter);
                        }

                    }
                    catch (UnknownHostException ignored) { System.out.println("Can not decode IP from Message");}
                    recentMessagesUUIDs.put(mess.getMessID(), System.currentTimeMillis());
                }

                sendAcceptMess(mess);
                break;
            }
            case MessageDecoder.MESS_DISCONNECT:
            {
                disconnectFromClient(mess.getClientINFO());
            }
        }
    }

    private void sendMessage(Message m)
    {
        try
        {
            DatagramPacket dp = MessageDecoder.encodeMessage(m);
            messageMap.put(m.getMessID(), m);
            socket.send(dp);
        }
        catch (IOException ignored) {System.out.println("Can't encode message");}
    }

    private void sendMessages()
    {
        for(Map.Entry<UUID, Message> m : messageMap.entrySet())
        {
            try
            {
                DatagramPacket dp = MessageDecoder.encodeMessage(m.getValue());
                m.getValue().incrementRetries();
                socket.send(dp);
            }
            catch (IOException ignored) {System.out.println("Failed message send"); }
        }
    }



    private void sendAcceptMess(Message mess)
    {
        Message acceptMess = new Message(MessageDecoder.MESS_ACCEPT, mess.getMessID(),
                null, mess.getClientINFO().getIp(), mess.getClientINFO().getPort());

        try
        {
            DatagramPacket acceptDp = MessageDecoder.encodeMessage(acceptMess);
            socket.send(acceptDp);
        }
        catch (IOException e)
        {
            System.out.println("Error while encoding or sending mess:" + e.getMessage());
        }
    }

    private void sendAlternative()
    {
        for(Map.Entry<ClientINFO, ClientINFO> entry : neighbors.entrySet())
        {
            Message newMess = new Message(MessageDecoder.MESS_ALTERNATIVE, UUID.randomUUID(),
                    Message.encodeIp(alternative.getIp(), alternative.getPort()), entry.getKey().getIp(), entry.getKey().getPort());
            sendMessage(newMess);
        }
    }

    private void changeAlternativeMess(ClientINFO ci)
    {
        if(ci.equals(alternative))
        {
            if(neighbors.size() != 0)
            {
                alternative = neighbors.entrySet().iterator().next().getKey();
                sendAlternative();
                return;
            }
            alternative = null;
        }
    }

    private void connectMessage(ClientINFO ci)
    {
        if(ci != null)
        {
            Message connMess = new Message(MessageDecoder.MESS_CONNECT, UUID.randomUUID(), null, ci.getIp(), ci.getPort());
            sendMessage(connMess);
        }
    }

    private void addHealthCheckMess(Message mess)
    {
        Message healthMess = new Message(MessageDecoder.MESS_HEALTH, UUID.randomUUID(), null,
                mess.getClientINFO().getIp(), mess.getClientINFO().getPort());
        sendMessage(healthMess);
    }


    private void deleteMessagesFromClient(ClientINFO ci)
    {
        for(Iterator<Map.Entry<UUID, Message>> iter = messageMap.entrySet().iterator(); iter.hasNext();)
        {
            Map.Entry<UUID, Message> entry = iter.next();
            Message m = entry.getValue();
            if(m.getClientINFO().equals(ci))
            {
                iter.remove();
            }
        }
    }

    private void disconnectFromClient(ClientINFO ci)
    {
        deleteMessagesFromClient(ci);
        connectMessage(neighbors.get(ci));
        neighbors.remove(ci);
        changeAlternativeMess(ci);
    }

    private boolean procceedMessage(String message)
    {
        String exitMess = "/exit";
        String ipMess = "/ip";

        if(message.equals(exitMess))
        {
            for (Map.Entry<ClientINFO, ClientINFO> n : neighbors.entrySet())
            {
                UUID newUUID = UUID.randomUUID();
                Message discMess = new Message(MessageDecoder.MESS_DISCONNECT,
                        newUUID, null, n.getKey().getIp(), n.getKey().getPort());

                try
                {
                    socket.send(MessageDecoder.encodeMessage(discMess));
                }
                catch (IOException ignored){}
            }
            return false;
        }
        else if (message.equals(ipMess))
        {
            System.out.println("Ip/port : " + me.getIp() + "/" + me.getPort());
        }
        else
        {
            addMessage(message);
        }

        return true;
    }

    private void addMessage(String mess)
    {
        byte[] message = Message.encodeString(name + ": " + mess);

        for(Map.Entry<ClientINFO, ClientINFO> n : neighbors.entrySet())
        {
            Message newMess = new Message(MessageDecoder.MESS_MESS, UUID.randomUUID(), message,
                    n.getKey().getIp(), n.getKey().getPort());
            sendMessage(newMess);
        }
    }


    private void soutMap()
    {
        System.out.println("_________________________________________");
        for(Map.Entry<UUID, Message> entry : messageMap.entrySet())
        {
            System.out.println(entry.getKey() + " " + entry.getValue().getMESS_TYPE() + " " + entry.getValue().getClientINFO().getIp() + " " + entry.getValue().getClientINFO().getPort());
        }


        System.out.println("_________________________________________");
    }

    private void soutRecent()
    {
        System.out.println("_________________________________________");
        for(Map.Entry<UUID, Long> entry : recentMessagesUUIDs.entrySet())
        {
            System.out.println(entry.getKey() + " " + entry.getValue());
        }
        System.out.println("Total:" + recentMessagesUUIDs.size());
        System.out.println("_________________________________________");
    }
}
