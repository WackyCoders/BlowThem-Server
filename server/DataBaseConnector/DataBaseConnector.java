package server.DataBaseConnector; /**
 * Created by foban on 16.07.14.
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.*;
import server.Garage.Purchase;
//import javax.servlet.*;
//import javax.servlet.http.*;


public class DataBaseConnector {
    private final String driverName = "org.mariadb.jdbc.Driver"; //MariaBD
    private Connection connection;
    private String login;
    private String password;

    private void connect() throws DataBaseConnectorException {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost/blow_them", login, password);
        }catch (Exception e) {
            throw new DataBaseConnectorException("Connect exception: " + e.toString());
        }

    }

    private ResultSet executeQuery(String sql) throws Exception {
        Statement query = connection.createStatement();
        return query.executeQuery(sql);
    }

    private int executeUpdate(String sql) throws Exception {
        Statement query = connection.createStatement();
        int m = query.executeUpdate(sql);
        query.close();
        return m;
    }

    private int getSomeInt(String sql, String column, String exception) throws DataBaseConnectorException {
        int someInt;
        try {
            ResultSet rs = executeQuery(sql);
            rs.next();
            someInt = rs.getInt(column);
        } catch (Exception e) {
            throw new DataBaseConnectorException(exception + e.toString());
        }
        return someInt;
    }



    public ResultSet getAllUsers() throws Exception {  //return fields id_user, username, scores, money
        return executeQuery("SELECT id_user, username, scores, money FROM users");
    }

    public ResultSet getUserMail(int id_user) throws Exception { //return field mail
        return executeQuery("SELECT mail FROM users WHERE id_user = " + id_user);
    }

    public ResultSet getUserInformation(int id_user) throws Exception { //return fields username, scores, money, tank(This tank was selected as the primary)
        return executeQuery("SELECT username, scores, money, tank FROM users WHERE id_user = " + id_user);
    }

    public ResultSet getUserTanks(int id_user) throws Exception{ //tank, armor, engine, first_weapon, second_weapon, id_tank
        return executeQuery("SELECT tank, armor, engine, first_weapon, second_weapon, id_tank FROM garage WHERE user = "+id_user);
    }



    public ResultSet getUserArmor(int id_user) throws Exception{ //armor
        return executeQuery("SELECT armor FROM garage_armor WHERE user = "+id_user);
    }
    public ResultSet getUserEngine(int id_user) throws Exception{ //engine
        return executeQuery("SELECT engine FROM garage_engine WHERE user = "+id_user);
    }
    public ResultSet getUserFirstWeapon(int id_user) throws Exception{ //first_weapon
        return executeQuery("SELECT first_weapon FROM garage_first_weapon WHERE user = "+id_user);
    }
    public ResultSet getUserSecondWeapon(int id_user) throws Exception{ //second_weapon
        return executeQuery("SELECT second_weapon FROM garage_second_weapon WHERE user = "+id_user);
    }


    private void setEquipment(String column, int id_tank, int id) throws DataBaseConnectorException {
        try {
            executeUpdate("UPDATE garage SET "+column+" = "+id+" WHERE id_tank = "+id_tank);
        }
        catch (Exception e){
            throw new DataBaseConnectorException("Error when setting equipment: " + e.toString());
        }
    }

    public void setTankArmor(int id_tank, int id_armor) throws DataBaseConnectorException {
        setEquipment("armor", id_tank, id_armor);
    }
    public void setTankEngine (int id_tank, int id) throws DataBaseConnectorException {
        setEquipment("engine", id_tank, id);
    }
    public void setTankFirstWeapon(int id_tank, int id) throws DataBaseConnectorException {
        setEquipment("first_weapon", id_tank, id);
    }
    public void setTankSecondWeapon(int id_tank, int id) throws DataBaseConnectorException {
        setEquipment("second_weapon", id_tank, id);
    }


    private int getCost(String table, String column, int id) throws DataBaseConnectorException {
        return getSomeInt("SELECT cost FROM "+table+" WHERE "+column+" = " + id,"cost", "Error when try get cost: ");
    }

    public int getTankCost(int id_tank) throws DataBaseConnectorException {
        return getCost("tanks", "id_tank", id_tank);
    }

    public int getArmorCost(int id_armor) throws DataBaseConnectorException {
        return getCost("armor", "id_armor", id_armor);
    }

    public int getEngineCost(int id_engine) throws DataBaseConnectorException {
        return getCost("engine", "id_engine", id_engine);
    }

    public int getFirstWeaponCost(int id_weapon) throws DataBaseConnectorException {
        return getCost("first_weapon", "id_weapon", id_weapon);
    }

    public int getSecondWeaponCost(int id_weapon) throws DataBaseConnectorException {
        return getCost("second_weapon", "id_weapon", id_weapon);
    }

    private void addEquipment(String type, int id_user, int id) throws DataBaseConnectorException {
        try {
            executeUpdate("insert into garage_"+type+" ("+type+", user ) values ("+id +", "+ id_user+")");
        }
        catch (Exception e){
            throw new DataBaseConnectorException("Error when adding equipment: " + e.toString());
        }

    }

    private int getUserTankID(int id_user, int id_tank) throws DataBaseConnectorException {
        return getSomeInt("SELECT id_tank FROM garage WHERE user = "+id_user+" AND tank = " + id_tank, "id_tank","Error when try get tank id: " );
    }

    private Purchase addTankToGarage(int id_user, int id_tank) throws DataBaseConnectorException {
        Purchase purchase = null;
        try {
            ResultSet tankInfo = executeQuery("SELECT first_weapon, second_weapon, armor, engine FROM tanks WHERE id_tank = "+id_tank);
            tankInfo.next();
            executeUpdate("insert into garage (user, tank, first_weapon, second_weapon, armor, engine) values (" +
                            id_user + ", " +
                            id_tank + ", " +
                            tankInfo.getInt("first_weapon") + ", " +
                            tankInfo.getInt("second_weapon") + ", " +
                            tankInfo.getInt("armor") + ", " +
                            tankInfo.getInt("engine") + ")"
            );
            addEquipment("first_weapon", id_user, tankInfo.getInt("first_weapon"));
            addEquipment("second_weapon", id_user, tankInfo.getInt("second_weapon"));
            addEquipment("armor", id_user, tankInfo.getInt("armor"));
            addEquipment("engine", id_user, tankInfo.getInt("engine"));
            purchase = new Purchase();
            purchase.setArmor(tankInfo.getInt("armor"));
            purchase.setEngine(tankInfo.getInt("engine"));
            purchase.setSecondWeapon(tankInfo.getInt("second_weapon"));
            purchase.setFirstWeapon(tankInfo.getInt("first_weapon"));
            purchase.setTank(id_tank);
            purchase.setID(getUserTankID(id_user,id_tank));
        }
        catch (Exception e){
            throw new DataBaseConnectorException("Error when adding a tank: " + e.toString());
        }
        return purchase;
    }

    public Purchase makePurchase(int id_user, int cost, String type, int id) throws DataBaseConnectorException {
        Purchase purchase = new Purchase();
        try {
            if(type == null){
                throw new DataBaseConnectorException("Shopping error ^_^ (type == null): ");
            }else if(type.equals("$tank$")){
                purchase = addTankToGarage(id_user, id);
            }else if(type.equals("$armor$")){
                addEquipment("armor", id_user, id);
                purchase.setArmor(id);
            }else if(type.equals("$engine$")){
                addEquipment("engine", id_user, id);
                purchase.setEngine(id);
            }else if(type.equals("$first_weapon$")){
                addEquipment("first_weapon", id_user, id);
                purchase.setFirstWeapon(id);
            }else if(type.equals("$second_weapon$")){
                addEquipment("second_weapon", id_user, id);
                purchase.setSecondWeapon(id);
            } else{
                throw new DataBaseConnectorException("Shopping error ^_^ (wrong type): ");
            }

            executeUpdate("UPDATE users SET money = money - "+cost+" WHERE id_user = "+ id_user);
        }
        catch (Exception e){
            throw new DataBaseConnectorException("Shopping error ^_^ : " + e.toString());
        }
        return purchase;
    }




    private int getTankIDForEquipment(String table, String column, int id) throws DataBaseConnectorException {
        return getSomeInt("SELECT tank FROM "+table+" WHERE "+column+" = "+id, "tank", "Error when we try get tank for equipment: ");
    }


    public int getTankIDForArmor(int id) throws DataBaseConnectorException {
        return getTankIDForEquipment("armor", "id_armor", id);
    }
    public int getTankIDForEngine(int id) throws DataBaseConnectorException {
        return getTankIDForEquipment("engine", "id_engine", id);
    }
    public int getTankIDForFirstWeapon (int id) throws DataBaseConnectorException {
        return getTankIDForEquipment("first_weapon", "id_weapon", id);
    }
    public int getTankIDForSecondWeapon(int id) throws DataBaseConnectorException {
        return getTankIDForEquipment("second_weapon", "id_weapon", id);
    }

    public void setUserTank(int id_user, int id_tank) throws DataBaseConnectorException {
        try {
            executeUpdate("UPDATE users SET tank = "+id_tank+" WHERE id_user = "+id_user);
        } catch (Exception e) {
            throw new DataBaseConnectorException("Error in changing tank in table 'users' " + e.toString());
        }
    }

    public void addScores(int id_user, int scores) throws DataBaseConnectorException {
        try {
            executeUpdate("UPDATE users SET scores = scores + " +scores+ " WHERE id_user = "+id_user);
        } catch (Exception e) {
            throw new DataBaseConnectorException("Error in changing scores in table 'users' " + e.toString());
        }
    }

    public void addMoney(int id_user, int money) throws DataBaseConnectorException {
        try {
            if(money > 0){
                executeUpdate("UPDATE users SET money = money + " +money+ " WHERE id_user = "+id_user);
            }
            else
                throw new Exception("Wrong money add");
        } catch (Exception e) {
            throw new DataBaseConnectorException("Error in changing scores in table 'users' " + e.toString());
        }
    }


    public String getUserName(int id_user) throws DataBaseConnectorException {
        String username;
        try {
            ResultSet rs = executeQuery("SELECT username FROM users WHERE id_user = " + id_user);
            rs.next();
            username = rs.getString("username");
        } catch (Exception e) {
            throw new DataBaseConnectorException("Error in getting name: " + e.toString());
        }
        return username;
    }

    public void deleteUser(int id_user) throws DataBaseConnectorException {
        try {
            executeUpdate("delete from users where id_user = "+ id_user);
        }
        catch (Exception e){
            throw new DataBaseConnectorException("Error we can't delete this User" + e.toString());
        }
    }

    public void addUser(String username, String password, String mail) throws DataBaseConnectorException {

        try {
            executeUpdate("insert into users (username, password, mail) values (\"" + username + "\",\"" + password + "\",\"" + mail + "\");");
        }
        catch (SQLIntegrityConstraintViolationException e){
            throw new DataBaseConnectorExistException("Such User \"" + username + "\" or mail \"" + mail + "\" already exist!");
        }
        catch (Exception e){
            throw new DataBaseConnectorException("Error we can't add this User " + e.toString());
        }

    }

    public int checkUser(String username, String password) throws DataBaseConnectorException {
        return getSomeInt("SELECT id_user FROM users WHERE username = \"" + username + "\" AND password = \"" + password + "\";", "id_user", "");
    }

    public DataBaseConnector(String login, String password) throws ClassNotFoundException, DataBaseConnectorException {
        Class.forName(driverName);
        this.login = login;
        this.password  = password;
        connect();
    }




    public static void main(String[] args){
        DataBaseConnector test = null;
        try {
            System.out.println("Enter the password:");
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
            String password = userInput.readLine();
            test = new DataBaseConnector("root", password);
        ResultSet rs = null, rm = null;


            rs = test.getAllUsers();
            test.getUserInformation(32);
            test.getUserTanks(32);
            //test.addEquipment("armor", 32, 1);



            while(rs.next()){
                System.out.print("Username: " + rs.getString("username") + "\tscores: " + rs.getString("scores")  + "\tmoney: " + rs.getString("money"));

                rm = test.getUserMail(rs.getInt("id_user"));
                rm.next();
                System.out.println(" mail: " + rm.getString("mail"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
