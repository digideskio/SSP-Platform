/**
 * Copyright (c) 2001, 2002 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.jasig.portal.groups;

import java.io.*;
import java.util.Properties;
import org.jasig.portal.PropertiesManager;
import org.jasig.portal.services.LogService;
import org.jasig.portal.utils.ResourceLoader;
/**
 * @author: Dan Ellentuck
 * @version $Revision$
 */
public class ReferenceGroupService implements IGroupService
{
    // Singleton instance:
    private static IGroupService singleton = null;

    // Factories for IGroupMembers:
    private IEntityStore entityFactory = null;
    private IEntityGroupStore groupFactory = null;

    // Key for everyone group:
    private String everyoneKey = null;

/**
 * ReferenceGroupsService constructor.
 */
private ReferenceGroupService() throws GroupsException
{
        super();
        initialize();
}
 /*
  * Returns a pre-existing <code>IEntityGroup</code> or null if it
  * does not exist.
  */
public IEntityGroup findGroup(String key) throws GroupsException
{
    return groupFactory.find(key);
}
  /*
   * Returns an <code>IEntity</code> representing a portal entity.  This does
   * not guarantee that the entity actually exists.
   */
  public IEntity getEntity(String key, Class type) throws GroupsException
  {
    return entityFactory.newInstance(key, type);
  }
  /*
   * Returns the group whose key is the everyone key in the security.properties file.
   */
  public IEntityGroup getEveryoneGroup() throws GroupsException
  {
    return findGroup(everyoneKey);
  }
  /**
   * @exception org.jasig.portal.groups.GroupsException.
   */
  private void initialize() throws GroupsException
  {
    String eMsg = null;
    entityFactory = new RDBMEntityStore();

    String groupFactoryName = PropertiesManager.getProperty
        ("org.jasig.portal.groups.EntityGroupFactory.implementation");

    if ( groupFactoryName == null )
    {
        eMsg = "ReferenceGroupService.initialize(): EntityGroupStoreImpl not specified in portal.properties";
        LogService.log(LogService.ERROR, eMsg);
        throw new GroupsException(eMsg);
    }

    try
    {
        groupFactory = (IEntityGroupStore)Class.forName(groupFactoryName).newInstance();
    }
    catch (Exception e)
    {
        eMsg = "ReferenceGroupService.initialize(): Failed to instantiate " + groupFactoryName + " " + e;
        LogService.log(LogService.ERROR, eMsg);
        throw new GroupsException(eMsg);
    }

    try
    {
        File secFile = ResourceLoader.getResourceAsFile(this.getClass(),"/properties/security.properties");
        Properties secProps = new Properties();
        secProps.load(new FileInputStream(secFile));
        everyoneKey = secProps.getProperty("everyoneKey");
    }
    catch (Exception e)
    {
        eMsg = "ReferenceGroupService.initialize(): Unable to load everyoneKey from security.properties... " + e;
        LogService.instance().log(LogService.ERROR, eMsg);
        throw new GroupsException(eMsg);
    }
}
  /*
   * Returns a new <code>IEntityGroup</code> for the given Class with an unused
   * key.
   */
  public IEntityGroup newGroup(Class type) throws GroupsException
  {
    return groupFactory.newInstance(type);
  }
  /**
   * @return org.jasig.portal.groups.IGroupService
   * @exception org.jasig.portal.groups.GroupsException
   */
  public static synchronized IGroupService singleton() throws GroupsException
  {
    if ( singleton == null )
        { singleton = new ReferenceGroupService(); }
    return singleton;
  }
}
