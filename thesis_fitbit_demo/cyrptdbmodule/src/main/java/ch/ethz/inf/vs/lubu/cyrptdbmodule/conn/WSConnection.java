package ch.ethz.inf.vs.lubu.cyrptdbmodule.conn;

import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.crypto.IKeyManager;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.crypto.mOPEClient;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.crypto.mOPEInteractionResult;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.crypto.mOPEJob;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.exceptions.CDBException;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.util.Setting;

/**
 * Created by lukas on 23.03.15.
 * Implements the REST Client to post queries to the Server
 */
public class WSConnection implements IDBConnection {

    private String username;
    private String secret;

    private URL serverUrl;

    private URL serverUrlOPE;

    private URL serverUrlPlain;

    private static final String WS_QUERY_URL = "/askQuery";
    private static final String WS_QUERYOPE_URL = "/askOPEQuery";
    private static final String WS_QUERYPLAIN_URL = "/askQueryPlain";

    public WSConnection() {
    }

    @Override
    public void connect(String url, String user, String secret) throws Exception {
        this.serverUrl = new URL(url + WS_QUERY_URL);
        this.serverUrlPlain = new URL(url + WS_QUERYPLAIN_URL);
        this.serverUrlOPE = new URL(url + WS_QUERYOPE_URL);
        this.username = user;
        this.secret = secret;
    }

    @Override
    public ICDBReply executeQuery(String query) throws CDBException {
        JSONObject res;
        JSONObject queryJ = new JSONObject();
        try {
            queryJ.put("Query", query);
            queryJ.put("Type", "Query");
            if(Setting.CRYPTO_ON)
                res = executeQueryOnWS(this.serverUrl,queryJ);
            else
                res = executeQueryOnWS(this.serverUrlPlain,queryJ);
        } catch (Exception e) {
            throw new CDBException("Executing on WebServer failed" + e.getMessage());
        }
        CDBReplyJSON reply = new CDBReplyJSON(res);
        return reply;
    }

    @Override
    public ICDBReply executemOPEStmt(mOPEJob job, IKeyManager keyManager) throws CDBException {
        JSONObject res, queryJ;
        try {
            queryJ = job.toJSON();
            res = executeQueryOnWS(this.serverUrlOPE, queryJ);
        } catch (Exception e) {
            throw new CDBException("Executing on WebServer failed" + e.getMessage());
        }
        CDBReplyJSON reply = new CDBReplyJSON(res);
        if(reply.hasError())
            throw new CDBException("Executing on WebServer failed" + reply.getErrorMessage());

        //startInteraction
        mOPEInteractionResult result = null;
        try {
            result = mOPEClient.interactWithOPEServer(keyManager, job, this.serverUrlOPE.getHost(), Setting.mOPE_PORT);
        } catch (Exception e) {
            throw new CDBException("Interaction failed " + e.getMessage());
        }

        if(!result.succeeded())
            throw new CDBException("Interaction failed ");

        if(result.hasResult())
            try {
                return new CDBReplyJSON(new JSONObject(result.getResult()));
            } catch (JSONException e) {
                throw new CDBException("Received non JSON Result " + e.getMessage());
            }
        else
            return null;
    }

    @Override
    public ICDBReply executeUpdate(String query) throws CDBException {
        JSONObject res;
        JSONObject queryJ = new JSONObject();
        try {
            queryJ.put("Query", query);
            queryJ.put("Type", "Update");
            if(Setting.CRYPTO_ON)
                res = executeQueryOnWS(this.serverUrl,queryJ);
            else
                res = executeQueryOnWS(this.serverUrlPlain,queryJ);
        } catch (Exception e) {
            throw new CDBException("Executing on WebServer failed" + e.getMessage());
        }
        CDBReplyJSON reply = new CDBReplyJSON(res);
        return reply;
    }

    private JSONObject executeQueryOnWS(URL url, JSONObject queryJ) throws Exception {
        JSONObject res = null;
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            urlConnection.setDoOutput(true);
            urlConnection.setChunkedStreamingMode(0);
            urlConnection.setRequestProperty("Content-Type", "application/json");
            String basicAuth = "Basic " + new String(Base64.encode((username + ":" + secret).getBytes(), Base64.NO_WRAP));
            urlConnection.setRequestProperty("Authorization", basicAuth);

            OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
            out.write(queryJ.toString().getBytes());
            out.close();

            int responseCode = urlConnection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK)
                throw new RuntimeException("Response Error");

            BufferedInputStream in = new BufferedInputStream(urlConnection.getInputStream());
            String response = getStringFromInput(in, "UTF-8");
            in.close();

            res = new JSONObject(response);

        } finally {
            urlConnection.disconnect();
        }
        return res;
    }

    private static String getStringFromInput(InputStream in, String format) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in, format));
        String cur = bufferedReader.readLine();
        while (cur != null) {
            sb.append(cur);
            cur = bufferedReader.readLine();
        }
        return sb.toString();
    }
}
