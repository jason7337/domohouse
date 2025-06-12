package com.pdm.domohouse.data.database.converter;

import androidx.room.TypeConverter;

import java.util.Date;

/**
 * Convertidor de tipos para Room Database
 * Convierte entre Date y Long para almacenamiento en SQLite
 */
public class DateConverter {
    
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }
    
    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}