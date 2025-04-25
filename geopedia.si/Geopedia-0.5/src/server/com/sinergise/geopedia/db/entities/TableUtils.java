package com.sinergise.geopedia.db.entities;

import java.sql.SQLException;

import com.sinergise.geopedia.core.entities.Field;
import com.sinergise.geopedia.core.entities.Field.FieldFlags;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.core.entities.walk.FieldPath;
import com.sinergise.geopedia.core.entities.walk.MetaFieldPath;
import com.sinergise.geopedia.core.entities.walk.TablePath;
import com.sinergise.geopedia.core.entities.walk.UserFieldPath;
import com.sinergise.geopedia.core.style.consts.ConstString;
import com.sinergise.geopedia.core.style.fields.NumberField;
import com.sinergise.geopedia.core.style.fields.StringField;
import com.sinergise.geopedia.core.style.model.StringSpec;
import com.sinergise.geopedia.core.style.model.StyleSpec;
import com.sinergise.geopedia.core.style.strings.StringConcat;
import com.sinergise.geopedia.core.style.strings.StringFromNumber;
import com.sinergise.geopedia.style.ParseStyleException;

public class TableUtils {
    private TableUtils() {
    }

    @Deprecated
    public static StringSpec getTextRepSpec(Table tbl, MetaData meta)
            throws ParseStyleException, SQLException {
//        if (tbl.textRepSpec == null) {
//            return tbl.textRepSpec = TextRepCodec.decode(tbl, tbl.textRepExpr, meta);
//        } else {
//            return tbl.textRepSpec;
//        }
    	return null;
    }

    @Deprecated
    public static StyleSpec getStyleSpec(Table tbl, MetaData meta) throws ParseStyleException,
            SQLException {
//        if (tbl.styleSpec == null) {
//            return tbl.styleSpec = StyleCodec.decode(tbl, tbl.styleSpecString, meta);
//        } else {
//            return tbl.styleSpec;
//        }
    	return null;
    }

    @Deprecated
    public static TablePath walk(TablePath path, Field field, MetaData meta)
            throws SQLException {
        Table next = meta.getTableById(field.refdTableId);
        if (next == null)
            return null;

        // since the expected path length are very small, don't use
        // System.arrayCopy, as it's slower until ~8 elements or so

        int num = path.tableIds.length;
        int[] newTables = new int[num + 1];
        int[] newFields = new int[num];

        for (int a = 0; a < num; a++)
            newTables[a] = path.tableIds[a];
        newTables[num] = next.id;

        num--;
        for (int a = 0; a < num; a++)
            newFields[a] = path.walkedFieldIds[a];
        newFields[num] = field.id;

        return new TablePath(newTables, newFields);
    }

    @Deprecated
    public static TablePath combine(TablePath first, TablePath follow) {
        if (first.lastTableId() != follow.tableIds[0])
            throw new IllegalArgumentException();

        int firstFieldsLength = first.walkedFieldIds.length;

        int[] walkedFields = new int[firstFieldsLength
                + follow.walkedFieldIds.length];
        int[] tables = new int[walkedFields.length + 1];
        for (int a = 0; a < walkedFields.length; a++) {
            if (a < firstFieldsLength) {
                walkedFields[a] = first.walkedFieldIds[a];
                tables[a] = first.tableIds[a];
            } else {
                walkedFields[a] = follow.walkedFieldIds[a - firstFieldsLength];
                tables[a] = follow.tableIds[a - firstFieldsLength];
            }
        }
        tables[tables.length - 1] = follow.lastTableId();

        return new TablePath(tables, walkedFields);
    }

    @Deprecated
    public static FieldPath combine(TablePath toPrepend, FieldPath fieldPath) {
        if (fieldPath instanceof UserFieldPath) {
            return combine(toPrepend, (UserFieldPath) fieldPath);
        } else {
            return combine(toPrepend, (MetaFieldPath) fieldPath);
        }
    }
    @Deprecated
    public static UserFieldPath combine(TablePath toPrepend,
            UserFieldPath fieldPath) {
        return new UserFieldPath(combine(toPrepend, fieldPath.table),
                fieldPath.fieldId);
    }
    @Deprecated
    public static MetaFieldPath combine(TablePath toPrepend,
            MetaFieldPath fieldPath) {
        return new MetaFieldPath(combine(toPrepend, fieldPath.table),
                fieldPath.whichOne);
    }
    @Deprecated
    public static StyleSpec getStyleSpecForTable(int tableId, MetaData meta)
            throws ParseStyleException, SQLException {
        return getStyleSpec(meta.getTableById(tableId), meta);
    }
    @Deprecated
    public static StringSpec getTextRepSpecForTable(int tableId, MetaData meta)
            throws ParseStyleException, SQLException {
        return getTextRepSpec(meta.getTableById(tableId), meta);
    }
    @Deprecated
    public static StringSpec defaultTextRep(Table t) {
        TablePath tp = new TablePath(t.id);

        for (Field f : t.fields)
            if (f.type == Field.FieldType.PLAINTEXT && f.hasFlag(FieldFlags.MANDATORY))
                return new StringField(new UserFieldPath(tp, f.id));

        for (Field f : t.fields)
            if (f.type == Field.FieldType.PLAINTEXT)
                return new StringConcat(new StringField(new UserFieldPath(tp,
                        f.id)), new StringConcat(new ConstString(" (ID "),
                        new StringConcat(new StringFromNumber(new NumberField(
                                new MetaFieldPath(tp, MetaFieldPath.MF_ID))),
                                new ConstString(")"))));

        return new StringConcat(new ConstString("ID "), new StringFromNumber(
                new NumberField(new MetaFieldPath(tp, MetaFieldPath.MF_ID))));
    }
}
