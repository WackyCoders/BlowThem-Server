package server.Garage;

import java.io.DataOutputStream;
import java.util.*;

/**
 * Created by foban on 8/6/14.
 */
public class Garage {
    TreeMap<Integer, Tank> tanks = new TreeMap<Integer, Tank>(); //id of the tank in garage is a key
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

        //send tanks
        Tank s;
        outputStream.writeUTF("$tank$");
        outputStream.writeInt(tanks.size());
        for(Map.Entry<Integer, Tank> entry : tanks.entrySet()){
            s = entry.getValue();
            outputStream.writeInt(entry.getKey()); //it's a id of the tank which we send
            outputStream.writeInt(s.id_tank);
            outputStream.writeInt(s.id_armor);
            outputStream.writeInt(s.id_engine);
            outputStream.writeInt(s.id_first_weapon);
            outputStream.writeInt(s.id_second_weapon);
            outputStream.flush();
        }
        outputStream.flush();

        //send armor
        outputStream.writeUTF("$armor$");
        outputStream.writeInt(armorsID.size());
        for(Integer entry : armorsID){
            outputStream.writeInt(entry);
        }
        outputStream.flush();

        //send engine
        outputStream.writeUTF("$engine$");
        outputStream.writeInt(enginesID.size());
        for(Integer entry : enginesID){
            outputStream.writeInt(entry);
        }
        outputStream.flush();

        //send first weapon
        outputStream.writeUTF("$first_weapon$");
        outputStream.writeInt(firstWeaponID.size());
        for(Integer entry : firstWeaponID){
            outputStream.writeInt(entry);
        }
        outputStream.flush();

        //send second weapon
        outputStream.writeUTF("$second_weapon$");
        outputStream.writeInt(secondWeaponID.size());
        for(Integer entry : secondWeaponID){
            outputStream.writeInt(entry);
        }
        outputStream.flush();
    }

    public void setEquipment(int id_tank, String type, int id_equipment) throws Exception {
        Tank tank = tanks.get(id_tank);
        Queue equipment = null;
        if(tank == null || type == null){
            throw new Exception("Hi! I'm your old bug :)");
        } else if(type.equals("$armor$")){
            if(armorsID.contains(id_equipment)){
                tank.setArmor(id_equipment);
            }else
                throw new Exception("Some one try hack our server!! Let kill this stupid person!!!");
        } else if(type.equals("$engine$")){
            if(enginesID.contains(id_equipment)){
                tank.setEngine(id_equipment);
            }else
                throw new Exception("Some one try hack our server!! Let kill this stupid person!!!");
        } else if(type.equals("$first_weapon$")){
            if(firstWeaponID.contains(id_equipment)){
                tank.setFirstWeapon(id_equipment);
            }else
                throw new Exception("Some one try hack our server!! Let kill this stupid person!!!");
        } else if(type.equals("$second_weapon$")){
            if(secondWeaponID.contains(id_equipment)){
                tank.setSecondWeapon(id_equipment);
            }else
                throw new Exception("Some one try hack our server!! Let kill this stupid person!!!");
        } else
            throw new Exception("I'm older then you Babe");

    }

    public void addPurchase(Purchase purchase){
        if(purchase != null){
            if(purchase.id != null){
                addTank(purchase.id, purchase.id_tank, purchase.id_armor, purchase.id_engine, purchase.id_first_weapon, purchase.id_second_weapon);
                addArmor(purchase.id_armor);
                addEngine(purchase.id_engine);
                addFirstWeapon(purchase.id_first_weapon);
                addSecondWeapon(purchase.id_second_weapon);
            }else if(purchase.id_armor != null){
                addArmor(purchase.id_armor);
            }else if(purchase.id_engine != null){
                addEngine(purchase.id_engine);
            }else if(purchase.id_first_weapon != null){
                addFirstWeapon(purchase.id_first_weapon);
            }else if(purchase.id_second_weapon != null){
                addSecondWeapon(purchase.id_second_weapon);
            }
        }
    }

    public int getTankID(int id){
        return tanks.get(id).id_tank;
    }

    public void setArmor(int id_tank, int id_armor) throws Exception {
        Tank tank = tanks.get(id_tank);
        if(armorsID.contains(id_armor) && tank != null){
            tank.setArmor(id_armor);
        }else
            throw new Exception("Some one try hack our server!! Let kill this stupid person!!!");
    }
    public void setEngine(int id_tank, int id_engine) throws Exception {
        Tank tank = tanks.get(id_tank);
        if(enginesID.contains(id_engine) && tank != null){
            tank.setArmor(id_engine);
        }else
            throw new Exception("Some one try hack our server!! Let kill this stupid person!!!");
    }
    public void setFirstWeapon(int id_tank, int id_weapon) throws Exception {
        Tank tank = tanks.get(id_tank);
        if(firstWeaponID.contains(id_weapon) && tank != null){
            tank.setArmor(id_weapon);
        }else
            throw new Exception("Some one try hack our server!! Let kill this stupid person!!!");
    }
    public void setSecondWeapon(int id_tank, int id_weapon) throws Exception {
        Tank tank = tanks.get(id_tank);
        if(secondWeaponID.contains(id_weapon) && tank != null){
            tank.setArmor(id_weapon);
        }else
            throw new Exception("Some one try hack our server!! Let kill this stupid person!!!");
    }

    public boolean containsTank(int id){
        return tanks.containsKey(id);
    }
}
