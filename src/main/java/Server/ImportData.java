package Server;

import java.io.*;
import java.nio.channels.FileChannel;
import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

public class ImportData {

    public static void importTextFiles(){
        String url = "jdbc:postgresql://localhost:5432/steam";
        String user = "postgres";
        String pass = "12345";

        try {
            Connection connection = DriverManager.getConnection(url, user, pass);
            System.out.println("Connected to the PostgreSQL database!");

            Statement statement = connection.createStatement();

            File folder = new File("D:\\Eighth-Assignment-Steam\\src\\main\\java\\Server\\Resources");
            File[] listOfFiles = folder.listFiles();

            for (File file : listOfFiles) {
                if (file.isFile()) {
                    if (file.getName().endsWith(".txt")) {
                        importFile(statement,file);
                    }
                }
            }


            statement.close();
            connection.close();

        }
        catch (SQLException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void importFile(Statement statement,File file) throws FileNotFoundException, SQLException {
        Scanner myReader = new Scanner(file);
        ArrayList attributes = new ArrayList();

        while (myReader.hasNextLine()) {
            String data = myReader.nextLine();
            attributes.add(data);
        }

        attributes.add(file.getPath());

        String sql = "'";

        for (int i = 0; i < attributes.size(); i++) {
            sql = sql + attributes.get(i) + "','";
        }
        sql = sql.substring(0, sql.length() - 2);

        statement.executeUpdate("INSERT INTO games VALUES(" + sql + ")");
    }


    public static void main(String[] args) throws IOException {
        importTextFiles();
    }

}
