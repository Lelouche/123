package ch.ethz.inf.vs.lubu.cyrptdbmodule.exceptions;

/**
 * Created by lukas on 24.03.15.
 */
public class NotSupportedCDBException extends RewriteCDBException {
    public NotSupportedCDBException(String reason) {
        super(reason);
    }
}
