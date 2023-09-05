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

package space.ao.services.support.model;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Data
@Schema(description = "网关总体加密算法相关的配置信息")
public class AlgorithmConfig {
    PublicKey publicKey;
    Transportation transportation;

    public static AlgorithmConfig of(String publicKeyAlg, Integer publicKeySize, String transportationAlg,
                                     Integer transportationKeySize, String transformation, String initializationVector){
        AlgorithmConfig algorithmConfig = new AlgorithmConfig();
        PublicKey publicKey = new PublicKey();
        Transportation transportation = new Transportation();
        {
            publicKey.algorithm = publicKeyAlg;
            publicKey.keySize = publicKeySize;
            transportation.algorithm = transportationAlg;
            transportation.keySize = transportationKeySize;
            transportation.transformation = transformation;
            transportation.initializationVector = initializationVector;

            algorithmConfig.publicKey = publicKey;
            algorithmConfig.transportation =transportation;
        }

        return algorithmConfig;
    }

}
