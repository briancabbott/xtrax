/*
 * Copyright (c) Bosch Software Innovations GmbH 2013,2016-2017.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.xml;

import org.eclipse.sw360.antenna.api.exceptions.ConfigurationException;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

public class XMLValidatorTest {
    @Test
    public void validateXMLTestValidURL() {
        XMLValidator validator = new XMLValidator();
        URL xsd = validator.getClass().getResource("/configTest.xsd");
        URL xml = validator.getClass().getResource("/antennaconf.xml");
        validator.validateXML(xml, xsd);
    }

    @Test(expected = ConfigurationException.class)
    public void validateXMLTestInvalidURL() throws MalformedURLException {
        XMLValidator validator = new XMLValidator();
        URL xsd = validator.getClass().getResource("/configTest.xsd");
        URL xml = new URL("http://");
        validator.validateXML(xml, xsd);
    }
}
