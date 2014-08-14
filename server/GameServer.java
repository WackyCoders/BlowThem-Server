package server;

import java.util.*;

public class GameServer{

    TreeMap<Integer, ConnectServer.UserProcessor> loginUsers = new TreeMap<Integer, ConnectServer.UserProcessor>();
    //TreeMap<Integer, ConnectServer.UserProcessor> battleUsers = new TreeMap<Integer, ConnectServer.UserProcessor>();
    Queue<Battle> battleProcesses = new LinkedList<Battle>();

    private int battleSize;

    private synchronized boolean userExist(Integer id_user){
        return loginUsers.containsKey(id_user);
    }

    private synchronized boolean userExistBattle(Integer id_user){
        for(Battle entry : battleProcesses){
            if(entry.userExist(id_user)){
                return true;
            }
        }
        return false;
    }


    public synchronized void addUser(Integer id_user, ConnectServer.UserProcessor processor) throws Exception {
        if(!userExist(id_user)){
            loginUsers.put(id_user,processor);
        }
        else
            throw new Exception("Such user already in system");
    }

    public synchronized Battle addUserIntoBattle(Integer id_user) throws Exception {
        if(userExist(id_user)) {
            if (!userExistBattle(id_user)) {

                for(Battle entry : battleProcesses){
                    if(!entry.full()){
                        entry.addUser(id_user, loginUsers.get(id_user));
                        return entry;
                    }
                }
                final Battle battle = new Battle(battleSize);
                battle.addUser(id_user,loginUsers.get(id_user));
                battleProcesses.add(battle);
                return battle;

            } else
                throw new Exception("Such user already in Battle");
        }
        else
            throw new Exception("Such user is not exist");
    }

    public synchronized void deleteUser(Integer id_user, Battle battle){
        if(userExist(id_user)){
            loginUsers.remove(id_user);
            if(battle != null && battleProcesses.contains(battle))
                battle.deleteUser(id_user);
        }
    }


    private synchronized void startBattle() {
    }

    public GameServer(int battleSize) {
        this.battleSize = battleSize;
    }

}
