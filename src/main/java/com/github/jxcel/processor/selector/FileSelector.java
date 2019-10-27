/*
 * MIT License
 *
 * Copyright (c) [2019] [He Zhang]
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

package com.github.jxcel.processor.selector;

import com.github.jxcel.annotation.Column;
import com.github.jxcel.exception.JxcelException;
import com.github.jxcel.processor.adapter.Adapter;
import com.github.jxcel.processor.adapter.AdapterFactory;
import com.github.jxcel.processor.adapter.NullAdapter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Workbook;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public abstract class FileSelector {

    protected Map<Integer, Field> fieldIndexes;
    protected Map<Field, Adapter<String, ?>> fieldAdapters;
    protected Workbook workbook;

    public abstract <T>Stream<T> selectFromFile(Selector selector);

    public void init(Field[] fields) throws Exception {
        fieldIndexes = new HashMap<>(fields.length);
        fieldAdapters = new HashMap<>();

        for (Field field : fields) {
            Column column = field.getAnnotation(Column.class);
            if (column == null) {
                continue;
            }
            // Cancel Java language access check
            // Turn off the security check to achieve the purpose of increasing the reflection speed
            field.setAccessible(true);
            fieldIndexes.put(column.index(), field);

            Adapter adapter = null;
            if (NullAdapter.class.equals(column.adapter())) {
                adapter = AdapterFactory.getInstance(field);
            } else {
                adapter = column.adapter().newInstance();
            }
            if (adapter != null) {
                fieldAdapters.put(field, adapter);
            }
        }
    }

    public Object getCellValue(Cell cell, Field field) throws JxcelException {
        return cell.getStringCellValue();
    }
}
