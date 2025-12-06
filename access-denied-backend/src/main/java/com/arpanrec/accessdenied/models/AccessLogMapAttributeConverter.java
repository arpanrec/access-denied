/*

           DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
                   Version 2, December 2004

Copyright (C) 2025 Arpan Mandal <me@arpanrec.com>

Everyone is permitted to copy and distribute verbatim or modified
copies of this license document, and changing it is allowed as long
as the name is changed.

           DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
  TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION

 0. You just DO WHAT THE FUCK YOU WANT TO.

*/
package com.arpanrec.accessdenied.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Map;

@Converter
public class AccessLogMapAttributeConverter implements AttributeConverter<Map<String, String>, String> {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Map<String, String> attribute) {
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert map to JSON string", e);
        }
    }

    @Override
    public Map<String, String> convertToEntityAttribute(String dbData) {
        try {
            return objectMapper.readValue(
                    dbData, objectMapper.getTypeFactory().constructMapType(Map.class, String.class, String.class));
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert JSON string to list of roles", e);
        }
    }
}
