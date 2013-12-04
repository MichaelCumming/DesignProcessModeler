package dpm.content.advertisement.designEntity.related.constraint;

import dpm.content.DeleteChecker;
import dpm.content.advertisement.designEntity.related.ConstraintAdv;
import dpm.peer.Peer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownServiceException;
import java.text.ParseException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import net.jxta.document.*;
import net.jxta.document.AdvertisementFactory.Instantiator;
import net.jxta.id.ID;
import net.jxta.id.IDFactory;
import net.jxta.peer.PeerID;
import dpm.dpmApp.desktop.*;



/** Once a task attains a certain state, this is advertised using a PolicyAdvertisement
 * @author cumming */
public class PolicyAdvertisement extends ConstraintAdv implements DeleteChecker, DpmTerms {
    
    public static final String rolesTag = "Roles"; //header tag
    public static final String roleTag = "Role"; //header tag
    public static final String constraintNameTag = "RoleName"; //aka: constraintName in superClass
    
    /** All the roles for that transName. Revised 1 March 04 */
    private Set roles = new HashSet();
    
    private static final String[] fields =
    {advIDTag, constraintNameTag, targetTransTag, targetIDTag, targetBaseNameTag, targetIterationTag, targetTypeTag, descTag, dateCreateTag, authorIDTag, authorNameTag, rolesTag, roleTag};
    
    public boolean isDeleted(Peer appUser) {
        String storageKey = getID().toString();
        return appUser.getDeleteAdvs().storageKeyExists(storageKey);
    }
    
    /** {@inheritDoc} */
    public static class Instantiator implements AdvertisementFactory.Instantiator {
        /** {@inheritDoc} */
        public String getAdvertisementType() {
            return "jxta:PolicyAdv";
        }
        /** {@inheritDoc} */
        public Advertisement newInstance() {
            return new PolicyAdvertisement();
        }
        /** {@inheritDoc} */
        public Advertisement newInstance(Element root) {
            return new PolicyAdvertisement(root);
        }
    };
    
    /** Private Constructor, use the instantiator */
    private PolicyAdvertisement() {
    }
    
    private PolicyAdvertisement(Element root) {
        this();
        initialize(root);
    }
    
    public static String getAdvertisementType() {
        return "jxta:PolicyAdv";
    }
    
    /** Builds a Java object from a structured text document (the adv) */
    private void initialize(Element root) {
        if (!TextElement.class.isInstance(root))
            throw new IllegalArgumentException(getClass().getName() + " only supports TextElement");
        TextElement doc = (TextElement)root;
        if (!doc.getName().equals(getAdvertisementType()))
            throw new IllegalArgumentException("Could not construct : " + getClass().getName() + " from doc containing a '" +
            doc.getName() + "'. Should be : " + getAdvertisementType());
        Enumeration elements = doc.getChildren();
        //
        while (elements.hasMoreElements()) {
            TextElement elem = (TextElement)elements.nextElement();
            //
            if (elem.getName().equals(advIDTag)) {
                try {
                    URL advID = IDFactory.jxtaURL(elem.getTextValue());
                    setAdvID(IDFactory.fromURL(advID));
                } catch (UnknownServiceException badID) {
                    throw new IllegalArgumentException("Unusable advID in policy advertisement");
                } catch (MalformedURLException badID) {
                    throw new IllegalArgumentException("Bad advID in policy advertisement");
                }
                continue;
            }
            if (elem.getName().equals(targetTransTag)) {
                setTransName(elem.getTextValue());
                continue;
            }
            if (elem.getName().equals(targetIDTag)) {
                try {
                    URL designEntityID = IDFactory.jxtaURL(elem.getTextValue());
                    setDesignEntityID(IDFactory.fromURL(designEntityID));
                } catch (UnknownServiceException badID) {
                    throw new IllegalArgumentException("Unusable task ID in task state advertisement");
                } catch (MalformedURLException badID) {
                    throw new IllegalArgumentException("Bad task ID in task state advertisement");
                }
                continue;
            }
            if (elem.getName().equals(targetBaseNameTag)) {
                setBaseName(elem.getTextValue());
                continue;
            }
            if (elem.getName().equals(targetIterationTag)) {
                setIteration(elem.getTextValue());
                continue;
            }
            if (elem.getName().equals(targetTypeTag)) {
                setEntityType(elem.getTextValue());
                continue;
            }
            if (elem.getName().equals(dateCreateTag)) {
                try {
                    setDateCreate(dateFormat.parse(elem.getTextValue()));
                }
                catch (ParseException e) {
                    e.printStackTrace();
                }
                continue;
            }
            if (elem.getName().equals(authorIDTag)) {
                try {
                    URL authorID = IDFactory.jxtaURL(elem.getTextValue());
                    setAuthorID((PeerID)IDFactory.fromURL(authorID));
                } catch (UnknownServiceException badID) {
                    throw new IllegalArgumentException("Unusable author ID in policy advertisement");
                } catch (MalformedURLException badID) {
                    throw new IllegalArgumentException("Bad author ID in policy advertisement");
                }
                continue;
            }
            if (elem.getName().equals(authorNameTag)) {
                setAuthorName(elem.getTextValue());
                continue;
            }
            if (elem.getName().equals(rolesTag)) {
                for(Enumeration rolesEnum = elem.getChildren(); rolesEnum.hasMoreElements(); ) {
                    elem = (TextElement)rolesEnum.nextElement();
                    if (elem.getName().equals(constraintNameTag)) {
                        roles.add(elem.getTextValue());
                    }
                }
                continue;
            }
            //            if (elem.getName().equals(rolesTag)) {
            //                for(Enumeration rolesEnum = elem.getChildren(); rolesEnum.hasMoreElements(); ) {
            //                    TextElement roleData = (TextElement)rolesEnum.nextElement();
            //                    for(Enumeration roleEnum = roleData.getChildren(); roleEnum.hasMoreElements(); ) {
            //                        TextElement roleElem = (TextElement)roleEnum.nextElement();
            //
            //                        String roleName = null;
            //                        String roleFillerName = null;
            //                        String roleFillerType = null;
            //
            //                        if (roleElem.getName().equals(constraintNameTag)) {
            //                            roleName = roleElem.getTextValue();
            //                        }
            //                        if (roleElem.getName().equals(roleFillerNameTag)) {
            //                            roleFillerName = roleElem.getTextValue();
            //                        }
            //                        if (roleElem.getName().equals(roleFillerTypeTag)) {
            //                            roleFillerType = roleElem.getTextValue();
            //                        }
            //                        if (elem.getName().equals(roleFillerIDTag)) {
            //                            try {
            //                                URL roleFillerID = IDFactory.jxtaURL(elem.getTextValue());
            //                                Role newRole = new Role(
            //                                roleName, roleFillerType, roleFillerName, IDFactory.fromURL(roleFillerID));
            //                                if (newRole != null) {
            //                                    roles.add(newRole);
            //                                }
            //                            } catch (UnknownServiceException badID) {
            //                                throw new IllegalArgumentException("Unusable ID in pol advertisement");
            //                            } catch (MalformedURLException badID) {
            //                                throw new IllegalArgumentException("Bad ID in pol advertisement");
            //                            }
            //                        }
            //                    }
            //                }
            //                continue;
            //            }
        }
    }
    
