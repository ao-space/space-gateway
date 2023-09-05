/*
 * Copyright (c) 2022 Institute of Software Chinese Academy of Sciences (ISCAS)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package space.ao.services;

import com.google.common.io.ByteStreams;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.PartType;
import space.ao.services.support.log.Logged;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Tag(name = "Space Gateway Hello Service",
    description = "Provides gateway service hello helper APIs(used for testing).")
@Path("/hello")
public class GreetingResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Logged
    public String hello() {
        return "Hello";
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/upload")
    public String upload(MultipartBody mtp) throws Exception {
        final var file = mtp.file;
        try (file) {
            return new String(ByteStreams.toByteArray(file), StandardCharsets.UTF_8);
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/upload-one")
    public String uploadOne(@QueryParam("FileName") String fileName, InputStream file) {
        try (file) {
            return fileName + ", " + new String(ByteStreams.toByteArray(file), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "";
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("/download")
    public Response download(@QueryParam("file") String file, @QueryParam("content") String content) {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        Response.ResponseBuilder response = Response.ok(
            (StreamingOutput) output -> {
                try (output; var input = new ByteArrayInputStream(bytes)) {
                    ByteStreams.copy(input, output);
                }
            });
        response.header("Content-Disposition", "attachment;filename=" + file);
        return response.build();
    }

    public static class MultipartBody {
        @FormParam("file")
        @PartType(MediaType.APPLICATION_OCTET_STREAM)
        public InputStream file;

        @FormParam("fileName")
        @PartType(MediaType.TEXT_PLAIN)
        public String fileName;
    }
}