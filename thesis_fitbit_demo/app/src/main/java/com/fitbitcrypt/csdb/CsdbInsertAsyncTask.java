package com.fitbitcrypt.csdb;

import android.os.AsyncTask;
import android.util.Log;

import com.fitbitcrypt.Constants;
import com.fitbitcrypt.db_util.HeartRateTuple;
import com.fitbitcrypt.db_util.Tuple;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.exceptions.CDBException;

/**
 * Created by ajpeacock0_desktop on 4/10/2015.
 */
public class CsdbInsertAsyncTask extends AsyncTask<Void, Void,Void> {
    private final String TAG = CsdbInsertAsyncTask.this.getClass().getSimpleName();

    private Tuple input;

    public CsdbInsertAsyncTask(Tuple input) {
        this.input = input;
    }

    @Override
    protected Void doInBackground(Void... value) {
        DBStoreInterface store = DBInterfaceHolder.getCon();
        Log.d(TAG, "input type: "+input.getType());
        try {
            if (input.getType().equals(Constants.REQUEST_HEART_DATA)) {
                store.storeHeartRate((HeartRateTuple) input);
            }
        } catch (CDBException e) {
            e.printStackTrace();
        }
        return null;
    }
}
