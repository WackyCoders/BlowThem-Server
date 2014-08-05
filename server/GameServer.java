package server;

import java.util.*;

public class GameServer{

    TreeMap<Integer, ConnectServer.UserProcessor> loginUsers = new TreeMap<Integer, ConnectServer.UserProcessor>();
    TreeMap<Integer, ConnectServer.UserProcessor> battleUsers = new TreeMap<Integer, ConnectServer.UserProcessor>();

    private int battleSize;

    private synchronized boolean userExist(Integer id_user){
        return loginUsers.containsKey(id_user);
    }

    private synchronized boolean userExistBattle(Integer id_user){
        return battleUsers.containsKey(id_user);
    }

    public synchronized void addUser(Integer id_user, ConnectServer.UserProcessor processor) throws Exception {
        if(!userExist(id_user)){
            loginUsers.put(id_user,processor);
        }
        else
            throw new Exception("Such user already in system");
    }

    public synchronized void addUserBattle(Integer id_user) throws Exception {
        if(!userExist(id_user)) {
            if (!userExistBattle(id_user)) {

                battleUsers.put(id_user, loginUsers.get(id_user));
                if (battleUsers.size() == battleSize)
                    startBattle();
            } else
                throw new Exception("Such user already in queue on new Battle");
        }
        else
            throw new Exception("Such user is not exist");
    }

    public synchronized void deleteUser(Integer id_user){
        if(userExist(id_user)){
            loginUsers.remove(id_user);
            if(userExistBattle(id_user))
                battleUsers.remove(id_user);
        }
    }


    private synchronized void startBattle() {

    }

    public GameServer(int battleSize) {
        this.battleSize = battleSize;
    }

}
