package ch.ethz.inf.vs.lubu.cyrptdbmodule.crypto;

/**
 * Created by lukas on 30.03.15.
 */
public abstract class HOMLayer extends EncLayer {

    protected HOMLayer() {
        this.type = EncLayerType.HOM;
    }

    public abstract String getAgrFunc(String arg);

}
