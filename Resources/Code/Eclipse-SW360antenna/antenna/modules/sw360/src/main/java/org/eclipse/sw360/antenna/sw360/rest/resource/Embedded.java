/*
 * Copyright (c) Bosch Software Innovations GmbH 2017-2018.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.sw360.antenna.sw360.rest.resource;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.eclipse.sw360.antenna.sw360.rest.deserializer.SW360ResourceDeserializer;

@JsonDeserialize(using = SW360ResourceDeserializer.class)
public interface Embedded {
}
