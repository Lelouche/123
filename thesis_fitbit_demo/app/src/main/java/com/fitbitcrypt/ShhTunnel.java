package com.fitbitcrypt;

import android.os.AsyncTask;
import android.util.Log;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Properties;

/**
 * Created by ajpeacock0_desktop on 24/09/2015.
 */
public class ShhTunnel extends AsyncTask<Void, Void, Void> {
    private final String TAG = ShhTunnel.this.getClass().getSimpleName();

    private Properties properties;

    public ShhTunnel(Properties properties) {
        this.properties = properties;
    }
    /**
     * Used to find if setupShhTunnel should be called. Tests
     * if there is a current SSH Tunnel for the required
     * URL on the port specified
     * @param url
     * @param port
     * @return If there exists a SSH Tunnel for the
     * given url on the given port
     */
    private boolean sshTunnelUp(String url, int port) {
        Socket socket = null;
        boolean reachable = false;

        InetAddress addr = null;
        try {
            addr = InetAddress.getByName(url);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        try {
            socket = new Socket(addr, port);
            reachable = true;
        } catch (IOException e) {
            // do nothing
        } finally {
            if (socket != null)try { socket.close(); } catch(IOException e) {}
        }

        return reachable;
    }

    public void setupShhTunnel() {
        int listenPort = Integer.parseInt(properties.getProperty("listenPort"));
        Log.d(TAG, "sshTunnelUp(localhost, " + listenPort + "): "+String.valueOf(sshTunnelUp(Constants.LOCALHOST, listenPort)));
        if (!sshTunnelUp(Constants.LOCALHOST, listenPort)) {
            try {
                startShhTunnel(properties);
            } catch (JSchException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "sshTunnelUp(localhost, " + listenPort + "): "+String.valueOf(sshTunnelUp(Constants.LOCALHOST, listenPort)));
    }


    /**
     * Create the SSH Tunnel required to access
     * my personal database. I may be able to use
     * `System Settings -> HTTP Proxy` in Android Studio
     * as a replacement to this
     * @throws JSchException
     */
    private void startShhTunnel(Properties properties) throws JSchException {
        String user = properties.getProperty("user");
        String host = properties.getProperty("host");
        String password = properties.getProperty("password");
        int listenPort = Integer.parseInt(properties.getProperty("listenPort"));
        String destHost = properties.getProperty("destHost");
        int destPort = Integer.parseInt(properties.getProperty("destPort"));

        JSch jsch = new JSch();
        Session session = jsch.getSession(user, host);
        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();
        session.setPortForwardingL(listenPort, destHost, destPort);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        setupShhTunnel();

        return null;
    }

    @Override
    protected void onPostExecute(Void feed) {
        /*if (type == Constants.SERVER_SIDE) {
            Log.d(TAG, "onPostExecute Constants.SERVER_SIDE");
            sendActivity.onSsdbShhResultResult(encryption);
        }
        if (type == Constants.CLIENT_SIDE) {
            Log.d(TAG, "onPostExecute Constants.CLIENT_SIDE");
            sendActivity.onCsdbShhResultResult();
        }*/
    }
}
