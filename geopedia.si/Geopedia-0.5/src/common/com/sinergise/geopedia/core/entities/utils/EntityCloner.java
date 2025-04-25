/*
 * 
 */
package com.sinergise.geopedia.core.entities.utils;


@Deprecated
public class EntityCloner implements EntityConsts
{
	/*
    private EntityCloner()
    {}

    private static Field doCopy(Field src, Field tgt, int copyType)
    {
        if (src == null)
            throw new IllegalArgumentException("Null source field");
        if (tgt == null)
            tgt = new Field();
        switch (copyType)
        {
            case COPY_SHALLOW:
            case COPY_META:
                tgt.descRawHtml = src.descRawHtml;
                tgt.descDisplayableHtml = src.descDisplayableHtml;
                tgt.defaultValueString = src.defaultValueString;
            case COPY_BASIC:
                tgt.id = src.id;
                tgt.setName(src.getName());
                tgt.lastTableMeta = src.lastTableMeta;
                tgt.type = src.type;
                tgt.setFlags(src.getFlags());
                tgt.order = src.order;
                tgt.tableId = src.tableId;
                tgt.refdTableId = src.refdTableId;
                tgt.sysId = src.sysId;
                tgt.properties = cloneState(src.properties);
                break;
            default:
                throw new IllegalArgumentException("Illegal copyType");
        }
        return tgt;
    }

    private static State cloneState(State sIn) {
        if (sIn==null || sIn.isEmpty()) return null;
        State ret=new State();
        ret.setFrom(sIn, true);
        return ret;
    }

    private static boolean has(Field fld, int copyType)
    {
        switch (copyType)
        {
            case COPY_BASIC:
                if (fld.getName() != null)//TODO:drejmar:fld.name
                    return true;
            case COPY_META:
                if (fld.descRawHtml != null || fld.descDisplayableHtml != null)
                    return true;
            case COPY_SHALLOW:
            default:
                break;
        }
        return false;
    }

    private static boolean has(Field[] flds, int copyType)
    {
        for (int i = 0; i < flds.length; i++)
        {
            if (!has(flds[i], copyType))
            {
                return false;
            }
        }
        return true;
    }

    private static void keepOnly(Table t, int copyType)
    {
        switch (copyType)
        {
        	case 0:
        		t.setName(null);
        		t.geomType = GeomType.NONE; 
        		t.textRepExpr = null;
        		t.textRepSpec = null;
        		t.user_perms = 0;
        		t.public_perms = 0;
            case COPY_BASIC:
                t.descRawHtml = null;
                t.descDisplayableHtml = null;
                t.styleSpecString = null;
                t.styleSpec = null;
            case COPY_META:
                t.fields = null;
            case COPY_SHALLOW:
                break;
            default:
                throw new IllegalArgumentException("Illegal copyType");
        }
    }

    private static Table doCopy(Table src, Table tgt, int copyType)
    {
        if (src == null)
            throw new IllegalArgumentException("Null source table");
        if (tgt == null)
            tgt = new Table();
        switch (copyType)
        {
            case COPY_SHALLOW:
                tgt.fields = (src.fields == null ? new Field[0] : doCopy(src.fields, tgt.fields, COPY_META));
            case COPY_META:
                tgt.descRawHtml = src.descRawHtml;
                tgt.descDisplayableHtml = src.descDisplayableHtml;
                if (src.styleSpec == null)
                {
                    tgt.styleSpecString = src.styleSpecString;
                    tgt.styleSpec = null;
                }
                else
                {
                    tgt.styleSpec = (StyleSpec)src.styleSpec.clone();
                    tgt.styleSpecString = src.styleSpecString;
                }
            case COPY_BASIC:
                tgt.id = src.id;
                tgt.setName(src.getName());
                tgt.geomType = src.geomType;
                tgt.lastDataWrite = src.lastDataWrite;
                tgt.setMetaChange(src.lastMetaChange);
                tgt.public_perms = src.public_perms;
                tgt.textRepExpr = src.textRepExpr;
                tgt.user_perms = src.user_perms;
                tgt.setDeleted(src.isDeleted());
                tgt.properties = cloneState(src.properties);
                tgt.envelope = new Envelope(src.envelope);
                break;
            default:
                throw new IllegalArgumentException("Illegal copyType");
        }
        return tgt;
    }

    private static boolean has(Table tbl, int copyType)
    {
        switch (copyType)
        {
            case COPY_BASIC:
                if (tbl.getName() != null)
                    return true;
            case COPY_META:
                if (tbl.styleSpecString != null || tbl.styleSpec != null)
                    return true;
            case COPY_SHALLOW:
                if (tbl.fields != null)
                    return true;
            default:
                break;
        }
        return false;
    }

    private static boolean has(Table[] tbls, int copyType)
    {
        for (int i = 0; i < tbls.length; i++)
        {
            if (!has(tbls[i], copyType))
            {
                return false;
            }
        }
        return true;
    }

//    public static Feature doCopy(Feature src, Feature tgt, int copyType)
//    {
//        if (src == null)
//            throw new IllegalArgumentException("Null source feature");
//        if (tgt == null)
//            tgt = new Feature();
//        switch (copyType)
//        {
//            case COPY_SHALLOW:
//                // TODO: copy values and fields, dont need this for now
//            	throw new UnsupportedOperationException("Shallow feature copy not implemented");
//            case COPY_BASIC:
//                tgt.featureId = src.featureId;
//                tgt.lastUserId = src.lastUserId;
//                tgt.tableDataTs = src.tableDataTs;
//                tgt.tableId = src.tableId;
//                tgt.timestamp = src.timestamp;
//                break;
//            default:
//                throw new IllegalArgumentException("Illegal copyType");
//        }
//        return tgt;
//    }

    private static Field[] doCopy(Field[] src, Field[] tgt, int copyType)
    {
        if (src == null)
            throw new IllegalArgumentException("Null source fields");
        HashMap<Integer, Field> tgtMap = new HashMap<Integer, Field>();
        if (tgt != null)
        {
            for (int i = 0; i < tgt.length; i++)
            {
                tgtMap.put(new Integer(tgt[i].id), tgt[i]);
            }
        }

        Field[] ret = tgt;
        if (tgt == null || src.length != tgt.length)
        {
            ret = new Field[src.length];
        }

        for (int i = 0; i < src.length; i++)
        {
            Field tgtFld = tgtMap.get(new Integer(src[i].id));
            if (tgtFld == null)
                tgtFld = new Field();
            ret[i] = doCopy(src[i], tgtFld, copyType);
        }
        return ret;
    }

    private static void keepOnly(Theme t, int copyType)
    {
        switch (copyType)
        {
        	case 0:
        		t.setName(null);
            case COPY_BASIC:
                t.descRawHtml = null;
                t.descDisplayableHtml = null;
            case COPY_META:
                t.tables = null;
            case COPY_SHALLOW:
                break;
            default:
                throw new IllegalArgumentException("Illegal copyType");
        }
    }

    private static Theme doCopy(Theme src, Theme tgt, int copyType)
    {
        if (src == null)
            throw new IllegalArgumentException("Null source field");
        if (tgt == null)
            tgt = new Theme();
        switch (copyType)
        {
            case COPY_SHALLOW:
                tgt.tables = doCopy(src.tables, tgt.tables, COPY_SHALLOW);
            case COPY_META:
                tgt.descRawHtml = src.descRawHtml;
                tgt.descDisplayableHtml = src.descDisplayableHtml;
            case COPY_BASIC:
                tgt.id = src.id;
                tgt.setName(src.getName());
                tgt.baseLayers = src.baseLayers;
                tgt.public_perms = src.public_perms;
                tgt.user_perms = src.user_perms;
                tgt.lastMetaChange=src.lastMetaChange;
                tgt.properties=cloneState(src.properties);
                break;
            default:
                throw new IllegalArgumentException("Illegal copyType");
        }
        return tgt;
    }

    private static boolean has(Theme th, int copyType)
    {
        switch (copyType)
        {
            case COPY_BASIC:
                if (th.getName() != null)
                    return true;
            case COPY_META:
                if (th.descRawHtml != null)
                    return true;
            case COPY_SHALLOW:
                if (th.tables != null)
                    return true;
                break;
            default:
                break;
        }
        return false;
    }

    private static boolean has(Theme[] thms, int copyType)
    {
        for (int i = 0; i < thms.length; i++)
        {
            if (!has(thms[i], copyType))
            {
                return false;
            }
        }
        return true;
    }

    private static ThemeTableLink doCopy(ThemeTableLink src, ThemeTableLink tgt, int copyType)
    {
        if (src == null)
            throw new IllegalArgumentException("Null source field");
        if (tgt == null)
            tgt = new ThemeTableLink();
        switch (copyType)
        {
            case COPY_SHALLOW:
                tgt.table = doCopy(src.table, new Table(), COPY_SHALLOW);
            case COPY_META:
                if (src.styleSpec == null)
                {
                    tgt.styleString = src.styleString;
                    tgt.styleSpec = null;
                }
                else
                {
                    tgt.styleSpec = (StyleSpec)src.styleSpec.clone();
                    tgt.styleString = null;
                }
            case COPY_BASIC:
                tgt.id = src.id;
                tgt.on = src.on;
                tgt.group = src.group;
                tgt.setName(src.getName());
                tgt.properties = cloneState(src.properties);
                tgt.themeId = src.themeId;
                tgt.tableId = src.tableId;
                tgt.orderInTheme = src.orderInTheme;
                break;
            default:
                throw new IllegalArgumentException("Illegal copyType");
        }
        return tgt;
    }

    private static boolean has(ThemeTableLink link, int copyType)
    {
        switch (copyType)
        {
            case COPY_BASIC:
                if (link.themeId >= 0)
                    return true;
            case COPY_META:
                if (link.styleString != null || link.styleSpec != null)
                    return true;
            case COPY_SHALLOW:
                if (link.table != null)
                    return true;
            default:
                break;
        }
        return false;
    }

    private static boolean has(ThemeTableLink[] tbls, int copyType)
    {
        for (int i = 0; i < tbls.length; i++)
        {
            if (!has(tbls[i], copyType))
            {
                return false;
            }
        }
        return true;
    }

   


    private static Table[] doCopy(Table[] src, Table[] tgt, int copyType)
    {
        HashMap<Integer, Table> tgtMap = new HashMap<Integer, Table>();
        if (tgt != null)
        {
            for (int i = 0; i < tgt.length; i++)
            {
                tgtMap.put(new Integer(tgt[i].id), tgt[i]);
            }
        }

        Table[] ret = tgt;
        if (tgt == null || src.length != tgt.length)
        {
            ret = new Table[src.length];
        }

        for (int i = 0; i < src.length; i++)
        {
            Table tgtEl = tgtMap.get(new Integer(src[i].id));
            if (tgtEl == null)
                tgtEl = new Table();
            ret[i] = doCopy(src[i], tgtEl, copyType);
        }
        return ret;
    }

    private static Theme[] doCopy(Theme[] src, Theme[] tgt, int copyType)
    {
        HashMap<Integer, Theme> tgtMap = new HashMap<Integer, Theme>();
        if (tgt != null)
        {
            for (int i = 0; i < tgt.length; i++)
            {
                tgtMap.put(new Integer(tgt[i].id), tgt[i]);
            }
        }

        Theme[] ret = tgt;
        if (tgt == null || src.length != tgt.length)
        {
            ret = new Theme[src.length];
        }

        for (int i = 0; i < src.length; i++)
        {
            Theme tgtFld = tgtMap.get(new Integer(src[i].id));
            if (tgtFld == null)
                tgtFld = new Theme();
            ret[i] = doCopy(src[i], tgtFld, copyType);
        }
        return ret;
    }

    private static ThemeTableLink[] doCopy(ThemeTableLink[] src, ThemeTableLink[] tgt, int copyType)
    {
        if (src==null) return null;
        HashMap<Integer, ThemeTableLink> tgtMap = new HashMap<Integer, ThemeTableLink>();
        if (tgt != null)
        {
            for (int i = 0; i < tgt.length; i++)
            {
                tgtMap.put(new Integer(tgt[i].id), tgt[i]);
            }
        }

        ThemeTableLink[] ret = tgt;
        if (tgt == null || src.length != tgt.length)
        {
            ret = new ThemeTableLink[src.length];
        }

        for (int i = 0; i < src.length; i++)
        {
            ThemeTableLink tgtEl = tgtMap.get(new Integer(src[i].id));
            ret[i] = doCopy(src[i], tgtEl, copyType);
        }
        return ret;
    }*/
}
