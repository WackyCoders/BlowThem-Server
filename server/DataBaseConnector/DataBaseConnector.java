package server.DataBaseConnector; /**
 * Created by foban on 16.07.14.
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.*;
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



    /*public ResultSet getAllUsersPrivate() throws Exception {
        return executeQuery("SELECT * FROM users");
    }*/

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

    public ResultSet getUserEquipment(int id_user) throws Exception{ //armor, engine, first_weapon, second_weapon
        return executeQuery("SELECT armor, engine, first_weapon, second_weapon FROM garage_armor, garage_engine, garage_first_weapon, garage_second_weapon \n" +
                "where garage_armor.user = garage_engine.user = garage_first_weapon.user = garage_second_weapon.user = "+id_user);
    }

    private int getCost(String table, String column, int id) throws DataBaseConnectorException {
        int cost;
        try {
            ResultSet rs = executeQuery("SELECT cost FROM "+table+" WHERE "+column+" = " + id);
            rs.next();
            cost = rs.getInt("cost");
        } catch (Exception e) {
            throw new DataBaseConnectorException("Error when try get cost: " + e.toString());
        }
        return cost;
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

    private void addTankToGarage(int id_user, int id_tank) throws DataBaseConnectorException {

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
        }
        catch (Exception e){
            throw new DataBaseConnectorException("Error when adding a tank: " + e.toString());
        }

    }

    public void makePurchase(int id_user, int cost, String type, int id) throws DataBaseConnectorException {

        try {
            if(type == null){
                throw new DataBaseConnectorException("Shopping error ^_^ (type == null): ");
            }else if(type.equals("$tank$")){
                addTankToGarage(id_user, id);
            }else if(type.equals("$armor$")){
                addEquipment("armor", id_user, id);
            }else if(type.equals("$engine$")){
                addEquipment("engine", id_user, id);
            }else if(type.equals("$first_weapon$")){
                addEquipment("first_weapon", id_user, id);
            }else if(type.equals("$second_weapon$")){
                addEquipment("second_weapon", id_user, id);
            } else{
                throw new DataBaseConnectorException("Shopping error ^_^ (wrong type): ");
            }

            executeUpdate("UPDATE users SET money = money - "+cost+" WHERE id_user = "+ id_user);
        }
        catch (Exception e){
            throw new DataBaseConnectorException("Shopping error ^_^ : " + e.toString());
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

    public Integer checkUser(String username, String password) throws Exception{
        Integer id_user = null;
        ResultSet rs = executeQuery("SELECT id_user FROM users WHERE username = \"" + username + "\" AND password = \"" + password + "\";");
        rs.next();
        id_user = rs.getInt("id_user");
        return id_user;
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
            rs = test.getUserEquipment(32);
            ResultSetMetaData rmd = rs.getMetaData();
            System.out.println(rmd.getColumnName(1));
            while(rs.next()){
                System.out.print(rs.getInt("armor"));

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
