package server;

import java.util.*;

/**
 * Created by foban on 31.07.14.
 */
public class Battle extends Thread {

    TreeMap<Integer, ConnectServer.UserProcessor> battleUsers = new TreeMap<Integer, ConnectServer.UserProcessor>();
    private int battleSize;
    private boolean closed = false;



    public Battle(int battleSize){
        this.battleSize = battleSize;
    }

    boolean full(){
        return battleSize <= battleUsers.size();
    }

    synchronized boolean userExist(Integer id_user){
        return battleUsers.containsKey(id_user);
    }

    synchronized void deleteUser(Integer id_user){
        if(userExist(id_user)){
            battleUsers.remove(id_user);
            if(battleUsers.size()==0)
                closed = true;
        }
    }

    void addUser(Integer id_user, ConnectServer.UserProcessor processor) throws Exception {
        if(!userExist(id_user) && !full()){
            battleUsers.put(id_user,processor);
            if(full()){
                final Thread thread = new Thread(this);
                thread.start();
            }
        }
        else
            throw new Exception("Such user already in Battle");
    }


    public void run() {
        System.out.println("Здесь по идее будет происходить обработка данных боя");
        while(!closed){
            try {
                sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Людей: " + battleUsers.size());
        }
        System.out.println("Бой завершился");
    }
}
