package icm.com.rushrush.tasks;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by jgravato on 13/12/2016.
 */

public class DownloadDirectionsUrl extends AsyncTask<String, Void, String> {

    @Override
    protected String doInBackground(String... params) {
        String result = "";
        result = downloadUrl(params[0]);
        return result;
    }

    @Override
    protected void onPostExecute(String s) {
        //super.onPostExecute(s);
    }

    public String getResult(String... params) {
        String result = "";
        result = downloadUrl(params[0]);
        return result;
    }

    private String downloadUrl(String urlStr) {
        String result = "";
        HttpsURLConnection urlCon;
        InputStream in;
        BufferedReader br;
        StringBuffer sb;
        URL url;

        try {
            url = new URL(urlStr);
            urlCon = (HttpsURLConnection) url.openConnection();
            urlCon.connect();
            in = urlCon.getInputStream();
            br = new BufferedReader(new InputStreamReader(in));
            sb = new StringBuffer();

            String line = "";
            while((line = br.readLine()) != null) {
                sb.append(line);
            }

            result = sb.toString();

            br.close();
            in.close();
            urlCon.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }


}
