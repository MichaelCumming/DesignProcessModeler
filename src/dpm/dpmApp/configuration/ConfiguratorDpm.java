/*
 * ConfiguratorDpm.java
 *
 * Created on September 11, 2003, 3:22 PM
 */

package dpm.dpmApp.configuration;

import java.io.IOException;
import java.net.UnknownHostException;
import java.net.ServerSocket;
import net.jxta.util.config.Configurator;
import java.net.InetAddress;
import dpm.content.*;

/** Used to avoid the standard JXTA configurator which is quite complicated for
 * non-developer users. Currently not used.
 * @author cumming
 */
public class ConfiguratorDpm extends Configurator {
    protected String peerType;
    //private int availablePort;
    //private String IPaddress;
    //private static String DEFAULT_PERSON_DIR = "p:/cumming/research/java/dpm.cumming.latest/src";
    
    
    /** Creates a new instance of Configurator
     * @param peerName
     * @param peerDescription
     * @param principal
     * @param credential
     * @param peerType
     */
    public ConfiguratorDpm(String peerName, String peerDescription, String principal, String credential, String peerType) { //String peerType) {
        super(peerName, peerDescription, principal, credential);
        this.peerType = peerType;
    }
    
    public void setJxtaHome() {
    }   
}
