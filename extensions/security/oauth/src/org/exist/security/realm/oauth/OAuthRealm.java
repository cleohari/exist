/*
 *  eXist Open Source Native XML Database
 *  Copyright (C) 2011 The eXist Project
 *  http://exist-db.org
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 *  $Id$
 */
package org.exist.security.realm.oauth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.exist.EXistException;
import org.exist.config.Configuration;
import org.exist.config.ConfigurationException;
import org.exist.config.Configurator;
import org.exist.config.annotation.*;
import org.exist.security.AbstractRealm;
import org.exist.security.Account;
import org.exist.security.AuthenticationException;
import org.exist.security.Group;
import org.exist.security.PermissionDeniedException;
import org.exist.security.SchemaType;
import org.exist.security.Subject;
import org.exist.security.internal.SecurityManagerImpl;
import org.exist.security.internal.aider.GroupAider;
import org.exist.security.internal.aider.UserAider;
import org.exist.storage.DBBroker;
import org.scribe.exceptions.OAuthException;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
@ConfigurationClass("realm") //TODO: id = OAuth
public class OAuthRealm extends AbstractRealm {

    protected final static Logger LOG = Logger.getLogger(OAuthRealm.class);
    private final static String OAUTH = "OAuth";
    
    protected static OAuthRealm _ = null;
    
    @ConfigurationFieldAsAttribute("id")
    public final static String ID = "OAuth";

    @ConfigurationFieldAsAttribute("version")
    public final static String version = "1.0";
    
    //@ConfigurationReferenceBy("name")
    @ConfigurationFieldAsElement("service")
    @ConfigurationFieldClassMask("org.exist.security.realm.oauth.Service")
    List<Service> services; 

    private Group primaryGroup = null;

    public OAuthRealm(final SecurityManagerImpl sm, Configuration config) throws ConfigurationException {
        super(sm, config);
        _ = this;
        
		configuration = Configurator.configure(this, config);
    }

	@Override
	public String getId() {
		return ID;
	}
	
	private synchronized Group getPrimaryGroup() throws PermissionDeniedException {
		if (primaryGroup == null) {
			primaryGroup = getGroup(OAUTH);
			if (primaryGroup == null)
				try {
					primaryGroup = executeAsSystemUser(new Unit<Group>() {
						@Override
						public Group execute(DBBroker broker) throws EXistException, PermissionDeniedException {
							return addGroup(new GroupAider(ID, OAUTH));
						}
					});
		            
					if (primaryGroup == null)
						throw new ConfigurationException("OAuth realm can not create primary group 'OAuth'.");
					
				} catch (PermissionDeniedException e) {
					throw e;
				} catch (ConfigurationException e) {
					throw new PermissionDeniedException(e);
				} catch (EXistException e) {
					throw new PermissionDeniedException(e);
				}
		}
		return primaryGroup;
	}

	@Override
	public List<String> findUsernamesWhereNameStarts(Subject invokingUser, String startsWith) {
		// Auto-generated method stub
		return null;
	}

	@Override
	public List<String> findUsernamesWhereUsernameStarts(Subject invokingUser, String startsWith) {
		// Auto-generated method stub
		return null;
	}

	@Override
	public List<String> findAllGroupNames(Subject invokingUser) {
		// Auto-generated method stub
		return null;
	}

	@Override
	public List<String> findAllGroupMembers(Subject invokingUser, String groupName) {
		// Auto-generated method stub
		return null;
	}

    @Override
    public Collection<? extends String> findGroupnamesWhereGroupnameContains(Subject invokingUser, String fragment) {
        // Auto-generated method stub
        return null;
    }

    @Override
    public List<String> findUsernamesWhereNamePartStarts(Subject invokingUser, String startsWith) {
        // Auto-generated method stub
        return null;
    }
        
	@Override
	public Collection<? extends String> findGroupnamesWhereGroupnameStarts(Subject invokingUser, String startsWith) {
		// Auto-generated method stub
		return null;
	}

	@Override
	public Subject authenticate(String accountName, Object credentials) throws AuthenticationException {
		// Auto-generated method stub
		return null;
	}

	@Override
	public boolean deleteAccount(Account account) throws PermissionDeniedException, EXistException, ConfigurationException {
		// Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteGroup(Group group) throws PermissionDeniedException, EXistException, ConfigurationException {
		// Auto-generated method stub
		return false;
	}

    protected Account createAccountInDatabase(final String username, final Map<SchemaType, String> metadata) throws AuthenticationException {

        try {
            return executeAsSystemUser(new Unit<Account>(){
                @Override
                public Account execute(DBBroker broker) throws EXistException, PermissionDeniedException {
                    //create the user account
                    UserAider userAider = new UserAider(ID, username, getPrimaryGroup());

                    //store any requested metadata
                    for(Entry<SchemaType, String> entry : metadata.entrySet())
                        userAider.setMetadataValue(entry.getKey(), entry.getValue());

                    Account account = getSecurityManager().addAccount(userAider);

                    return account;
                }
            });
        } catch(Exception e) {
            throw new AuthenticationException(AuthenticationException.UNNOWN_EXCEPTION, e.getMessage(), e);
        }
    }
    
	public Service getServiceBulderByPath(String name) {
		for (Service service : services) {
			if (service.getName().equals(name)) {
				return service;
			}
		}
		
		throw new OAuthException("Service no found by name '"+name+"'.");
	}

}
