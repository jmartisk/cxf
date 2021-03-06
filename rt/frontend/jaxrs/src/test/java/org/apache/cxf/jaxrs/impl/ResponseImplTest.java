/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.cxf.jaxrs.impl;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Link.Builder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.Variant;
import javax.ws.rs.core.Variant.VariantListBuilder;
import javax.ws.rs.ext.RuntimeDelegate;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

import org.apache.cxf.jaxrs.resources.Book;
import org.apache.cxf.jaxrs.utils.HttpUtils;

import org.junit.Assert;
import org.junit.Test;


public class ResponseImplTest extends Assert {
    
    @Test
    public void testResourceImpl() {
        String entity = "bar";
        ResponseImpl ri = new ResponseImpl(200, entity);
        assertEquals("Wrong status", ri.getStatus(), 200);
        assertSame("Wrong entity", entity, ri.getEntity());
        
        MetadataMap<String, Object> meta = new MetadataMap<String, Object>();
        ri.addMetadata(meta);
        ri.getMetadata();
        assertSame("Wrong metadata", meta, ri.getMetadata());
        assertSame("Wrong metadata", meta, ri.getHeaders());
    }
    
    @Test
    public void testGetHeaderStringUsingHeaderDelegate() throws Exception {
        StringBean bean = new StringBean("s3");
        RuntimeDelegate original = RuntimeDelegate.getInstance();
        RuntimeDelegate.setInstance(new StringBeanRuntimeDelegate(original));
        try {
            Response response = Response.ok().header(bean.get(), bean).build();
            String header = response.getHeaderString(bean.get());
            assertTrue(header.contains(bean.get()));
        } finally {
            RuntimeDelegate.setInstance(original);
            StringBeanRuntimeDelegate.assertNotStringBeanRuntimeDelegate();
        }
    }
    
    @Test
    public void testHasEntity() {
        assertTrue(new ResponseImpl(200, "").hasEntity());
        assertFalse(new ResponseImpl(200).hasEntity());
    }
    
    @Test
    public void testGetEntityUnwrapped() {
        final Book book = new Book();
        Response r = Response.ok().entity(
            new GenericEntity<Book>(book) {
            }
        ).build();
        assertSame(book, r.getEntity());
    }
    
    @Test
    public void testGetEntity() {
        final Book book = new Book();
        Response r = Response.ok().entity(book).build();
        assertSame(book, r.getEntity());
    }
    
    @Test(expected = IllegalStateException.class)
    public void testGetEntityAfterClose() {
        Response response = Response.ok("entity").build();
        response.close();
        response.getEntity();
    }
    
    @Test
    public void testStatuInfoForOKStatus() {
        StatusType si = new ResponseImpl(200, "").getStatusInfo();
        assertNotNull(si);
        assertEquals(200, si.getStatusCode());
        assertEquals(Status.Family.SUCCESSFUL, si.getFamily());
        assertEquals("OK", si.getReasonPhrase());
    }
    
    @Test
    public void testStatuInfoForClientErrorStatus() {
        StatusType si = new ResponseImpl(400, "").getStatusInfo();
        assertNotNull(si);
        assertEquals(400, si.getStatusCode());
        assertEquals(Status.Family.CLIENT_ERROR, si.getFamily());
        assertEquals("Bad Request", si.getReasonPhrase());
    }
    
    @Test
    public void testStatuInfoForClientErrorStatus2() {
        StatusType si = new ResponseImpl(499, "").getStatusInfo();
        assertNotNull(si);
        assertEquals(499, si.getStatusCode());
        assertEquals(Status.Family.CLIENT_ERROR, si.getFamily());
        assertEquals("", si.getReasonPhrase());
    }
    
    @Test(expected = IllegalStateException.class)
    public void testHasEntityAfterClose() {
        Response r = new ResponseImpl(200, new ByteArrayInputStream("data".getBytes())); 
        assertTrue(r.hasEntity());
        r.close();
        r.hasEntity();
    }
    
    
    @Test
    public void testBufferEntityNoEntity() {
        Response r = new ResponseImpl(200); 
        assertFalse(r.bufferEntity());
    }
    
    @Test
    public void testGetHeaderString() {
        ResponseImpl ri = new ResponseImpl(200);
        MetadataMap<String, Object> meta = new MetadataMap<String, Object>();
        ri.addMetadata(meta);
        assertNull(ri.getHeaderString("a"));
        meta.putSingle("a", "aValue");
        assertEquals("aValue", ri.getHeaderString("a"));
        meta.add("a", "aValue2");
        assertEquals("aValue,aValue2", ri.getHeaderString("a"));
    }
    
