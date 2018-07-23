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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.mitre.dsmiley.httpproxy.ProxyServlet;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.*;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Extending {@link ProxyServlet} to fix the cookie path which was overriden even when preserveCookie is set to true
 */
@Slf4j
public class PreservingCookiePathProxyServlet extends ProxyServlet {

    public static final String P_PORTAL_USER = "portalUser";
    public static final String P_PORTAL_PASSWORD = "portalPassword";
    private BonitaCredentials credentials;


    /**
     * Copy cookie from the proxy to the servlet client.
     * Replaces cookie path to local path and renames cookie to avoid collisions.
     */
    @Override
    protected void copyProxyCookie(HttpServletRequest servletRequest,
                                   HttpServletResponse servletResponse, String headerValue) {
        List<HttpCookie> cookies = HttpCookie.parse(headerValue);
        String path = servletRequest.getContextPath(); // path starts with / or is empty string
        path += servletRequest.getServletPath(); // servlet path starts with / or is empty string
        if (path.isEmpty()) {
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

    @Override
    public void init() throws ServletException {
        super.init();
        credentials = new BonitaCredentials();
        credentials.username = getConfigParam(P_PORTAL_USER);
        credentials.password = getConfigParam(P_PORTAL_PASSWORD);
        if (targetHost != null) {
            try {
                credentials.loginServletURI = new URL(targetHost + "/bonita/loginservice").toURI();
            } catch (URISyntaxException | MalformedURLException e) {
                throw new ServletException(e);
            }
        }
    }


    @Override
    protected HttpResponse doExecute(HttpServletRequest servletRequest, HttpServletResponse servletResponse, HttpRequest proxyRequest) throws IOException {
        //reuse previous login
        if (credentials.jsessionID != null) {
            setJSessionID(proxyRequest, credentials.jsessionID);
        }
        HttpResponse httpResponse = super.doExecute(servletRequest, servletResponse, proxyRequest);
        //when a login is required, we try to login on bonita platform
        if (httpResponse.getStatusLine().getStatusCode() == 401 && credentials.isSet()) {
            log.info("response 401, will try to login");
            HttpResponse loginResponse = login(credentials);
            String responseContent = IOUtils.toString(httpResponse.getEntity().getContent());
            int statusCode = loginResponse.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                log.error("Unable to log in bonita platform, code {}, response: {}", statusCode, responseContent);
                return httpResponse;
            }
            credentials.jsessionID = getJSessionId(loginResponse);
            log.info("created server session: {}", credentials.jsessionID);
            setJSessionID(proxyRequest, credentials.jsessionID);
            return super.doExecute(servletRequest, servletResponse, proxyRequest);
        }
        return httpResponse;
    }

    private void setJSessionID(HttpRequest proxyRequest, String jSessionId) {
        proxyRequest.setHeader("Cookie", jSessionId);
    }

    private HttpResponse login(BonitaCredentials credentials) throws IOException {
        BasicHttpContext httpContext = new BasicHttpContext();
        BasicCookieStore cookieStore = new BasicCookieStore();
        httpContext.setAttribute("http.cookie-store", cookieStore);
        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("username", credentials.username));
        urlParameters.add(new BasicNameValuePair("password", credentials.password));
        urlParameters.add(new BasicNameValuePair("redirect", "false"));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(urlParameters, "utf-8");
        HttpPost postRequest = new HttpPost(credentials.loginServletURI);
        postRequest.setEntity(entity);
        return getProxyClient().execute(postRequest, httpContext);
    }


    private String getJSessionId(HttpResponse response) {
        List<Header> headers = Arrays.asList(response.getHeaders("Set-Cookie"));
        String prefix = "JSESSIONID=";
        Optional<String> any = headers.stream()
                .filter(h -> h.getValue().contains(prefix))
                .map(Header::getValue)
                .map(value -> Arrays.asList(value.split(";[ ]*")))
                .flatMap(Collection::stream)
                .filter(s -> s.startsWith(prefix))
                .findAny();
        if (any.isPresent()) {
            return any.get();
        } else {
            throw new RuntimeException("Unable to find JSESSIONID in headers " + headers);
        }
    }

    private static class BonitaCredentials {
        String username;
        String password;
        URI loginServletURI;
        String jsessionID;

        boolean isSet() {
            return !isBlank(username) && !isBlank(password) && loginServletURI != null;
        }
    }

}
