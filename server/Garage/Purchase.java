package server.Garage;

/**
 * Created by foban on 8/10/14.
 */
public class Purchase {
    Integer id =null;
    Integer id_tank = null;
    Integer id_armor = null;
    Integer id_engine = null;
    Integer id_first_weapon = null;
    Integer id_second_weapon = null;

    public Purchase(){}

    public Purchase(Purchase another){
        this.id = another.id;
        this.id_tank = another.id_tank;
        this.id_armor = another.id_armor;
        this.id_engine = another.id_engine;
        this.id_first_weapon = another.id_first_weapon;
        this.id_second_weapon = another.id_second_weapon;
    }

    public void setTank(int id_tank){
        this.id_tank = id_tank;
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

    public void setID(int id){
        this.id = id;
    }
}
