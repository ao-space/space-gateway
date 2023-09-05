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

package space.ao.services.support.jwt;

import io.smallrye.jwt.algorithm.SignatureAlgorithm;
import io.smallrye.jwt.build.JwtClaimsBuilder;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map.Entry;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import space.ao.services.support.service.ServiceError;
import space.ao.services.support.service.ServiceOperationException;

import jakarta.inject.Singleton;

@Singleton
public class JwtUtils {
    JwtUtils(){}
    /**
     * jwt 签名预处理
     * @param builder JwtClaimsBuilder
     * @return 未签名的 header 和 payload
     */
    public String privateKeySignPre(JwtClaimsBuilder builder){
        String result;

        try {
            Class<?> clazz = Class.forName("io.smallrye.jwt.build.impl.JwtSignatureImpl");

            Field claimsField = clazz.getDeclaredField("claims");
            setFieldAccessible(claimsField);
            JwtClaims claims = (JwtClaims) claimsField.get(builder);

            Field headersField = clazz.getDeclaredField("headers");
            setFieldAccessible(headersField);
            HashMap<?, ?> headers = (HashMap<?, ?>) headersField.get(builder);

            JsonWebSignature jws = new JsonWebSignature();

            for (Entry<?, ?> stringObjectEntry : headers.entrySet()) {
                jws.setHeader((String) stringObjectEntry.getKey(), stringObjectEntry.getValue());
            }

            if (!headers.containsKey("typ")) {
                jws.setHeader("typ", "JWT");
            }

            String algorithm = (String)headers.get("alg");
            if ("none".equals(algorithm)) {
                jws.setAlgorithmConstraints(AlgorithmConstraints.ALLOW_ONLY_NONE);
            }
            if (algorithm == null) {
                algorithm = SignatureAlgorithm.RS256.name();
            }

            jws.setAlgorithmHeaderValue(algorithm);
            jws.setPayload(claims.toJson());

            result = jws.getHeaders().getEncodedHeader() + "." + jws.getEncodedPayload();

        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            throw new ServiceOperationException(ServiceError.SIGNATURE_FAILED);
        }

        return result;
    }

    private static void setFieldAccessible(Field field){
        field.setAccessible(true);
    }

}