    /** Builds a structured text document from a Java object */
    public Document getDocument(MimeMediaType mediaType) {
        StructuredTextDocument adv = (StructuredTextDocument)
        StructuredDocumentFactory.newStructuredDocument(mediaType, getAdvertisementType());
        if (adv instanceof Attributable) {
            ((Attributable)adv).addAttribute("xmlns:jxta", "http://jxta.org");
        }
        if (getAdvID().equals(ID.nullID))
            throw new IllegalStateException(" role has no assigned advID");
        TextElement e = adv.createElement(advIDTag, getAdvID().toString());
        adv.appendChild(e);
        e = adv.createElement(targetTransTag, getTransName());
        adv.appendChild(e);
        
        /**Target information */
        if (getDesignEntityID().equals(ID.nullID))
            throw new IllegalStateException("link adv has no assigned designEntityID");
        e = adv.createElement(targetIDTag, getDesignEntityID().toString());
        adv.appendChild(e);
        e = adv.createElement(targetBaseNameTag, getBaseName());
        adv.appendChild(e);
        e = adv.createElement(targetIterationTag, getIteration());
        adv.appendChild(e);
        e = adv.createElement(targetTypeTag, getEntityType());
        adv.appendChild(e);
        
        /**Other information */
        e = adv.createElement(dateCreateTag, dateFormat.format(getDateCreate()));
        adv.appendChild(e);
        if (getAuthorID().equals(ID.nullID))
            throw new IllegalStateException("DesignEntity policy has no assigned author ID");
        e = adv.createElement(authorIDTag, getAuthorID().toString());
        adv.appendChild(e);
        e = adv.createElement(authorNameTag, getAuthorName());
        adv.appendChild(e);
        
        /** Add roles */
        TextElement allRoles = adv.createElement(rolesTag);
        adv.appendChild(allRoles);
        
        /**roles set contains roleNames */
        for (Iterator i = roles.iterator(); i.hasNext(); ) {
            String roleName = (String)i.next();
            TextElement roleElem = adv.createElement(constraintNameTag, roleName);
            allRoles.appendChild(roleElem);
        }
        //        for (Iterator i = roles.iterator(); i.hasNext(); ) {
        //            Role role = (Role)i.next();
        //            TextElement roleElem = adv.createElement(roleTag);
        //            allRoles.appendChild(roleElem);
        //
        //            /**Get the data from each role */
        //            TextElement roleName = adv.createElement(constraintNameTag, role.getName());
        //            roleElem.appendChild(roleName);
        //            TextElement fillerType = adv.createElement(roleFillerTypeTag, role.getFillerType());
        //            roleElem.appendChild(fillerType);
        //            TextElement fillerName = adv.createElement(roleFillerNameTag, role.getFillerName());
        //            roleElem.appendChild(fillerType);
        //            if (role.getFillerID().equals(ID.nullID))
        //                throw new IllegalStateException("polAdv role filler ID has no assigned ID");
        //            TextElement fillerID = adv.createElement(roleFillerIDTag, role.getFillerID().toString());
        //            roleElem.appendChild(fillerID);
        //        }
        
        return adv;
    }
    
    
    /** Getter for property roles.
     * @return Value of property roles.
     *
     */
    public Set getRoles() {
        //        Set cleanedRoles = roles;
        //        for(Iterator i = cleanedRoles.iterator(); i.hasNext(); ) {
        //            String role = (String)i.next();
        //            if(role.equals(EMPTY_STRING)) {
        //                roles.remove(role);
        //            }
        //        }
        return roles;
    }
    
    /** Setter for property roles.
     * @param roles New value of property roles.
     *
     */
    public void setRoles(Set roles) {
        this.roles = roles;
    }
    
}
