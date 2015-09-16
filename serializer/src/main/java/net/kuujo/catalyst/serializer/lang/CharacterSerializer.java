/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.kuujo.catalyst.serializer.lang;

import net.kuujo.catalyst.buffer.BufferInput;
import net.kuujo.catalyst.buffer.BufferOutput;
import net.kuujo.catalyst.serializer.Serializer;
import net.kuujo.catalyst.serializer.TypeSerializer;

/**
 * Character serializer.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class CharacterSerializer implements TypeSerializer<Character> {

  @Override
  public void write(Character object, BufferOutput buffer, Serializer serializer) {
    buffer.writeChar(object);
  }

  @Override
  public Character read(Class<Character> type, BufferInput buffer, Serializer serializer) {
    return buffer.readChar();
  }

}