    @Test
    public void testGetHeaderStrings() {
        ResponseImpl ri = new ResponseImpl(200);
        MetadataMap<String, Object> meta = new MetadataMap<String, Object>();
        meta.add("Set-Cookie", NewCookie.valueOf("a=b"));
        ri.addMetadata(meta);
        MultivaluedMap<String, String> headers = ri.getStringHeaders();
        assertEquals(1, headers.size());
        assertEquals("a=b;Version=1", headers.getFirst("Set-Cookie"));
    }
    
    @Test
    public void testGetCookies() {
        ResponseImpl ri = new ResponseImpl(200);
        MetadataMap<String, Object> meta = new MetadataMap<String, Object>();
        meta.add("Set-Cookie", NewCookie.valueOf("a=b"));
        meta.add("Set-Cookie", NewCookie.valueOf("c=d"));
        ri.addMetadata(meta);
        Map<String, NewCookie> cookies = ri.getCookies();
        assertEquals(2, cookies.size());
        assertEquals("a=b;Version=1", cookies.get("a").toString());
        assertEquals("c=d;Version=1", cookies.get("c").toString());
    }
    
    @Test
    public void testGetContentLength() {
        ResponseImpl ri = new ResponseImpl(200);
        MetadataMap<String, Object> meta = new MetadataMap<String, Object>();
        ri.addMetadata(meta);
        assertEquals(-1, ri.getLength());
        meta.add("Content-Length", "10");
        assertEquals(10, ri.getLength());
    }
    
    @Test
    public void testGetDate() {
        doTestDate(HttpHeaders.DATE);
    }
    
    @Test
    public void testLastModified() {
        doTestDate(HttpHeaders.LAST_MODIFIED);
    }
    
    public void doTestDate(String dateHeader) {
        boolean date = HttpHeaders.DATE.equals(dateHeader);
        ResponseImpl ri = new ResponseImpl(200);
        MetadataMap<String, Object> meta = new MetadataMap<String, Object>();
        meta.add(dateHeader, "Tue, 21 Oct 2008 17:00:00 GMT");
        ri.addMetadata(meta);
        assertEquals(HttpUtils.getHttpDate("Tue, 21 Oct 2008 17:00:00 GMT"), 
                     date ? ri.getDate() : ri.getLastModified());
    }
    
    @Test
    public void testEntityTag() {
        ResponseImpl ri = new ResponseImpl(200);
        MetadataMap<String, Object> meta = new MetadataMap<String, Object>();
        meta.add(HttpHeaders.ETAG, "1234");
        ri.addMetadata(meta);
        assertEquals("\"1234\"", ri.getEntityTag().toString());
    }
    
    @Test
    public void testLocation() {
        ResponseImpl ri = new ResponseImpl(200);
        MetadataMap<String, Object> meta = new MetadataMap<String, Object>();
        meta.add(HttpHeaders.LOCATION, "http://localhost:8080");
        ri.addMetadata(meta);
        assertEquals("http://localhost:8080", ri.getLocation().toString());
    }
    
    @Test
    public void testGetLanguage() {
        ResponseImpl ri = new ResponseImpl(200);
        MetadataMap<String, Object> meta = new MetadataMap<String, Object>();
        meta.add(HttpHeaders.CONTENT_LANGUAGE, "en-US");
        ri.addMetadata(meta);
        assertEquals("en_US", ri.getLanguage().toString());
    }

    @Test
    public void testGetMediaType() {
        ResponseImpl ri = new ResponseImpl(200);
        MetadataMap<String, Object> meta = new MetadataMap<String, Object>();
        meta.add(HttpHeaders.CONTENT_TYPE, "text/xml");
        ri.addMetadata(meta);
        assertEquals("text/xml", ri.getMediaType().toString());
    }
    
    @Test
    public void testGetNoLinkBuilder() throws Exception {
        Response response = Response.ok().build();
        Builder builder = response.getLinkBuilder("anyrelation");
        assertNull(builder);
    }
    
    protected static List<Variant> getVariantList(List<String> encoding,
                                                  MediaType... mt) {
        return Variant.VariantListBuilder.newInstance()
            .mediaTypes(mt)
            .languages(new Locale("en", "US"), new Locale("en", "GB"), new Locale("zh", "CN"))
            .encodings(encoding.toArray(new String[]{}))
            .add()
            .build();
    }
    
