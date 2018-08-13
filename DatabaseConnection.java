package team5project.treasurehuntapp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class DatabaseConnection {

    protected static final String EXECUTE_QUERY_URL = "http://team5.x10host.com/executeQuery.php";

    public static List<String> executeQuery(String sqlQuery) {

        List<String> result = new ArrayList<String>();
        String line = "";

        try {

            //Connect to the php file that will connect to the database
            URL url = new URL(EXECUTE_QUERY_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);

            OutputStream output = conn.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(output, "UTF-8"));

            String post_data = URLEncoder.encode("sqlQuery","UTF-8") + "=" + URLEncoder.encode(sqlQuery,"UTF-8");

            bufferedWriter.write(post_data);
            bufferedWriter.flush();
            bufferedWriter.close();
            output.close();

            InputStream input = conn.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(input,"iso-8859-1"));

            line = bufferedReader.readLine();

            //Read in the data from the output of the php file and put into a list of strings
            if(line != null)
            for(int i = 0; i < line.split("<br>").length; i++) {
                result.add(line.split("<br>")[i]);
            } else {
                result.add("nothing returned");
            }

            bufferedReader.close();
            input.close();
            conn.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;

    }

    //A method to make it easier to use the results from the database
    public static String parseResultSet(List<String> result, int line, int field) {

        try {
            return result.get(line).split("\\|")[field];
        }
        catch(IndexOutOfBoundsException ioobe) {
            ioobe.printStackTrace();
            return "Error";
        }

    }

}
