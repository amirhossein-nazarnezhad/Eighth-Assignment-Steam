package Server;

import org.json.JSONObject;

import java.sql.SQLException;
import java.sql.Statement;

public class InsertData {

    public static void insertUser(JSONObject request, Statement statement) throws SQLException {
        JSONObject user = request.getJSONObject("user");
        String sql = "INSERT INTO users VALUES ('" + user.getString("id") + "','" + user.getString("username") + "', '" +
                user.getString("password") + "','" + user.getString("date") + "')";
        statement.executeUpdate(sql);
    }

    public static void insertDownload(JSONObject request,Statement statement,int download_count) throws SQLException {
        JSONObject user = request.getJSONObject("user");

        String sql = "";

        if(download_count  == 0) {
            sql = "INSERT INTO downloads VALUES ('" + user.getString("id") + "','" + request.getString("id") +
                    "','" + 1 + "')";
        }

        else {
            sql = "UPDATE downloads SET download_count = " + (download_count + 1) + " WHERE account_id = '"
                    + user.getString("id") + "' AND game_id = '" + request.getString("id") + "'";
        }

        statement.executeUpdate(sql);
    }

}
