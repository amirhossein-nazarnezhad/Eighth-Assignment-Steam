package Shared;

import Client.Menus;
import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public class Request
{

    public static String createRequest(JSONObject response,Scanner scan){
        String type = response.getString("type");

        if (type.equals("lobby menu")){
            return createLobbyMenuRequests(scan);
        }

        else if (type.equals("user menu")){
            return createUserMenuRequests(scan,response);
        }

        else if (type.equals("sign up")){
            if (response.get("status").equals("true")){
                return showUserMenuRequest(response);
            }

            else if (response.get("status").equals("false")){
                System.out.println("a user with this username already exists\ndo you want to try again? y/n");
                if (scan.nextLine().equals("y")){
                    return createSignUpRequest(scan);
                }

                else{
                    return showLobbyMenuRequest();
                }
            }
        }

        else if (type.equals("log in")){
            if (response.getString("status").equals("true")){
                return showUserMenuRequest(response);
            }

            else {
                System.out.println(response.getString("reason"));
                System.out.println("do you want to try again? y/n");

                if (scan.nextLine().equals("y")){
                    return createLogInRequest(scan);
                }

                else{
                    return showLobbyMenuRequest();
                }
            }
        }

        else if (type.equals("view game list")){
            return gameListRequest(response, scan);
        }

        else if (type.equals("view details")){
            printGameDetails(response);
            System.out.println("do you want to download this game? y/n");
            if (scan.nextLine().equals("y")){
                return downloadGameRequest(response.getString("id"),response);
            }

            else{
                return showUserMenuRequest(response);
            }
        }

        else if (type.equals("search")){
            return gameListRequest(response,scan);
        }


        return null;


    }

    private static void printGameDetails(JSONObject response) {
        JSONObject details = response.getJSONObject("details");

        System.out.println("name: " + details.getString("title"));
        System.out.println("developer: " + details.getString("developer"));
        System.out.println("genre: " + details.getString("genre"));
        System.out.println("price: " + details.getString("price"));
        System.out.println("release year: " + details.getString("release_year"));
        System.out.println("controller support: " + details.getString("controller_support"));
        System.out.println("reviews: " + details.getString("reviews"));
        System.out.println("size: " + details.getString("size"));
    }

    private static String gameListRequest(JSONObject response,Scanner scan) {
        JSONObject games = response.getJSONObject("games");
        List<String> ids =List.copyOf(games.keySet());

        for(int i=0;i<ids.size();i++){
            JSONObject game = games.getJSONObject(ids.get(i));
            System.out.println(i+1 + ". " + game.getString("title") + ", " + game.getString("genre") +
                    ", " + game.getString("reviews"));
        }

        System.out.println("do you want to choose a game? y/n");
        if (scan.nextLine().equals("y")){
            System.out.println("insert a number");
            String gameId = ids.get(Integer.parseInt(scan.nextLine()) - 1);
            System.out.println("what do you want to do with " + games.getJSONObject(gameId).getString("title") + "?");
            System.out.println("1.view details\n2.download");
            switch (Integer.parseInt(scan.nextLine())){
                case 1:
                    return viewDetailRequest(gameId,response);

                case 2:
                    return downloadGameRequest(gameId,response);
            }
        }

        else{
            return showUserMenuRequest(response);
        }

        return null;
    }

    private static String downloadGameRequest(String gameId, JSONObject response) {
        JSONObject json = new JSONObject();

        json.put("type","download");
        json.put("id",gameId);
        json.put("user",response.getJSONObject("user"));

        return json.toString();
    }

    private static String viewDetailRequest(String gameId, JSONObject response) {
        JSONObject json = new JSONObject();

        json.put("type","view details");
        json.put("id",gameId);
        json.put("user",response.getJSONObject("user"));

        return json.toString();
    }

    private static String createUserMenuRequests(Scanner scan, JSONObject response) {
        JSONObject user = response.getJSONObject("user");

        System.out.println(user.getString("username"));
        System.out.println(Menus.userMenu());
        System.out.println("insert a number");
        int ch = Integer.parseInt(scan.nextLine());

        switch (ch){
            case 1:
                return viewGameListRequest(response);

            case 2:
                return searchGameRequest(response,scan);

            case 3:
                return showLobbyMenuRequest();
        }

        return null;
    }

    private static String searchGameRequest(JSONObject response,Scanner scan) {
        JSONObject json = new JSONObject();

        json.put("type","search");
        json.put("user",response.getJSONObject("user"));


        System.out.println("insert the title of the game you want to search");
        json.put("title",scan.nextLine());

        return json.toString();
    }

    private static String viewGameListRequest(JSONObject response) {
        JSONObject json = new JSONObject();

        json.put("type", "view games");
        json.put("user", response.getJSONObject("user"));

        return json.toString();
    }


    public static String showLobbyMenuRequest(){
        JSONObject json = new JSONObject();

        json.put("type","lobby menu");

        return json.toString();
    }

    public static String showUserMenuRequest(JSONObject response){
        JSONObject json = new JSONObject();

        json.put("type","user menu");
        json.put("user",response.getJSONObject("user"));

        return json.toString();
    }

    public static String createLobbyMenuRequests(Scanner scan){
        System.out.println(Menus.lobbyMenu());
        System.out.println("insert a number");
        int ch = Integer.parseInt(scan.nextLine());

        switch (ch){
            case 1:
                return createSignUpRequest(scan);

            case 2:
                return createLogInRequest(scan);

            case 3:
                return exitRequest();
        }
        return null;
    }

    private static String exitRequest() {
        JSONObject json = new JSONObject();
        json.put("type","exit");

        return json.toString();
    }

    private static String createLogInRequest(Scanner scan) {
        JSONObject json = new JSONObject();
        JSONObject user = new JSONObject();
        json.put("type","log in");

        System.out.println("insert your username");
        String username = scan.nextLine();
        user.put("username",username);

        String fixedSalt = "$2a$10$5YX0fWlR/.MnPFOrWZWJ8u";
        System.out.println("insert your password");
        String password = BCrypt.hashpw(scan.nextLine(), fixedSalt);
        user.put("password",password);

        json.put("user",user);

        return json.toString();
    }

    public static String createSignUpRequest(Scanner scan){
        JSONObject json = new JSONObject();
        json.put("type","sign up");

        System.out.println("insert your username");
        String username = scan.nextLine();

        String fixedSalt = "$2a$10$5YX0fWlR/.MnPFOrWZWJ8u";
        System.out.println("insert your password");
        String password = BCrypt.hashpw(scan.nextLine(), fixedSalt);

        System.out.println("insert your birthday (YYYY-MM-DD)");
        String date = scan.nextLine();

        UUID uuid= UUID.randomUUID();

        json.put("user",userTOJson(username,password,date,uuid.toString()));

        return json.toString();
    }

    public static JSONObject userTOJson(String username,String password,String date,String id){
        JSONObject json = new JSONObject();

        json.put("id",id);
        json.put("username",username);
        json.put("password",password);
        json.put("date",date);

        return json;
    }










}
