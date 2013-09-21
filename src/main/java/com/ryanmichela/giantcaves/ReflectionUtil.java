package com.ryanmichela.giantcaves;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Copyright 2013 Ryan Michela
 */
public class ReflectionUtil {
    public static void setProtectedValue(Object o, String field, Object newValue) {
        setProtectedValue(o.getClass(), o, field, newValue);
    }

    public static void setProtectedValue(Class c, String field, Object newValue) {
        setProtectedValue(c, null, field, newValue);
    }

    public static void setProtectedValue(Class c, Object o, String field, Object newValue) {
        try {

            Field f = c.getDeclaredField(field);

            f.setAccessible(true);

            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(f, f.getModifiers() & ~Modifier.FINAL);

            f.set(o, newValue);
        } catch (NoSuchFieldException ex) {
            System.out.println("*** " + c.getName() + ":" + ex);
        } catch (IllegalAccessException ex) {
            System.out.println("*** " + c.getName() + ":" + ex);
        }
    }

    public static <T> T getProtectedValue(Object obj, String field) {
        try {
            Class c = obj.getClass();
            Field f = c.getDeclaredField(field);
            f.setAccessible(true);
            return (T) f.get(obj);
        } catch (Exception ex) {
            System.out.println("*** " + obj.getClass().getName() + ":" + ex);
            return null;
        }
    }
}