    @Test
    public void testGetLinksSameRel() {
        ResponseImpl ri = new ResponseImpl(200);
        MetadataMap<String, Object> meta = new MetadataMap<String, Object>();
        ri.addMetadata(meta);
        
        meta.add(HttpHeaders.LINK, "<http://link1>");
        meta.add(HttpHeaders.LINK, "<http://link2>");
        
        Set<Link> links = ri.getLinks();
        assertEquals(2, links.size());
        assertTrue(links.contains(Link.valueOf("<http://link1>")));
        assertTrue(links.contains(Link.valueOf("<http://link2>")));
    }
    
    @Test
    public void testGetLinks() {
        ResponseImpl ri = new ResponseImpl(200);
        MetadataMap<String, Object> meta = new MetadataMap<String, Object>();
        ri.addMetadata(meta);
        assertFalse(ri.hasLink("next"));
        assertNull(ri.getLink("next"));
        assertFalse(ri.hasLink("prev"));
        assertNull(ri.getLink("prev"));
        
        meta.add(HttpHeaders.LINK, "<http://next>;rel=next");
        meta.add(HttpHeaders.LINK, "<http://prev>;rel=prev");
        
        assertTrue(ri.hasLink("next"));
        Link next = ri.getLink("next");
        assertNotNull(next);
        assertTrue(ri.hasLink("prev"));
        Link prev = ri.getLink("prev");
        assertNotNull(prev);
        
        Set<Link> links = ri.getLinks();
        assertTrue(links.contains(next));
        assertTrue(links.contains(prev));
        
        assertEquals("http://next", next.getUri().toString());
        assertEquals("next", next.getRel());
        assertEquals("http://prev", prev.getUri().toString());
        assertEquals("prev", prev.getRel());
    }
    
    public static class StringBean {
        private String header;

        public StringBean(String header) {
            super();
            this.header = header;
        }
        
        public String get() {
            return header;
        }

        public void set(String h) {
            this.header = h;
        }
        
        @Override
        public String toString() {
            return "StringBean. To get a value, use rather #get() method.";
        }
    }
    
    public static class StringBeanRuntimeDelegate extends RuntimeDelegate {
        private RuntimeDelegate original;
        public StringBeanRuntimeDelegate(RuntimeDelegate orig) {
            super();
            this.original = orig;
            assertNotStringBeanRuntimeDelegate(orig);
        }

        @Override
        public <T> T createEndpoint(Application arg0, Class<T> arg1)
            throws IllegalArgumentException, UnsupportedOperationException {
            return original.createEndpoint(arg0, arg1);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> HeaderDelegate<T> createHeaderDelegate(Class<T> arg0)
            throws IllegalArgumentException {
            if (arg0 == StringBean.class) {
                return (HeaderDelegate<T>) new StringBeanHeaderDelegate();
            } else {
                return original.createHeaderDelegate(arg0);
            }
        }

        @Override
        public ResponseBuilder createResponseBuilder() {
            return original.createResponseBuilder();
        }

        @Override
        public UriBuilder createUriBuilder() {
            return original.createUriBuilder();
        }

        @Override
        public VariantListBuilder createVariantListBuilder() {
            return original.createVariantListBuilder();
        }

        public RuntimeDelegate getOriginal() {
            return original;
        }

        public static final void assertNotStringBeanRuntimeDelegate() {
            RuntimeDelegate delegate = RuntimeDelegate.getInstance();
            assertNotStringBeanRuntimeDelegate(delegate);
        }

        public static final void assertNotStringBeanRuntimeDelegate(RuntimeDelegate delegate) {
            if (delegate instanceof StringBeanRuntimeDelegate) {
                StringBeanRuntimeDelegate sbrd = (StringBeanRuntimeDelegate) delegate;
                if (sbrd.getOriginal() != null) {
                    RuntimeDelegate.setInstance(sbrd.getOriginal());
                    throw new RuntimeException(
                        "RuntimeDelegate.getInstance() is StringBeanRuntimeDelegate");
                }
            }
        }

        @Override
        public Builder createLinkBuilder() {
            return original.createLinkBuilder();
        }
    }
    
    public static class StringBeanHeaderDelegate implements HeaderDelegate<StringBean> {

        @Override
        public StringBean fromString(String string) throws IllegalArgumentException {
            return new StringBean(string);
        }

        @Override
        public String toString(StringBean bean) throws IllegalArgumentException {
            return bean.get();
        }

    }
}
