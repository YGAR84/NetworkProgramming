package ru.nsu.g.a.lyamin.chat_tree.message_scanner;

import ru.nsu.g.a.lyamin.chat_tree.message.Message;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public class MessageScanner implements Runnable
{

    private Map<UUID, Message> messageMap = new ConcurrentHashMap<>();
    private String name;

    public MessageScanner(ConcurrentHashMap<UUID, Message> _messageMap, String _name)
    {

    }

    @Override
    public void run()
    {

    }
}
