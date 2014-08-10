package server;

import server.Garage.Garage;
import server.Garage.Tank;

import java.io.*;
import java.net.*;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;

import DataBaseConnector.*;

/**
 * Created by foban on 27.07.14.
 */
public class ConnectServer {

    private static Logger log = Logger.getLogger(ConnectServer.class.getName());

    private ServerSocket serverSocket;
    private Thread serverThread;
    private GameServer gameServer;
    private DataBaseConnector dataBaseConnector;


    private int port;
    BlockingQueue<UserProcessor> userProcessorQueue = new LinkedBlockingQueue<UserProcessor>();

    public ConnectServer(int port, String password) throws IOException, DataBaseConnectorException, ClassNotFoundException {
        log.severe("Setup server...");
        serverSocket = new ServerSocket(port);
        this.port = port;
        InetAddress addr = InetAddress.getLocalHost();
        String myLANIP = addr.getHostAddress();
        log.severe("Server IP: " + myLANIP);

        log.severe("Setup game server...");
        dataBaseConnector = new DataBaseConnector("root", password);
        gameServer = new GameServer(8);
    }

    void run() {
        log.severe("Start server...");
        //gameServer.start();
        serverThread = Thread.currentThread();
        while (true) {
            Socket s = getNewConn();
            if (serverThread.isInterrupted()) {
                break;
            } else if (s != null){
                try {
                    final UserProcessor processor = new UserProcessor(s);
                    final Thread thread = new Thread(processor);
                    thread.setDaemon(true);
                    thread.start();

                    userProcessorQueue.offer(processor);
                }
                catch (IOException ignored) {}
            }
        }
    }


    private Socket getNewConn() {
        log.severe("Waiting new connection...");
        Socket s = null;
        try {
            s = serverSocket.accept();
        } catch (IOException e) {
            log.warning("Connection failed" + e.toString());
            shutdownServer();
        }
        log.severe("Get connection from " + s.toString());
        return s;
    }


