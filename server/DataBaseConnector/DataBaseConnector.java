package server.DataBaseConnector; /**
 * Created by foban on 16.07.14.
 */

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

    public ResultSet getUserTanks(int id_user) throws Exception{ //tank, armor, engine, first_weapon, second_weapon
        return executeQuery("SELECT tank, armor, engine, first_weapon, second_weapon FROM garage WHERE user = "+id_user);
    }

    public String getUserName(int id_user) throws DataBaseConnectorException { //return field username
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
            test = new DataBaseConnector("root","trin1TRON");
        ResultSet rs = null, rm = null;
            //test.addUser("zornical", "111111", "steste1r12@mama.ru");
            rs = test.getAllUsers();
            System.out.println(test.checkUser("foban", "trin1TRON"));
            test.getUserInformation(32);
            test.getUserTanks(32);

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
