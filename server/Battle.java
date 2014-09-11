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
                startSend();
                thread.start();
            }
        }
        else
            throw new Exception("Such user already in Battle");
    }
    private void startSend() {
        int i = 1;
        for(Map.Entry<Integer, ConnectServer.UserProcessor> entry : battleUsers.entrySet()){
            if(i == 1)entry.getValue().send("first");
            else if(i == 2) entry.getValue().send("second");
            else entry.getValue().send(i);
            i++;
        }
    }


    public void run() {
        System.out.println("Здесь по идее будет происходить обработка данных боя");
        ConnectServer.UserProcessor currentGamer, gamer;
        while(!closed){
            for(Map.Entry<Integer, ConnectServer.UserProcessor> entry : battleUsers.entrySet()){
                currentGamer = entry.getValue();
                for(Map.Entry<Integer, ConnectServer.UserProcessor> entryS : battleUsers.entrySet()){
                    gamer = entryS.getValue();
                    if(currentGamer != gamer){
                        currentGamer.send("m");

                        currentGamer.send(gamer.X);
                        currentGamer.send(gamer.Y);
                        currentGamer.send(gamer.bitmapAngle);
                        currentGamer.send(gamer.targetX);
                        currentGamer.send(gamer.targetY);

                        if(gamer.fired){
                            //System.out.println("FIRE!!!");
                            currentGamer.send("$fire$");
                            currentGamer.send(String.valueOf(gamer.xFire));
                            currentGamer.send(String.valueOf(gamer.yFire));
                            gamer.fired = false;
                        }

                        if(gamer.lost){
                            System.out.println("Surrender accepted :)");
                            currentGamer.send("$victory$");
                            gamer.lost = false;
                            closed = true;
                        }
                    }
                }
            }
        }
        System.out.println("Бой завершился");
    }
}
