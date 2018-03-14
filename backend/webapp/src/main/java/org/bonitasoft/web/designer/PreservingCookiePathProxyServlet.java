/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.web.designer;

import org.mitre.dsmiley.httpproxy.ProxyServlet;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.HttpCookie;
import java.util.List;

/**
 * Extending {@link ProxyServlet} to fix the cookie path which was overriden even when preserveCookie is set to true
 */
public class PreservingCookiePathProxyServlet extends ProxyServlet {

    /** Copy cookie from the proxy to the servlet client.
     *  Replaces cookie path to local path and renames cookie to avoid collisions.
     */
    @Override
    protected void copyProxyCookie(HttpServletRequest servletRequest,
                                   HttpServletResponse servletResponse, String headerValue) {
        List<HttpCookie> cookies = HttpCookie.parse(headerValue);
        String path = servletRequest.getContextPath(); // path starts with / or is empty string
        path += servletRequest.getServletPath(); // servlet path starts with / or is empty string
        if(path.isEmpty()){
            path = "/";
        }

        for (HttpCookie cookie : cookies) {
            //set cookie name prefixed w/ a proxy value so it won't collide w/ other cookies
            String cookieName = doPreserveCookies ? cookie.getName() : getCookieNamePrefix(cookie.getName()) + cookie.getName();
            Cookie servletCookie = new Cookie(cookieName, cookie.getValue());
            servletCookie.setComment(cookie.getComment());
            servletCookie.setMaxAge((int) cookie.getMaxAge());
            //fix: preserve path when preserving cookies
            String cookiePath = doPreserveCookies ? cookie.getPath() : path;
            servletCookie.setPath(cookiePath); //set to the path of the proxy servlet
            // don't set cookie domain
            servletCookie.setSecure(cookie.getSecure());
            servletCookie.setVersion(cookie.getVersion());
            servletResponse.addCookie(servletCookie);
        }
    }
}
