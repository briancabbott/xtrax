/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License (the "License").
 * You may not use this file except in compliance with the License.
 *
 * See LICENSE.txt included in this distribution for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at LICENSE.txt.
 * If applicable, add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your own identifying
 * information: Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 */

/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 */
package org.opengrok.web.api.v1.controller;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opengrok.indexer.configuration.Configuration;
import org.opengrok.indexer.configuration.RuntimeEnvironment;
import org.opengrok.web.api.v1.suggester.provider.service.SuggesterService;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

public class ConfigurationControllerTest extends JerseyTest {

    private RuntimeEnvironment env = RuntimeEnvironment.getInstance();

    @Mock
    private SuggesterService suggesterService;

    @Override
    protected Application configure() {
        MockitoAnnotations.initMocks(this);
        return new ResourceConfig(ConfigurationController.class)
                .register(new AbstractBinder() {
                    @Override
                    protected void configure() {
                        bind(suggesterService).to(SuggesterService.class);
                    }
                });
    }

    @Test
    public void testApplySetAndGetBasicConfig() {
        Configuration config = new Configuration();
        String srcRoot = "/foo";
        config.setSourceRoot(srcRoot);

        String configStr = config.getXMLRepresentationAsString();

        target("configuration")
                .request()
                .put(Entity.xml(configStr));

        assertEquals(env.getSourceRootPath(), srcRoot);

        String returnedConfig = target("configuration")
                .request()
                .get(String.class);

        assertEquals(configStr, returnedConfig);
    }

    @Test
    public void testApplySetInvalidMethod() {
        Response r = setValue("noMethodExists", "1000");

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), r.getStatus());
    }

    private Response setValue(final String field, final String value) {
        return target("configuration")
                .path(field)
                .request()
                .put(Entity.text(value));
    }

    @Test
    public void testApplyGetInvalidMethod() {
        Response r = target("configuration")
                .path("FooBar")
                .request()
                .get();

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), r.getStatus());
    }

    @Test
    public void testApplySetInvalidMethodParameter() {
        Response r = setValue("setDefaultProjects", "1000"); // expecting Set

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), r.getStatus());
    }

    @Test
    public void testApplySetOptionInteger() {
        assertEquals(25, env.getHitsPerPage());

        setValue("hitsPerPage", "1000");

        assertEquals(1000, env.getHitsPerPage());

        env.setHitsPerPage(25);
    }

    @Test
    public void testApplySetOptionInvalidInteger() {
        Response r = setValue("hitsPerPage", "abcd");

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), r.getStatus());
    }

    @Test
    public void testApplySetOptionBooleanTrue() {
        testSetChattyStatusPageTrue("true");
        testSetChattyStatusPageTrue("on");
        testSetChattyStatusPageTrue("1");
    }

    private void testSetChattyStatusPageTrue(final String value) {
        env.setChattyStatusPage(false);

        setValue("chattyStatusPage", value);

        Assert.assertTrue(env.isChattyStatusPage());
    }

    @Test
    public void testApplySetOptionBooleanFalse() {
        testSetChattyStatusPageFalse("false");
        testSetChattyStatusPageFalse("off");
        testSetChattyStatusPageFalse("0");
    }

    private void testSetChattyStatusPageFalse(final String value) {
        env.setChattyStatusPage(true);

        setValue("chattyStatusPage", value);

        Assert.assertFalse(env.isChattyStatusPage());
    }

    @Test
    public void testApplySetOptionInvalidBoolean1() {
        Response r = setValue("chattyStatusPage", "1000"); // only 1 is accepted as true

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), r.getStatus());
    }

    @Test
    public void testApplySetOptionInvalidBoolean2() {
        Response r = setValue("chattyStatusPage", "anything");

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), r.getStatus());
    }

    @Test
    public void testApplySetOptionString() {
        String old = env.getUserPage();

        setValue("userPage", "http://users.portal.com?user=");

        assertEquals("http://users.portal.com?user=", env.getUserPage());

        setValue("userPage", "some complicated \"string\" with &#~Đ`[đ\\ characters");

        assertEquals("some complicated \"string\" with &#~Đ`[đ\\ characters", env.getUserPage());

        env.setUserPage(old);
    }

    @Test
    public void testApplyGetOptionString() {
        env.setSourceRoot("/foo/bar");
        String response = target("configuration")
                .path("sourceRoot")
                .request()
                .get(String.class);

        assertEquals(response, env.getConfiguration().getSourceRoot());
    }

    @Test
    public void testApplyGetOptionInteger() {
        int hitsPerPage = target("configuration")
                .path("hitsPerPage")
                .request()
                .get(int.class);

        assertEquals(env.getHitsPerPage(), hitsPerPage);
    }

    @Test
    public void testApplyGetOptionBoolean() {
        boolean response = target("configuration")
                .path("historyCache")
                .request()
                .get(boolean.class);

        assertEquals(env.getConfiguration().isHistoryCache(), response);
    }

    @Test
    public void testSuggesterServiceNotifiedOnConfigurationFieldChange() {
        reset(suggesterService);
        setValue("sourceRoot", "test");
        verify(suggesterService).refresh();
    }

    @Test
    public void testSuggesterServiceNotifiedOnConfigurationChange() {
        reset(suggesterService);
        target("configuration")
                .request()
                .put(Entity.xml(new Configuration().getXMLRepresentationAsString()));
        verify(suggesterService).refresh();
    }

}
