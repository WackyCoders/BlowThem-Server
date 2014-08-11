package server.Garage;

/**
 * Created by foban on 02.08.14.
 */
public class Tank {
    int id_tank;
    int id_armor;
    int id_engine;
    int id_first_weapon;
    int id_second_weapon;

    Tank(int id_tank, int id_armor, int id_engine, int id_first_weapon, int id_second_weapon){
        this.id_tank = id_tank;
        this.id_armor = id_armor;
        this.id_engine = id_engine;
        this.id_first_weapon = id_first_weapon;
        this.id_second_weapon = id_second_weapon;
    }

    public void setArmor(int id_armor){
        this.id_armor = id_armor;
    }

    public void setEngine(int id_engine){
        this.id_engine = id_engine;
    }

    public void setFirstWeapon(int id_first_weapon){
        this.id_first_weapon = id_first_weapon;
    }

    public void setSecondWeapon(int id_second_weapon){
        this.id_second_weapon = id_second_weapon;
    }
}
