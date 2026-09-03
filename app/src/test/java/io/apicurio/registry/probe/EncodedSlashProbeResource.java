package io.apicurio.registry.probe;

import jakarta.ws.rs.Encoded;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * Test-only probe for how the HTTP stack (Vert.x + Undertow servlet bridge + RESTEasy Classic)
 * treats an encoded slash (%2F) in a path segment. Not shipped - lives under src/test/java.
 */
@Path("/probe")
@Produces(MediaType.TEXT_PLAIN)
public class EncodedSlashProbeResource {

    @GET
    @Path("/one/{name}")
    public String single(@PathParam("name") String name) {
        return "decoded=[" + name + "]";
    }

    @GET
    @Path("/one-enc/{name}")
    public String singleEncoded(@Encoded @PathParam("name") String name) {
        return "encoded=[" + name + "]";
    }

    @GET
    @Path("/one-regex/{name:.+}")
    public String singleRegex(@Encoded @PathParam("name") String name) {
        return "regex=[" + name + "]";
    }

    @GET
    @Path("/two/{a}/{b}")
    public String two(@PathParam("a") String a, @PathParam("b") String b) {
        return "two=[" + a + "][" + b + "]";
    }
}
