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

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class GreetingResourceTest {

    @Test
    void testHelloEndpoint() {
        given()
          .when().get("/hello")
          .then()
             .statusCode(200)
             .body(is("Hello"));
    }

    @Test
    void testDownloadEndpoint() {
        given()
            .queryParam("file", "hello.txt")
            .queryParam("content", "hello")
            .when().get("/hello/download")
            .then()
            .statusCode(200)
            .body(is("hello"));
    }

    @Test
    void testUploadEndpoint() {
        given()
            .multiPart("fileName", "hello.txt")
            .multiPart("file", "hello.txt", "hello".getBytes(StandardCharsets.UTF_8))
            .when().post("/hello/upload")
            .then()
            .statusCode(200)
            .body( is("hello"));
    }
}