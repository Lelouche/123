package ch.ethz.inf.vs.lubu.cyrptdbmodule.conn;

import org.json.JSONException;
import org.json.JSONObject;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.main.JSONResultScheme;

/**
 * Created by lukas on 20.04.15.
 * Implements a JSON Reply from the server
 */
public class CDBReplyJSON implements ICDBReply {

    JSONObject reply;

    public CDBReplyJSON(JSONObject jo) {
        this.reply = jo;
    }

    @Override
    public boolean hasError() {
        String status;
        if(reply == null)
            return false;
        try {
            status = JSONResultScheme.getResultStatus(reply);
        } catch (JSONException e) {
            return true;
        }
        if (status == null)
            return true;

        return status.equals(JSONResultScheme.HAS_ERROR);
    }

    @Override
    public String getErrorMessage() {
        if (!hasError())
            return "";
        try {
            return JSONResultScheme.getErrorMsg(reply);
        } catch (JSONException e) {
            return "";
        }
    }

    @Override
    public JSONObject getDataMessage() {
        return reply;
    }
}
