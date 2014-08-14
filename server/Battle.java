package server;

import java.util.*;

/**
 * Created by foban on 31.07.14.
 */
public class Battle extends Thread {

    TreeMap<Integer, ConnectServer.UserProcessor> battleUsers = new TreeMap<Integer, ConnectServer.UserProcessor>();
    private int battleSize;

    boolean full(){
        return battleSize < battleUsers.size();
    }

    synchronized boolean userExist(Integer id_user){
        return battleUsers.containsKey(id_user);
    }

    synchronized void deleteUser(Integer id_user){
        if(userExist(id_user))
            battleUsers.remove(id_user);
    }

    void addUser(Integer id_user, ConnectServer.UserProcessor processor) throws Exception {
        if(!userExist(id_user) && !full()){
            battleUsers.put(id_user,processor);
            if(battleSize <= battleUsers.size()){
                final Thread thread = new Thread(this);
                thread.start();
            }
        }
        else
            throw new Exception("Such user already in Battle");
    }

    public Battle(int battleSize){
        this.battleSize = battleSize;
    }

    public void run() {
        System.out.println("Здесь по идее будет происходить обработка данных боя");
    }
}
