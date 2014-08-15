package server;

import server.Garage.*;

import java.io.*;
import java.net.*;
import java.sql.ResultSet;
import java.util.concurrent.*;
import java.util.logging.Logger;

import server.DataBaseConnector.*;

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
        gameServer = new GameServer(2);
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


    /*Подклас который поддерживает связь с клиентом и обрабатывает его запросы*/
    class UserProcessor implements Runnable{
        Socket socket;

        private boolean closed = false;

        DataOutputStream outputStream;
        DataInputStream inputStream;

        String username;
        Integer userId = null;
        int currentTank;
        int scores;
        int money;
        //Queue<Tank> tanks = new LinkedList<Tank>();
        Garage garage = new Garage();
        Battle currentBattle = null;


        UserProcessor(Socket socketParam) throws IOException {
            socket = socketParam;

            outputStream = new DataOutputStream(socket.getOutputStream());
            inputStream = new DataInputStream(socket.getInputStream());


            //bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            //bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8") );
        }

        synchronized void send(String text){
            try {
                outputStream.writeUTF(text);
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
                close();
            }
        }

        synchronized void send(int num){
            try {
                outputStream.writeInt(num);
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
                close();
            }
        }

        synchronized int readInt() throws IOException {
            try {
                return inputStream.readInt();
            } catch (IOException e) {
                close();
                throw e;
            }
        }

        synchronized String readUTF() throws IOException {
            try {
                return inputStream.readUTF();
            } catch (IOException e) {
                close();
                throw e;
            }
        }

        /*Получение информации о пользователе из базы данных*/
        private void getUserInformation(){
            if(userId!=null){
                try {
                    ResultSet userInfo = dataBaseConnector.getUserInformation(userId);
                    userInfo.next();
                    scores = userInfo.getInt("scores");
                    money = userInfo.getInt("money");
                    currentTank = userInfo.getInt("tank");

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

                    userInfo = dataBaseConnector.getUserArmor(userId);
                    while(userInfo.next()){
                        garage.addArmor(userInfo.getInt("armor"));
                    }
                    userInfo = dataBaseConnector.getUserEngine(userId);
                    while(userInfo.next()){
                        garage.addEngine(userInfo.getInt("engine"));
                    }
                    userInfo = dataBaseConnector.getUserFirstWeapon(userId);
                    while(userInfo.next()){
                        garage.addFirstWeapon(userInfo.getInt("first_weapon"));
                    }
                    userInfo = dataBaseConnector.getUserSecondWeapon(userId);
                    while(userInfo.next()){
                        garage.addSecondWeapon(userInfo.getInt("second_weapon"));
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    close();
                }
            }
        }


        /*Отправка данных пользователя клиенту*/
        private void sendUserInformation(){
            if(userId != null){
                send("$info$");
                send(money);
                send(scores);
                send(currentTank);
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


        /*Вход в систему при помощи пароля и никнэйма(глобальная переменная класса)*/
        private void login(String password){
            try {
                userId = dataBaseConnector.checkUser(username, password);
                gameServer.addUser(userId, this);
                send("$login_success$");
            }
            catch (Exception e) {
                //e.printStackTrace();
                userId = null;
                send("$login_failed$");
                enter();
            }
        }

        /*Обертка над входом в систему*/
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

        /*Регистрация с последующим входом в систему при помощи пароля, почты и никнэйма(глобальная переменная класса)*/
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

        /*Обертка над регистрацией*/
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

        /*Ф-ция предоставляющая различные варианты входа в систему*/
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

        /*Покупка каких либо товаров*/
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
                   garage.addPurchase(dataBaseConnector.makePurchase(userId, cost, type,id));
                   money -= cost;
               }else {
                   close();
               }

           }else{
               close();
           }
        }

        /*Смена оружия, брони или двигателя танка пользователя*/
        private void set(String type, int id_tank, int id) throws Exception{
            int tankTypeID = garage.getTankID(id_tank);
            if(type == null){
                close();
            }else if(type.equals("$armor$")){
                if(tankTypeID == dataBaseConnector.getTankIDForArmor(id)){
                    garage.setArmor(id_tank, id);
                    dataBaseConnector.setTankArmor(id_tank,id);
                }
                else
                    throw new Exception();
            }else if(type.equals("$engine$")){
                if(tankTypeID == dataBaseConnector.getTankIDForEngine(id)){
                    garage.setEngine(id_tank, id);
                    dataBaseConnector.setTankEngine(id_tank, id);
                }
                else
                    throw new Exception();
            }else if(type.equals("$first_weapon$")){
                if(tankTypeID == dataBaseConnector.getTankIDForFirstWeapon(id)){
                    garage.setFirstWeapon(id_tank, id);
                    dataBaseConnector.setTankFirstWeapon(id_tank, id);
                }
                else
                    throw new Exception();
            }else if(type.equals("$second_weapon$")){
                if(tankTypeID == dataBaseConnector.getTankIDForSecondWeapon(id)){
                    garage.setSecondWeapon(id_tank, id);
                    dataBaseConnector.setTankSecondWeapon(id_tank, id);
                }
                else
                    throw new Exception();
            }else
                close();
        }

        /*Выбор основного танка*/
        private void choose(int id_tank) throws Exception {
            if(garage.containsTank(id_tank)){
                currentTank = id_tank;
                dataBaseConnector.setUserTank(userId, id_tank);
            }
            else
                throw new Exception("We don't have such tank");
        }

        private void battle(){

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


                //Дальше идут различные команды которые может отдать пользователь при входе в систему
                if (line == null) {
                    close();
                } else if (line.equals("$start$")){//это начало битвы
                    try {
                        currentBattle = gameServer.addUserIntoBattle(userId);
                        boolean end =false;
                        while(!end && !closed){
                            //все что в цикле сугубо для того чтоб комп не вис)
                            line = inputStream.readUTF();
                            if(line.equals("$close$"))
                                end = true;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        close();
                    }

                } else if(line.equals("$buy$")){ //это покупка танка, брони и т.д.
                    try {
                        buy(
                                inputStream.readUTF(),
                                inputStream.readInt()
                        );
                        send("buy_success");
                    } catch (IOException e) {
                        close();
                    } catch (DataBaseConnectorException e) {
                        e.printStackTrace();
                        send("$error$");
                        close();
                    }
                } else if(line.equals("$set$")){//это смена оружия брони и т.д. у танка
                    try {
                        set(
                                inputStream.readUTF(),
                                inputStream.readInt(),
                                inputStream.readInt()
                        );
                        send("set_success");
                    } catch (Exception e) {
                        e.printStackTrace();
                        close();
                    }
                } else if(line.equals("$choose$")){//выбор основного танка
                    try {
                        choose(inputStream.readInt());
                        send("choose_success");
                    } catch (Exception e) {
                        e.printStackTrace();
                        close();
                    }

                } else if(line.equals("$close$")){
                    close();
                    return;
                }
            }
        }


        /*Окончание работы с пользователем*/
        public synchronized void close() {
            if(!closed){
                System.out.println("Умираю :(");

                if(userId != null){
                    gameServer.deleteUser(userId, currentBattle);
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
