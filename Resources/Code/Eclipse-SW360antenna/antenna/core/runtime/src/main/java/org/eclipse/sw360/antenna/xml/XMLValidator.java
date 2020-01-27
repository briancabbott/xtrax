/*
 * Copyright (c) Bosch Software Innovations GmbH 2016-2017.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.sw360.antenna.xml;

import org.apache.commons.lang.Validate;
import org.eclipse.sw360.antenna.api.IXMLValidator;
import org.eclipse.sw360.antenna.api.exceptions.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Checks if given xml is suitable to the given xsd.
 */
public class XMLValidator extends IXMLValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(XMLValidator.class);

    /**
     * @param xmlFile
     *            File to the xml File which will be validated.
     * @param xsdUrl
     *            URL to the xsd File against which the xml File will be
     *            validated.
     * @throws ConfigurationException
     *             if the config.xml is not valid or if an IOException occurs
     *             during processing.
     */
    @Override
    public void validateXML(File xmlFile, URL xsdUrl) {
        Validate.notNull(xmlFile, "No URL to xml file provided!");
        if (xsdUrl != null) {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            try {
                Schema schema = factory.newSchema(xsdUrl);
                Validator validator = schema.newValidator();

                StreamSource source = new StreamSource(xmlFile);
                validator.validate(source);
            } catch (SAXException e) {
                LOGGER.error("Invalid XML", e);
                throw new ConfigurationException("The config File " + xmlFile + " could not be validated against" + xsdUrl, e);
            } catch (IOException e) {
                LOGGER.error("File access failing", e);
                throw new ConfigurationException("The config File could not be validated", e);
            }
        }
    }
}
