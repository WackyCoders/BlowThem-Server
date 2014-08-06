package server.Garage;

import java.io.DataOutputStream;
import java.util.*;

/**
 * Created by foban on 8/6/14.
 */
public class Garage {
    TreeMap<Integer, Tank> tanks = new TreeMap<Integer, Tank>();
    Queue<Integer> armorsID = new LinkedList<Integer>();
    Queue<Integer> enginesID = new LinkedList<Integer>();
    Queue<Integer> firstWeaponID = new LinkedList<Integer>();
    Queue<Integer> secondWeaponID = new LinkedList<Integer>();



    public void addTank(int id, int id_tank, int id_armor, int id_engine, int id_first_weapon, int id_second_weapon){
        //tanks.add();
        tanks.put(id, new Tank(id_tank, id_armor, id_engine, id_first_weapon, id_second_weapon));
    }

    public void addArmor(int id_armor){
        armorsID.add(id_armor);
    }

    public void addEngine(int id_engine){
        enginesID.add(id_engine);
    }

    public void addFirstWeapon(int id_weapon){
        firstWeaponID.add(id_weapon);
    }

    public void addSecondWeapon(int id_weapon){
        secondWeaponID.add(id_weapon);
    }

    public void send(DataOutputStream outputStream)throws Exception{
        Tank s;
        for(Map.Entry<Integer, Tank> entry : tanks.entrySet()){
            s = entry.getValue();
            outputStream.writeUTF("$tank$");
            outputStream.writeInt(entry.getKey());
            outputStream.writeInt(s.id_tank);
            outputStream.writeInt(s.id_armor);
            outputStream.writeInt(s.id_engine);
            outputStream.writeInt(s.id_first_weapon);
            outputStream.writeInt(s.id_second_weapon);
            outputStream.flush();
        }
    }
}
