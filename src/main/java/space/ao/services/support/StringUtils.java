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

/*
 * Copyright 2002-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package space.ao.services.support;

import javax.annotation.Nullable;

public abstract class StringUtils {

  /**
   * Replace all occurrences of a substring within a string with another string.
   * @param inString {@code String} to examine
   * @param oldPattern {@code String} to replace
   * @param newPattern {@code String} to insert
   * @return a {@code String} with the replacements
   */
  public static String replace(String inString, String oldPattern, String newPattern) {
    if (!hasLength(inString) || !hasLength(oldPattern) || newPattern == null) {
      return inString;
    }
    int index = inString.indexOf(oldPattern);
    if (index == -1) {
      // no occurrence -> can return input as-is
      return inString;
    }

    int capacity = inString.length();
    if (newPattern.length() > oldPattern.length()) {
      capacity += 16;
    }
    StringBuilder sb = new StringBuilder(capacity);

    int pos = 0;  // our position in the old string
    int patLen = oldPattern.length();
    while (index >= 0) {
      sb.append(inString, pos, index);
      sb.append(newPattern);
      pos = index + patLen;
      index = inString.indexOf(oldPattern, pos);
    }

    // append any characters to the right of a match
    sb.append(inString, pos, inString.length());
    return sb.toString();
  }

  /**
   * Check that the given {@code CharSequence} is neither {@code null} nor
   * of length 0.
   * <p>Note: this method returns {@code true} for a {@code CharSequence}
   * that purely consists of whitespace.
   * <p><pre class="code">
   * StringUtils.hasLength(null) = false
   * StringUtils.hasLength("") = false
   * StringUtils.hasLength(" ") = true
   * StringUtils.hasLength("Hello") = true
   * </pre>
   * @param str the {@code CharSequence} to check (may be {@code null})
   * @return {@code true} if the {@code CharSequence} is not {@code null} and has length
   */
  public static boolean hasLength(@Nullable CharSequence str) {
    return (str != null && str.length() > 0);
  }
  public static boolean isBlank(String strings) {
    return org.apache.commons.lang3.StringUtils.isBlank(strings);
  }
}
