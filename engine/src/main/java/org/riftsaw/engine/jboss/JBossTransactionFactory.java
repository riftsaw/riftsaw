/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and others contributors as indicated
 * by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.riftsaw.engine.jboss;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.TransactionManager;

/**
 * This is a factory class used by ODE to obtain a transaction manager.
 * 
 * @author gbrown
 *
 */
public class JBossTransactionFactory {

    public static final String JNDI_LOOKUP_PATH = "java:/TransactionManager";
    
    public JBossTransactionFactory() {
    }

    public TransactionManager getTransactionManager() {
        InitialContext ctx;
        try {
           ctx = new InitialContext();
        } catch (NamingException except) {
            throw new RuntimeException( "Can't create InitialContext", except);
        }
        try {
            return (TransactionManager) ctx.lookup(JNDI_LOOKUP_PATH);
        } catch (NamingException except) {
            throw new RuntimeException( "Error while looking up TransactionManager at " + JNDI_LOOKUP_PATH, except);
        }
    }

}
