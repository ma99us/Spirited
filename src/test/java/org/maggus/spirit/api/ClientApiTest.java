package org.maggus.spirit.api;

import junit.framework.TestCase;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.junit.Classes;
import org.apache.openejb.junit.EnableServices;
import org.apache.openejb.junit.Module;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.maggus.spirit.services.WhiskyService;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

//@EnableServices(value = "jaxrs", httpDebug = true)
//@RunWith(ApplicationComposer.class)
public class ClientApiTest {
    private static final URI BASE_URI = URI.create("http://localhost:8080/Spirited");		// localhost

//    @Module
//    @Classes(value = {WhiskyApi.class, WhiskyService.class}) //This enables the CDI magic
//    public WebApp app() {
//        return new WebApp().contextRoot("test");
//    }

    @Test
    public void testGetAllWhisky() throws Exception {
//        Client client = ClientBuilder.newClient();
//        WebTarget webTarget = client.target(BASE_URI + "/api/whisky");
//        WebTarget getAllWT = webTarget.path("all");
        //Response res = getAllWT.queryParam("username", username).queryParam("password", password).request(MediaType.APPLICATION_JSON_TYPE).get(User.class);
//        Response res = getAllWT.request(MediaType.APPLICATION_JSON_TYPE).get();

//        final Response resp = WebClient.create("http://localhost:4204").path("/test/api/whisky/")
//                .accept(MediaType.APPLICATION_XML_TYPE)
//                .get(Response.class);
//        assertNotNull(resp);
//        System.out.println("res.getEntity(): " + resp.getEntity());
        //assertEquals("Hi REST!", message);
    }
}
