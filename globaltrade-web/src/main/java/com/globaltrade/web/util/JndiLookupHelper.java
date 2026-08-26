package com.globaltrade.web.util;

import com.globaltrade.customs.beans.CustomsClearanceSessionBean;

import javax.naming.InitialContext;
import javax.naming.NamingException;

public final class JndiLookupHelper {

    private JndiLookupHelper() {
    }

    public static CustomsClearanceSessionBean lookupClearanceSessionBean() throws NamingException {
        InitialContext ctx = new InitialContext();
        return (CustomsClearanceSessionBean) ctx.lookup("java:app/globaltrade-customs-ejb/CustomsClearanceSessionBean");
    }

}
