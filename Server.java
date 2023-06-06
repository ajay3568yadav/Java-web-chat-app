
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
    private final ServerSocket serverSocket;
    private static ArrayList<String> usersOnline=new ArrayList<>();

    protected int count=0;

    public Server(ServerSocket serverSocket)
    {
        this.serverSocket=serverSocket;
    }

    public static String getUsersList()
    {
        return String.join(", ", usersOnline);
    }
    public void startServer()
    {
        try {
            while(!serverSocket.isClosed())
            {
                Socket socket=serverSocket.accept();

                ClientHandler clientHandler=new ClientHandler(socket);
                System.out.println(clientHandler.getClientUsername()+ " has connected");
                usersOnline.add(clientHandler.getClientUsername());
                count++;
                Thread thread =new Thread(clientHandler);
                thread.start();
            }
        }catch (IOException ex){
            System.out.println("Error");
        }
    }
    public void closeServerSocket()
    {
        try{
            if(serverSocket!=null)
            {
                serverSocket.close();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException{
        ServerSocket serverSocket=new ServerSocket(1234);
        Server server=new Server(serverSocket);
        server.startServer();
    }
}
