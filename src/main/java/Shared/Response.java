package Shared;


import Server.InsertData;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Locale;

public class Response
{

    public static String responseCreator(JSONObject request,Statement statement) throws SQLException, IOException {
        String type = request.getString("type");

        if (type.equals("lobby menu")){
            return lobbyMenuResponse();
        }

        else if (type.equals("user menu")){
            return userMenuResponse(request);
        }

        else if(type.equals("sign up")){
            return signUpResponseCreator(doesUserExist(request,statement),statement,request);
        }

        else if (type.equals("log in")){
            return logInResponseCreator(doesUserExist(request,statement),statement,request);
        }

        else if (type.equals("view games")){
            return viewGameListResponse(statement, request);
        }

        else if (type.equals("view details")){
            return viewDetailsResponse(request,statement);
        }

        else if (type.equals("download")){
            return downloadResponse(request,statement);
        }

        else if (type.equals("search")){
            return searchResponse(request,statement);
        }

        return null;
    }

    private static String searchResponse(JSONObject request, Statement statement) throws SQLException {
        JSONObject json = new JSONObject();
        JSONObject result = new JSONObject();
        ArrayList<String> columns = columnNames(statement);

        json.put("type","search");
        json.put("user",request.getJSONObject("user"));

        ResultSet resultSet = statement.executeQuery("SELECT * FROM games WHERE LOWER(title) LIKE '%" +
                request.getString("title").toLowerCase(Locale.ROOT) + "%'");
        resultSet.next();

        while (!resultSet.isAfterLast()){
            JSONObject details = new JSONObject();
            for (int j=0;j<8;j++){
                String column = columns.get(j);
                details.put(column,resultSet.getString(column));
            }
            result.put(resultSet.getString("id"),details);
            resultSet.next();
        }
        json.put("games",result);

        return json.toString();

    }

    public static String viewDetailsResponse(JSONObject request,Statement statement) throws SQLException {
        String id = request.getString("id");
        JSONObject json = new JSONObject();
        json.put("type","view details");
        json.put("id",request.getString("id"));
        json.put("user",request.getJSONObject("user"));

        JSONObject details = new JSONObject();
        ResultSet result = statement.executeQuery("SELECT * FROM games WHERE id = '" + id + "'");
        result.next();

        details.put("title", result.getString("title"));
        details.put("developer", result.getString("developer"));
        details.put("genre", result.getString("genre"));
        details.put("price", result.getString("price"));
        details.put("release_year", result.getString("release_year"));
        details.put("controller_support", result.getString("controller_support"));
        details.put("reviews", result.getString("reviews"));
        details.put("size", result.getString("size") + " GB");

        json.put("details",details);

        return json.toString();
    }


    private static String viewGameListResponse(Statement statement, JSONObject request) throws SQLException {
        JSONObject json = new JSONObject();

        json.put("type", "view game list");
        json.put("user",request.getJSONObject("user"));
        ArrayList<String> columns = columnNames(statement);

        ResultSet result = statement.executeQuery("SELECT * FROM games");
        result.next();

        JSONObject games = new JSONObject();

        while (!result.isAfterLast()){
            JSONObject details = new JSONObject();
            for (int j=0;j<8;j++){
                String column = columns.get(j);
                details.put(column,result.getString(column));
            }
            games.put(result.getString("id"),details);
            result.next();
        }
        json.put("games",games);

        return json.toString();
    }

    private static String logInResponseCreator(String doesUserExist, Statement statement, JSONObject request) throws SQLException {
        JSONObject json = new JSONObject();
        json.put("type", "log in");
        JSONObject user = request.getJSONObject("user");

        if (doesUserExist.equals("true")){
            ResultSet result = statement.executeQuery("SELECT * FROM users WHERE username = '" + user.getString("username") + "'");
            result.next();

            if (result.getString("password").equals(user.getString("password"))){
                user.put("date",result.getString("date_of_birth"));
                user.put("id",result.getString("id"));
                json.put("user",user);
                json.put("status", "true");
            }

            else{
                json.put("status", "false");
                json.put("reason","password is incorrect");
            }
        }

        else{
            json.put("status", "false");
            json.put("reason","no user found with such username");
        }

        return json.toString();
    }

    public static String lobbyMenuResponse(){
        JSONObject json = new JSONObject();

        json.put("type","lobby menu");

        return json.toString();
    }

    public static String userMenuResponse(JSONObject request){
        JSONObject json = new JSONObject();

        json.put("type","user menu");
        json.put("user",request.getJSONObject("user"));

        return json.toString();
    }

    public static String downloadResponse(JSONObject request,Statement statement) throws IOException, SQLException {
        InsertData.insertDownload(request,statement,downloadCount(request,statement));
        JSONObject json = new JSONObject();
        json.put("type","user menu");
        json.put("user", request.getJSONObject("user"));

        String id = request.getString("id");
        FileChannel src = new FileInputStream(
                "C:\\Users\\ASUS\\Desktop\\java code\\Eighth-Assignment-Steam\\src\\main\\java\\Server\\Resources" + id + ".png").getChannel();

        File folder = new File("C:\\Users\\ASUS\\Desktop\\java code\\Eighth-Assignment-Steam\\src\\main\\java\\Client\\Downloads\\");
        File[] listOfFiles = folder.listFiles();
        ArrayList<String> fileNames = new ArrayList<>();

        for (File file:listOfFiles){
            if (file.getName().endsWith(".png")) {
                fileNames.add(file.getName().substring(0,file.getName().length() - 4));
            }
        }

        int i = 1;
        String plainId = id;
        while (fileNames.contains(id)){
            id = plainId + " (" + i + ")";
            i++;
        }

        return json.toString();

    }

    public static String signUpResponseCreator(String doesUserExist, Statement statement,JSONObject request) throws SQLException {
        JSONObject json = new JSONObject();

        if (doesUserExist.equals("false")){
            InsertData.insertUser(request,statement);
        }

        json.put("type","sign up");

        if (doesUserExist.equals("false")) {
            json.put("status", "true");
        }

        else{
            json.put("status", "false");
        }

        json.put("user",request.getJSONObject("user"));

        return json.toString();
    }

    public static String doesUserExist(JSONObject json,Statement statement) throws SQLException {
        ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM users WHERE username = '" +
                json.getJSONObject("user").getString("username") + "'");
        result.next();

        if (result.getInt("count") == 0){
            return "false";
        }
        return "true";
    }

    public static int downloadCount(JSONObject request,Statement statement) throws SQLException {
        ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM downloads WHERE account_id = '" +
                request.getJSONObject("user").getString("id") + "' AND game_id = '" + request.getString("id") + "'");
        result.next();

        if (result.getInt("count") == 0){
            return 0;
        }

        else{
            result = statement.executeQuery("SELECT * FROM downloads WHERE account_id = '" +
                    request.getJSONObject("user").getString("id") + "' AND game_id = '" + request.getString("id") + "'");
            result.next();
            return result.getInt("download_count");
        }
    }

    public static ArrayList<String> columnNames(Statement statement) throws SQLException {
        ArrayList<String> columns = new ArrayList<>();

        for (int i=2;i<=9;i++) {
            String sql = "SELECT column_name FROM information_schema.columns\n" +
                    "WHERE table_name = '" + "games" + "' AND ordinal_position = " + i + ";";
            ResultSet result = statement.executeQuery(sql);
            result.next();
            columns.add(result.getString("column_name"));
        }

        return columns;
    }




}
