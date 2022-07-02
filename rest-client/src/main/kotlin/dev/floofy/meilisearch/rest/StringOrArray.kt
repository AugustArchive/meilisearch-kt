/*
 * 🔍 Meilisearch for Kotlin: Type-safe and Kotlin Coroutine-based client for Meilisearch
 * Copyright (c) 2022 Noel <cutie@floofy.dev>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dev.floofy.meilisearch.rest

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@kotlinx.serialization.Serializable(with = StringOrArray.Companion::class)
class StringOrArray(private val value: Any) {
    init {
        check(value is String || value is List<*>) { "Expected `String` or `List<*>`, received ${value::class}" }
        check(value is List<*> && value.every { it !is String }) { "Expected a list of strings." }
    }

    val stringOrNull: String?
        get() = value as? String

    @Suppress("UNCHECKED_CAST")
    val listOrNull: List<String>?
        get() = value as? List<String>

    val list: List<String>
        get() = listOrNull ?: error("Casted value was not a List.")

    val string: String
        get() = stringOrNull ?: error("Casted value was not a String.")

    companion object: KSerializer<StringOrArray> {
        override val descriptor: SerialDescriptor = buildClassSerialDescriptor("meilisearch.StringOrArray")
        override fun deserialize(decoder: Decoder): StringOrArray {
            return try {
                val listOfStrings = decoder.decodeSerializableValue(ListSerializer(String.serializer()))
                StringOrArray(listOfStrings)
            } catch (e: SerializationException) {
                try {
                    StringOrArray(decoder.decodeString())
                } catch (e: Exception) {
                    throw e
                }
            } catch (e: Exception) {
                throw e
            }
        }

        override fun serialize(encoder: Encoder, value: StringOrArray) = try {
            val listOfStrings = value.list
            encoder.encodeSerializableValue(ListSerializer(String.serializer()), listOfStrings)
        } catch (e: SerializationException) {
            try {
                val string = value.string
                encoder.encodeString(string)
            } catch (e: Exception) {
                throw e
            }
        } catch (e: Exception) {
            throw e
        }
    }
}