    private synchronized void shutdownServer() {
        log.severe("Shutdown server...");
        for (UserProcessor s: userProcessorQueue) {
            s.close();
        }
        if (!serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException ignored) {
            }
        }
    }

    public static void main(String[] args){
        try {
            System.out.println("Enter the password:");
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
            String password = userInput.readLine();
            new ConnectServer(8080, password).run();
        } catch (Exception e) {
            log.warning("Start server failed: " + e.toString());
        }
    }


    class UserProcessor implements Runnable{//подкласс
        Socket socket;

        private boolean closed = false;

        DataOutputStream outputStream;
        DataInputStream inputStream;

        String username;
        Integer userId = null;
        int tank;
        int scores;
        int money;
        //Queue<Tank> tanks = new LinkedList<Tank>();
        Garage garage = new Garage();


        UserProcessor(Socket socketParam) throws IOException {
            socket = socketParam;

            outputStream = new DataOutputStream(socket.getOutputStream());
            inputStream = new DataInputStream(socket.getInputStream());


            //bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            //bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8") );
        }

        private synchronized void send(String text){
            try {
                outputStream.writeUTF(text);
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
                close();
            }
        }

        private synchronized void send(int num){
            try {
                outputStream.writeInt(num);
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
                close();
            }
        }

        private void getUserInformation(){
            if(userId!=null){
                try {
                    ResultSet userInfo = dataBaseConnector.getUserInformation(userId);
                    userInfo.next();
                    scores = userInfo.getInt("scores");
                    money = userInfo.getInt("money");
                    tank = userInfo.getInt("tank");

                    userInfo = dataBaseConnector.getUserTanks(userId);

                    while(userInfo.next()){
                        garage.addTank(
                                userInfo.getInt("id_tank"),
                                userInfo.getInt("tank"),
                                userInfo.getInt("armor"),
                                userInfo.getInt("engine"),
                                userInfo.getInt("first_weapon"),
                                userInfo.getInt("second_weapon")
                        );
                    }

                    userInfo = dataBaseConnector.getUserEquipment(userId);
                    while(userInfo.next()){
                        garage.addArmor(userInfo.getInt("armor"));
                        garage.addEngine(userInfo.getInt("engine"));
                        garage.addFirstWeapon(userInfo.getInt("first_weapon"));
                        garage.addSecondWeapon(userInfo.getInt("second_weapon"));
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    close();
                }
            }
        }

        private void sendUserInformation(){
            if(userId != null){
                send("$info$");
                send(money);
                send(scores);
                send(tank);
                send("$garage$");
                try {
                    garage.send(outputStream);
                } catch (Exception e) {
                    e.printStackTrace();
                    close();
                }
                send("$garage_end$");
            }
        }

        private void login(String password){
            try {
                userId = dataBaseConnector.checkUser(username, password);
                gameServer.addUser(userId, this);
                send("$login_success$");
            }
            catch (Exception e) {
                e.printStackTrace();
                userId = null;
                send("$login_failed$");
                enter();
            }
        }

        private void login(){
            String password = null;
            try {
                username = inputStream.readUTF();
                password = inputStream.readUTF();

            } catch (IOException e) {
                close();
            }
            login(password);

        }
        
        private void motion(){
        	try {
				System.out.println("X : " + inputStream.readUTF() + " Y : " + inputStream.readUTF());
			} catch (IOException e) {
				close();
			}
        }

        private void registration(String password, String mail){

            try {
                dataBaseConnector.addUser(username, password, mail);
                send("registration_success");
                login(password);
            } catch (DataBaseConnectorExistException e) {
                send("$registration_exist$");
                enter();
            } catch (DataBaseConnectorException e) {
                e.printStackTrace();
                close();
            }
        }

        private void registration(){
            String password = null, mail = null;
            try {
                username = inputStream.readUTF();
                password = inputStream.readUTF();
                mail = inputStream.readUTF();


                //username = bufferedReader.readLine();
                //password = bufferedReader.readLine();
                //mail = bufferedReader.readLine();
            } catch (IOException e) {
                close();
            }
            registration(password, mail);

        }

        private void enter(){
            String status = null;
            try {
                status = inputStream.readUTF();
                //status = bufferedReader.readLine();
            } catch (IOException e) {
                close();
            }
            //System.out.println("!!!!STATUSS!!! ---> " + status);
            if(status.equals("$login$")){
                login();
            }else if (status.equals("$registration$")){
                registration();
            }else if (status.equals("$motion$")){
            	motion();
            }
            else
                close();
        }

        private void buy(String type, int id) throws DataBaseConnectorException {
           if(type != null){
               int cost = -1;
               if(type.equals("$tank$")){
                   cost = dataBaseConnector.getTankCost(id);
               } else if(type.equals("$armor$")){
                   cost = dataBaseConnector.getArmorCost(id);
               }else if(type.equals("$engine$")){
                   cost = dataBaseConnector.getEngineCost(id);
               }else if(type.equals("$first_weapon$")){
                   cost = dataBaseConnector.getFirstWeaponCost(id);
               }else if(type.equals("$second_weapon$")){
                   cost = dataBaseConnector.getSecondWeaponCost(id);
               }

               if(money >= cost && cost != -1){
                    dataBaseConnector.makePurchase(userId, cost, type,id);
                   money -= cost;
               }else {
                   close();
               }

           }else{
               close();
           }
        }

        public void run() {
            enter();
            getUserInformation();
            sendUserInformation();

            if(!closed)System.out.println("We enter to the system");

            while (!socket.isClosed()) {
                String line = null;
                try {
                    line = inputStream.readUTF();

                    //line = bufferedReader.readLine();
                } catch (IOException e) {
                    close();
                }


                if (line == null) {
                    close();
                } else if (line.equals("$start$")){

                } else if(line.equals("$buy$")){
                    try {
                        buy(
                                inputStream.readUTF(),
                                inputStream.readInt()
                        );
                    } catch (IOException e) {
                        close();
                    } catch (DataBaseConnectorException e) {
                        e.printStackTrace();
                        send("$error$");
                        close();
                    }
                } else if(line.equals("$set$")){

                } else if(line.equals("$choose$")){

                } else if(line.equals("$close$")){
                    close();
                }

                /*("$shutdown$".equals(line)) {
                    serverThread.interrupt();
                    try {
                        new Socket("localhost", port);
                        } catch (IOException ignored) {
                    } finally {
                        shutdownServer();
                    }
                } else {
                    for (UserProcessor sp: userProcessorQueue) {
                        if(sp != this)sp.send(username +": " + line);
                    }
                }*/
            }
        }


        public synchronized void close() {
            if(!closed){
                System.out.println("Умираю :(");

                if(userId != null){
                    gameServer.deleteUser(userId);
                    userId = null;
                }


                userProcessorQueue.remove(this);
                if (!socket.isClosed()) {
                    try {
                        socket.close();
                    } catch (IOException ignored) {}
                }
                closed = true;
            }
        }


        protected void finalize() throws Throwable {
            super.finalize();
            close();
        }
    }
}
