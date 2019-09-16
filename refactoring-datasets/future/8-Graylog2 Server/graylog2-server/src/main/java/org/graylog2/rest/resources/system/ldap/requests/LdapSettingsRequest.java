/*
 * Copyright 2013 TORCH UG
 *
 * This file is part of Graylog2.
 *
 * Graylog2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Graylog2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Graylog2.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.graylog2.rest.resources.system.ldap.requests;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.net.URI;

@JsonAutoDetect
public class LdapSettingsRequest {

    public boolean enabled;

    public String systemUsername;

    public String systemPassword;

    public URI ldapUri;

    public boolean useStartTls;

    public boolean trustAllCertificates;

    public boolean activeDirectory;

    public String searchBase;

    public String searchPattern;

    public String displayNameAttribute;

    public String defaultGroup;
}
