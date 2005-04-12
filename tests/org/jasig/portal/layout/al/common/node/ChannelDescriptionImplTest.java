/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.al.common.node;

import org.jasig.portal.channels.BaseChannel;
import org.jasig.portal.channels.error.CError;
import org.jasig.portal.channels.portlet.CPortletAdapter;
import org.jasig.portal.channels.portlet.IPortletAdaptor;

import junit.framework.TestCase;

/**
 * JUnit testcase for ChannelDescriptionImpl.
 */
public class ChannelDescriptionImplTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testIsPortlet() {
		// test that when channel class is not set
		// channel description does not think it's a portlet
		ChannelDescriptionImpl impl = new ChannelDescriptionImpl();
		assertFalse(impl.isPortlet());
		
		// test that when channel class is set to a non-portlet IChannel
		// channel description does not think its a portlet
		impl.setClassName(CError.class.getName());
		assertFalse(impl.isPortlet());
		
		// test that when channel class is set to a non-existing class
		// implementation does not think it's a portlet
		impl.setClassName("not.a.real.package.DoesNotExist");
		assertFalse(impl.isPortlet());
		
		// test that when channel class is set to CPortletAdapter
		// channel description thinks its a portlet
		impl.setClassName(CPortletAdapter.class.getName());
		assertTrue(impl.isPortlet());
		
		// test that when channel class is set to another IPortletAdaptor
		// channel description thinks its a portlet
		impl.setClassName(AnotherPortletAdaptor.class.getName());
		assertTrue(impl.isPortlet());
	}

	/**
	 * Test class used to test recognition of non-CPortletAdaptor 
	 * IPortletAdaptors.
	 */
	private class AnotherPortletAdaptor 
		extends BaseChannel 
		implements IPortletAdaptor {
		
	}
	
}