package ru.sibguti.lab2;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/*
 * КОПИЯ ФАЙЛА. Создан в задании 1, перенесён в задание 3 без изменений.
 * Зачем он нужен в этом задании: переводит вывод System.out в UTF-8, иначе русские строки в консоли Windows печатаются мусором.
 * Копия нужна для автономности папки задания: правьте её здесь, остальные задания не затронуты.
 * Класс Console — маленький помощник для всех main-методов лабораторной.
 * Он делает ровно одну вещь: переключает стандартный вывод System.out
 * в кодировку UTF-8, потому что в Windows консоль по умолчанию работает
 * в кодировке 866/1251 и русский текст иначе превращается в «кракозябры».
 * Зависимости: его вызывает main каждого задания (первой строкой),
 * чтобы весь остальной код мог просто писать System.out.println.
 */
public final class Console {

    private Console() {
    }

    public static void enableUtf8() {
        PrintStream utf8Stream = new PrintStream(
                new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8);
        System.setOut(utf8Stream);
    }
}
