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

package space.ao.services.support.log;

import com.google.common.base.Stopwatch;
import io.quarkus.runtime.Startup;
import io.quarkus.scheduler.Scheduled;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.SneakyThrows;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@ApplicationScoped
@Startup
public class LogFileManagerUtils {
  static final Logger LOG = Logger.getLogger("app.log");
  @ConfigProperty(name = "quarkus.log.file.path")
  String quarkusLogFilePath;

  @Scheduled(every = "P1D")
  @SneakyThrows
  @PostConstruct
  void deleteOldLogs() {
    Stopwatch stopwatch = Stopwatch.createStarted();
    var file = quarkusLogFilePath;
    var parent = Paths.get(file).getParent();
    long totalSize = calculateTotalSize(parent);

    long maxFileSize = (long) 200 * 1024 * 1024;
    if (totalSize > maxFileSize) {
      try(Stream<Path> files = Files.list(parent)){
        var logFiles = files
                .filter(Files::isRegularFile)
                .sorted(Comparator.comparingLong(f -> f.toFile().lastModified()))
                .toList();

        for (var f : logFiles) {
          long fileSize = f.toFile().length();
          if (totalSize > maxFileSize) {
            LOG.info("delete log file: " + f + " - " + f.toFile().lastModified());
            Files.deleteIfExists(f);
            totalSize -= fileSize;
          }
        }
      }
    }
    LOG.info("regularly clean log file completed - " + stopwatch.elapsed(TimeUnit.SECONDS));

  }

  private long calculateTotalSize(Path dir) throws IOException {
    if(Objects.isNull(dir)){
      return 0;
    }
    try (Stream<Path> files = Files.walk(dir)) {
      return files.filter(Files::isRegularFile)
              .mapToLong(f -> f.toFile().length())
              .sum();
    }
  }
}
