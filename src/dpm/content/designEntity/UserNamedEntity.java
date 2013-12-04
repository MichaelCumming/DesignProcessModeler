/*
 * UserNamedEntity.java
 *
 * Created on Sept 10, 2004, 10:42 AM
 */

package dpm.content.designEntity;

import dpm.content.DesignEntity;
import dpm.content.advertisement.DesignEntityAdv;
import dpm.content.advertisement.net.LoopNetAdvertisement;
import dpm.dpmApp.desktop.DpmTerms;
import dpm.peer.Peer;
import dpm.dpmApp.desktop.*;
import dpm.content.*;


/**
 *
 * @author  cumming
 */
public class UserNamedEntity extends DesignEntity {
    
    /** Creates a new instance of UserNamedEntity */
    public UserNamedEntity(DesignEntityAdv designEntityAdv, LoopNetAdvertisement loopNetAdv, Peer appUser) {
        super(designEntityAdv, loopNetAdv, appUser);
        this.entityType = designEntityAdv.getEntityType();
    }
    
    
}
