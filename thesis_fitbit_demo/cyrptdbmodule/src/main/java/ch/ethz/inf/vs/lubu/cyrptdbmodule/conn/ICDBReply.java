package ch.ethz.inf.vs.lubu.cyrptdbmodule.conn;

import org.json.JSONObject;

/**
 * Created by lukas on 20.04.15.
 * Represents a Relply from the server
 */
public interface ICDBReply {

    public boolean hasError();

    public String getErrorMessage();

    public JSONObject getDataMessage();
}
