import java.io.*;
import java.net.Socket;
import java.sql.*;
import java.util.Date;
import java.util.Scanner;

public class Client {
    private Socket socket;
    private  boolean online;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;
    private String password;

    public void setUsername(String uname)
    {
        username=uname;
    }
    public void setPassword(String pass)
    {
        password=pass;
    }

    //Client class constructor
    public Client(Socket socket)
    {
        try{
            this.socket=socket;
            this.bufferedWriter=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader=new BufferedReader(new InputStreamReader(socket.getInputStream()));

        } catch (IOException e)
        {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    //function to listen for messages from other users
    public void listenForMessage(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String msgFromGroupChat;

                while (socket.isConnected()){
                    try{

                        msgFromGroupChat=bufferedReader.readLine();
                       if (msgFromGroupChat.length() > 0 && msgFromGroupChat.charAt(0)!='\\')
                        {
                            System.out.println(msgFromGroupChat);
                        }

                    }catch (IOException e)
                    {
                        closeEverything(socket,bufferedReader,bufferedWriter);
                    }
                }
            }
        }).start();
    }
    //function to send messages to other users
    public void sendMessage(String roomName){
        try{
            bufferedWriter.write(username);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            Scanner scanner=new Scanner(System.in);
            while(socket.isConnected()){

                String messageToSend =scanner.nextLine();
                bufferedWriter.write(username+": "+messageToSend);

                //code to input into file/room
                FileWriter myWriter = new FileWriter(roomName+".txt",true);
                myWriter.write("Line Added on: " + new Date()+"\n"+username+": "+messageToSend+"\n");
                myWriter.close();

                //code for commands
                if(messageToSend.contains("\\help"))
                {
                    System.out.print("Leave(\\leave), list(\\list)");
                    String instruction=scanner.nextLine();
                    if(instruction.equals("\\leave"))
                    {
                        bufferedWriter.write(username+" has left the chat");
                        System.out.println("you left the chat");
                        dashboard();
                    }
                    if (instruction.contains("\\list")) {
                        bufferedWriter.write("\\list");
                        bufferedWriter.newLine();
                        bufferedWriter.flush();
                    }
                }
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        }catch (IOException e){
            closeEverything(socket,bufferedReader,bufferedWriter);
        }
    }

    public void setOnline(boolean ol)
    {
        online=ol;
    }


    //function checks if a room exists or not
    public boolean roomExists(String roomName)
    {
        File f=new File(roomName+".txt");
        return f.exists();
    }

    //this function creates a room(file) in which the chats are saved
    public void createRoom(String roomName)
    {
        try {
            File myObj = new File(roomName+".txt");

            if (myObj.createNewFile()) {
                System.out.println("Room created: " + roomName);
            } else {
                System.out.println("Welcome");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }


    //this function closes all reader and writers
    public void closeEverything(Socket socket,BufferedReader bufferedReader, BufferedWriter bufferedWriter){
        try{
            if(bufferedReader!=null){
                bufferedReader.close();
            }
            if(bufferedWriter!=null){
                bufferedWriter.close();
            }
            if(socket!=null)
            {
                socket.close();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    // this function loads the messages in that room
    public void loadMessages(String roomName)
    {
        try (BufferedReader br = new BufferedReader(new FileReader(roomName+".txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //this method checks if a username or password exists in the database
    public boolean LoginSuccess()
    {
        boolean a=false;
        try {
            Class.forName("org.postgresql.Driver");
            Connection c= DriverManager.getConnection("jdbc:postgresql://localhost:5432/test","postgres","ajay2022");

            if(c!=null)
            {
                PreparedStatement p=c.prepareStatement("SELECT * FROM db WHERE user_name=? AND password=?");
                p.setString(1,username);
                p.setString(2,password);
                ResultSet r=p.executeQuery();
                a=r.next();

                if (!a) {
                    System.out.println("please try again");
                }
                return a;
            }
            else {
                System.out.println("Connection failed");
            }
        }catch (ClassNotFoundException e)
        {
            System.out.println("Class not found");
        }
        catch (SQLException e)
        {
            System.out.println(e);
        }
        return a;
    }

    //this function registers an unregistered user
    public void register()
    {
        System.out.println("please register below: ");
        Scanner scanner=new Scanner(System.in);
        System.out.print("Enter Username: ");
        String username=scanner.nextLine();
        System.out.print("Enter password: ");
        String password=scanner.nextLine();
        if(userExits(username))
        {
            System.out.println("User already Exists");
            login();
        }
        else {
            try {
                Class.forName("org.postgresql.Driver");
                Connection c= DriverManager.getConnection("jdbc:postgresql://localhost:5432/test","postgres","ajay2022");

                if(c!=null)
                {
                    PreparedStatement p=c.prepareStatement("INSERT INTO db(user_name,password) VALUES(?,?);");
                    p.setString(1,username);
                    p.setString(2,password);
                    int i=p.executeUpdate();
                    c.close();
                    if (i==1) {
                        System.out.println("Registered");
                        dashboard();
                    }
                    else {
                        System.out.println("already registered");
                    }
                }
                else {
                    System.out.println("Connection failed");
                }
            }catch (ClassNotFoundException e)
            {
                System.out.println("Class not found");
            }
            catch (SQLException e)
            {
                System.out.println(e);
            }
        }

    }

    public boolean getOnline()
    {
        return online;
    }

    //this method updates a user's username and password
    public void update()
    {
        System.out.println("Update username and password");
        Scanner scanner=new Scanner(System.in);
        System.out.print("change username: ");
        String uname=scanner.nextLine();
        System.out.print("Change password: ");
        String pass=scanner.nextLine();
        try {
            Class.forName("org.postgresql.Driver");
            Connection c= DriverManager.getConnection("jdbc:postgresql://localhost:5432/test","postgres","ajay2022");

            if(c!=null)
            {
                PreparedStatement p=c.prepareStatement("UPDATE db SET user_name=?, password=? WHERE user_name=?");
                p.setString(1,uname);
                p.setString(2,pass);
                p.setString(3,username);
                int i=p.executeUpdate();
                c.close();
                if(i==1)
                {
                    System.out.println("Account Updates Successfully");
                    setUsername(uname);
                    setPassword(pass);
                    dashboard();
                }
                else{
                    System.out.println("error occurred");
                }
            }
            else {
                System.out.println("Connection failed");
            }
        }catch (ClassNotFoundException e)
        {
            System.out.println("Class not found");
        }
        catch (SQLException e)
        {
            System.out.println(e);
        }

    }
    public void dashboard()
    {
        Scanner scanner=new Scanner(System.in);
        String roomName="";
        System.out.println("join(J) or create(C) or Update(U) or LogOut(L)");
        String instruction=scanner.nextLine();

        while(getOnline()) {

            if (instruction.equals("J")) {
                System.out.print("Enter the room name: ");
                roomName = scanner.nextLine();

                if (roomExists(roomName)) {
                    System.out.println("Welcome to room: " + roomName);
                    loadMessages(roomName);
                        listenForMessage();
                        sendMessage(roomName);
                }
            } else if (instruction.equals("C")) {
                System.out.print("Enter room name: ");
                roomName = scanner.nextLine();
                if (roomName.equals("")) {
                    System.out.print("Enter a valid room name");
                } else {
                    createRoom(roomName);
                    System.out.println("Welcome to room: " + roomName);
                    listenForMessage();
                    sendMessage(roomName);
                }
            } else if(instruction.equals("U"))
            {
                update();
            }
            else if(instruction.equals("L"))
            {
                System.out.println("You have successfully logged out");
                first_page();
            }
            else {
                System.out.println("Enter a valid instruction");
                dashboard();
            }
        }
    }

    public boolean userExits(String uname)
    {
        boolean a=false;
        try {
            Class.forName("org.postgresql.Driver");
            Connection c= DriverManager.getConnection("jdbc:postgresql://localhost:5432/test","postgres","ajay2022");

            if(c!=null)
            {
                PreparedStatement p=c.prepareStatement("SELECT ? FORM db");
                p.setString(1,uname);
                ResultSet r=p.executeQuery();
                c.close();
                 a=r.next();
            }
            else {
                System.out.println("Connection failed");
            }
        }catch (ClassNotFoundException e)
        {
            System.out.println("Class not found");
        }
        catch (SQLException e)
        {
            System.out.println(e);
        }
        return a;
    }

public boolean login()
{
    Scanner scanner=new Scanner(System.in);
    System.out.print("Enter Username: ");
    String username=scanner.nextLine();
    System.out.print("Enter password: ");
    String password=scanner.nextLine();
    setPassword(password);
    setUsername(username);
    if(LoginSuccess())
    {
        System.out.println("Login Successfully");
        return true;
    }
    else {
        System.out.println("please try again, incorrect username or password");
        return login();
    }
}

public void first_page()
{
    Scanner scanner=new Scanner(System.in);
    System.out.println("Login(L) or Register(R)");
    String s=scanner.nextLine();
    if(s.equals("L"))
    {
        if(login()){
            setOnline(true);
            dashboard();
        }
    }
    else if(s.equals("R"))
    {
        register();
    }
}

    public static void main(String[] args) throws IOException {

        Scanner scanner=new Scanner(System.in);
        Socket socket=new Socket("localhost",1234);
        Client client=new Client(socket);
        client.first_page();
    }
}
