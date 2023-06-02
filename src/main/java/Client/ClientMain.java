package Client;

import Shared.Request;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Scanner;

public class ClientMain {

    private static ResourceBundle jsonRequest;

    public static void main(String[] args) {

        try
        {
            Scanner scanner = new Scanner(System.in);
            Socket socket = new Socket("localhost", 4321);
            InputStream inputStream = socket.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            OutputStream outputStream = socket.getOutputStream();
            PrintWriter printWriter = new PrintWriter(outputStream, true);
            String response;

            while ((response = bufferedReader.readLine()) != null)
            {
                if (!response.equals("null"))
                {
                    String request = Request.createRequest(new JSONObject(response), scanner);
                    JSONObject jsonObject = new JSONObject(request);
                    printWriter.println(request);
                    if (jsonRequest.getString("type").equals("download"))
                    {
                        recieveFile(socket, jsonRequest.getString("id"));
                    }
                }
            }




        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    public static void recieveFile(Socket socket, String id)
    {

        try
        {
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            Long fileSize = dataInputStream.readLong();

            File folder = new File("C:\\Users\\ASUS\\Desktop\\java code\\Eighth-Assignment-Steam\\src\\main\\java\\Client\\Downloads");
            File[] listOfFiles = folder.listFiles();
            ArrayList<String> fileNames = new ArrayList<>();

            for (File file : listOfFiles)
            {
                if (file.getName().endsWith(".png"))
                {
                    fileNames.add(file.getName().substring(0, file.getName().length() - 4));
                }
            }
            int i = 1;
            String plainId = id;
            while (fileNames.contains(id))
            {
                id = plainId + " (" + i + ")";
                i++;
            }

            String filePath = "C:\\Users\\ASUS\\Desktop\\java code\\Eighth-Assignment-Steam\\src\\main\\java\\Client\\Downloads" + id + ".png";
            FileOutputStream fileOutputStream = new FileOutputStream(filePath);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);

            byte[] buffer = new byte[1024];
            int bytesRead;
            long totalBytesRead = 0;
            while (totalBytesRead < fileSize &&
                    (bytesRead = dataInputStream.read(buffer, 0, (int) Math.min(buffer.length, fileSize - totalBytesRead))) != -1) {
                bufferedOutputStream.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
            }
            System.out.println("download complete");

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }


    }
}
