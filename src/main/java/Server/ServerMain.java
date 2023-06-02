package Server;

import Shared.Response;

import org.json.JSONObject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;

public class ServerMain {

    private ServerSocket serverSocket;
    private ArrayList<ClientHandler> clients = new ArrayList<ClientHandler>();

    public ServerMain(int portNumber) throws IOException
    {
        this.serverSocket = new ServerSocket(portNumber);
    }

    public void start() throws SQLException
    {
        System.out.println("start server!");
        Connection connection = connectSQL();

        while (true)
        {
            try
            {
                Socket socket = serverSocket.accept();
                System.out.println("new client connected : " + socket.getRemoteSocketAddress());
                ClientHandler handler = new ClientHandler(socket , connection);
                clients.add(handler);
                handler.start();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private Connection connectSQL() throws SQLException
    {
        String url = "jdbc:mysql://localhost:3306/steam";
        String user = "root";
        String pass = "Driver28";
        Connection connection = DriverManager.getConnection(url , user , pass);
        System.out.println("connected to database");
        return connection;
    }

    public class ClientHandler extends Thread
    {
        private Socket socket;
        private Connection connection;
        private BufferedReader in;
        private PrintWriter out;

        public ClientHandler(Socket socket , Connection connection) throws IOException
        {
            this.socket = socket;
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new PrintWriter(socket.getOutputStream() , true);
            this.connection = connection;
        }

        public void run()
        {
            try
            {
                Statement statement = connection.createStatement();
            }
            catch (SQLException e)
            {
                throw new RuntimeException(e);
            }

            this.out.println(Response.lobbyMenuResponse());
            String request;
            Statement statement = null;
            try
            {
                while ((request = in.readLine()) != null)
                {
                    if(!request.equals("null"))
                    {
                        JSONObject jsonRequest = new JSONObject(request);
                        if(jsonRequest.getString("type").equals("exit"))
                        {
                            socket.close();
                            clients.remove(this);
                        }
                        else if(jsonRequest.getString("type").equals("download"))
                        {
                            SendFiles(new JSONObject(request).getString("id") , socket);
                        }
                        String response = Response.responseCreator(jsonRequest , statement);
                        this.out.println(response);
                    }
                }
            }
            catch (IOException | SQLException e)
            {
                e.printStackTrace();
            }
            finally
            {
                try
                {
                    socket.close();
                    clients.remove(this);
                    statement.close();
                    System.out.println("Client disconnected: " + socket.getRemoteSocketAddress());
                }
                catch (IOException | SQLException e)
                {
                    e.printStackTrace();
                }
            }

        }


        public void SendFiles(String id, Socket clientsocket) {
            File file = new File("C:\\Users\\ASUS\\Desktop\\java code\\Eighth-Assignment-Steam\\src\\main\\java\\Server\\Resources" + id + ".png");
            long fileSize = file.length();

            try
            {
                FileInputStream fileInputStream = new FileInputStream(file);
                BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                DataOutputStream outputStream = new DataOutputStream(clientsocket.getOutputStream());
                outputStream.writeLong(fileSize);
                outputStream.flush();

                byte[] buffer = new byte[1024];
                int bytesRead;

                while ((bytesRead = bufferedInputStream.read(buffer)) != -1)
                {
                    outputStream.write(buffer, 0, bytesRead);
                }

            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }






    }















    public static void main(String[] args) throws IOException , SQLException
    {
        ServerMain serverMain = new ServerMain(4321);
        serverMain.start();
    }
}
