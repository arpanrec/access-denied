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

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.HashMap;
import java.util.Map;
import tools.jackson.databind.ObjectMapper;

@Converter
public class AccessLogQueryStringAttributeConverter implements AttributeConverter<Map<String, String[]>, String> {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Map<String, String[]> attribute) {
        try {
            return mapper.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert map to JSON string", e);
        }
    }

    @Override
    public Map<String, String[]> convertToEntityAttribute(String dbData) {
        try {
            return mapper.readValue(
                    dbData, mapper.getTypeFactory().constructMapType(HashMap.class, String.class, String[].class));
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert JSON string to list of roles", e);
        }
    }
}
