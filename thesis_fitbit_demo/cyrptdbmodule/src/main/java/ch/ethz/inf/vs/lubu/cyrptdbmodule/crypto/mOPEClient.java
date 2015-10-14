package ch.ethz.inf.vs.lubu.cyrptdbmodule.crypto;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.dbscheme.CDBColumn;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.dbscheme.CDBField;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.exceptions.CDBException;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.exceptions.ConnectionCDBException;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.util.DBType;

/**
 * Created by lukas on 20.05.15.
 */
public class mOPEClient extends EncLayer {

    private static final String OPE_TAG_PREFIX = "OPEENC";

    private final CDBKey key;

    private DETBf bf;

    public mOPEClient(CDBKey key) {
        this.key = key;
        this.type = EncLayerType.OPE;
        bf = new DETBf(key);
    }


    @Override
    public CDBField encrypt(CDBField field) throws CDBException {
        return bf.encrypt(field);
    }

    @Override
    public CDBField decrypt(CDBField field) throws CDBException {
        return null;
    }

    @Override
    public CDBField.DBStrType getStrType(DBType type) {
        return null;
    }

    public static mOPEInteractionResult interactWithOPEServer(IKeyManager keyMan, mOPEJob job, String opeServIP, int mOPEPort) throws Exception {
            PrintWriter toServer = null;
            BufferedReader fromServer = null;
            Socket sock = null;
            try {
                sock = new Socket(opeServIP,mOPEPort);
                toServer = new PrintWriter( new BufferedWriter( new OutputStreamWriter(sock.getOutputStream())));;
                fromServer = new BufferedReader( new InputStreamReader(sock.getInputStream()));
                DETBf curEnc = null;
                mOPEJob.mOPEWork curWork = null;
                BigInteger curPlain = null;

                while(true) {
                    String msg = fromServer.readLine();
                    if(msg == null)
                        throw new RuntimeException("Empty Message");

                    String[] tokens = msg.split(MessageUtils.MESSAGE_TOKEN_DELIM);

                    if(tokens.length == 0)
                        throw new RuntimeException("Empty Message");

                    MessageUtils.MessageType type = MessageUtils.MessageType.fromString(tokens[0]);

                    Log.d("mOPE","Rec Message: "+msg);
                    switch(type) {
                        case DECIDE_START:
                            curWork = handleDecideStart(job, tokens);
                            curEnc = getEnc(keyMan, curWork);
                            curPlain = decryptBF(curEnc, curWork.getColumn(), curWork.getCipher());
                            break;
                        case DECIDE_FOUND:
                            curEnc = null;
                            break;
                        case DECIDE_ORDER:
                            toServer.append(decideOrder(curEnc,tokens,curWork.getColumn(),curPlain));
                            toServer.flush();
                            break;
                        case ERROR_CODE:
                            if(tokens.length>1)
                                throw new ConnectionCDBException("Error on OpeSever: " + tokens[1]);
                            else
                                throw new ConnectionCDBException("Error on OpeSever ");
                        case INSERT_SUCC:
                            return new mOPEInteractionResult(true);
                        case QUERY_RESULT:
                            if(tokens.length==2)
                                return new mOPEInteractionResult(true, tokens[1]);
                            else
                                throw new ConnectionCDBException("Wrong Query Result Message");
                        default:
                            throw new ConnectionCDBException("Wrong Message Received");
                    }

                }


            } finally {
                if(sock!=null && ! sock.isClosed())
                    sock.close();
                if(toServer!=null)
                    toServer.close();
                if(fromServer!=null)
                    fromServer.close();
            }
    }

    private static DETBf getEnc(IKeyManager keyMan, mOPEJob.mOPEWork  work) throws CDBException {
        return new DETBf(keyMan.getKey(work.getColumn().getMainType(), work.getColumn()));
    }

    private static mOPEJob.mOPEWork handleDecideStart(mOPEJob job, String[] tokens) throws CDBException {
        if(tokens.length!=2)
            throw new ConnectionCDBException("Wrong message");
        int id = Integer.valueOf(tokens[1]);
        mOPEJob.mOPEWork  work = null;
        for(mOPEJob.mOPEWork w : job.getWork()) {
            if(w.getID()==id) {
                work = w;
                break;
            }
        }
        if(work == null)
            throw new ConnectionCDBException("Work not found");

        return work;
    }

    private static String decideOrder(DETBf enc, String[] tokens, CDBColumn col, BigInteger plain) throws Exception {
        StringBuffer buff = new StringBuffer();
        if(tokens.length<2 || enc==null || plain == null)
            throw new RuntimeException("Wrong message");

        buff.append(MessageUtils.MessageType.DECIDE_REPLY.toString())
                .append(MessageUtils.MESSAGE_TOKEN_DELIM);

        int size = Integer.valueOf(tokens[1]);

        if(tokens.length!=2+size)
            throw new RuntimeException("Wrong message");

        boolean equals = false;

        int i;
        for(i=0; i<size; i++) {
            BigInteger plainCur = decryptBF(enc, col, tokens[2+i]);
            equals = plain.compareTo(plainCur)==0;
            if(plain.compareTo(plainCur)<=0)
                break;
        }

        buff.append(i)
                .append(MessageUtils.MESSAGE_TOKEN_DELIM);

        if(equals)
            buff.append("1");
        else
            buff.append("0");

        buff.append(MessageUtils.MESSAGE_DELIM);
        return buff.toString();
    }

    private static BigInteger decryptBF(DETBf enc, CDBColumn col, String cipher) throws Exception {
        CDBField field = new CDBField(col);
        field.setOutputType(CDBField.DBStrType.NUM);
        field.setValueFromDB(cipher);
        CDBField resByte = enc.decrypt(field);
        return new BigInteger(resByte.getStrRep());
    }

}
