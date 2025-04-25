/*
 * (c) Infoterra Limited 2004.  The contents of this file are under the
 * copyright of Infoterra.  All rights reserved.  This file must not be
 * copied, reproduced or distributed in wholly or in part or used for
 * purposes other than for that for which it has been supplied without
 * the prior written permission of Infoterra.
 */

package com.sinergise.java.swing.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.util.HashMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;


/**
 * IconHelper acts as an interface to the all icons currently in classpath.
 * IconHelper is a singleton class that maintains internal cache to all
 * accessed icons. This class may be used to minimize the memory consumption
 * where application uses static icons. If icons are changed independently,
 * this class should not be used.
 *
 * @author <a href="mailto:ales.pucelj@cosylab.com">Ales Pucelj</a>
 * @version $id$
 */
public final class IconHelper
{
    /** cache for icons. */
    private static final HashMap<String, Icon> cache = new HashMap<String, Icon>();

    /**
     * singleton instance of this class.
     */

    // notused private static IconHelper instance = null;

    /**
     * Protected constructor for creation of singleton class.
     */
    private IconHelper()
    {
        super();
    }

    /**
     * Returns instance of the icon by loading it using the <code>
     * ClassLoader</code> instance provided. Icons are cached. If an icon with
     * the same name is requested several times, <code>createIcon</code> will
     * return the same instance each time. If icons will be manipulated
     * individually, do not use this method to load icons. If icon is not
     * found in the classpath, return value will be null.
     *
     * @param name String
     * @param cl ClassLoader Instance of class loader to use
     *
     * @return Icon Instance of Icon or null if icon could not be found.
     */
    public static Icon createIcon(String name, ClassLoader cl)
    {
        Icon icon = getCachedIcon(name);

        if (icon == null)
        {
            icon = loadResource(name, cl);

            if (icon != null)
            {
                setCachedIcon(name, icon);
            }
        }

        return icon;
    }


    /**
     * Returns instance of the icon. Icons are cached. If an icon with the same
     * name is requested several times, <code>createIcon</code> will return
     * the same instance each time. If icons will be manipulated individually,
     * do not use this method to load icons. If icon is not found in the
     * classpath, return value will be null.
     *
     * @param name String
     *
     * @return Icon Instance of Icon or null if icon could not be found.
     */
    public static Icon createIcon(String name)
    {
        return createIcon(name, IconHelper.class.getClassLoader());
    }


    /**
     * Creates an <code>Image</code> object for the given name
     *
     * @param name String
     *
     * @return Image
     */
    public static Image createImage(String name)
    {
        return createImage(name, IconHelper.class.getClassLoader());
    }


    /**
     * Creates an <code>Image</code> object for the given name and ClassLoader
     *
     * @param name String
     * @param cl ClassLoader
     *
     * @return Image
     */
    public static Image createImage(String name, ClassLoader cl)
    {
        ImageIcon icon = ((ImageIcon) createIcon(name, cl));

        if (icon == null)
        {
            return null;
        }

        return icon.getImage();
    }
    
    public static Icon createColorIcon(final Color fillC, final Color outlineC,final int w,final int h) {
    	return new Icon() {
				
				public void paintIcon(Component c, Graphics g, int x, int y) {
					if (fillC!=null) {
						g.setColor(fillC);
						g.fillRect(x, y, w, h);
					}
					if (outlineC!=null) {
						g.setColor(outlineC);
						g.drawRect(x, y, w-1, h-1);
					}
				}
				
				public int getIconWidth() {
					return w;
				}
				
				public int getIconHeight() {
					return h;
				}
			};
    }


    /**
     * Puts icon in the cache. If an icon with the specified name has already
     * been stored, current icon will be overwritten.
     *
     * @param name
     * @param icon
     */
    private static synchronized void setCachedIcon(String name, Icon icon)
    {
        cache.put(name, icon);
    }


    /**
     * Returns icon from the cache. If icon cannot be found in cache, return
     * value will be <code>null</code>.
     *
     * @param name String
     *
     * @return Icon instance of an icon or null if not found
     */
    private static Icon getCachedIcon(String name)
    {
        if (cache.containsKey(name))
        {
            return cache.get(name);
        }

        return null;
    }


    /**
     * Attempts to load the icon from the resources in classpath. If the icon
     * cannot be found or other error occurs, the return value is null.
     *
     * @param resource String
     * @param cl ClassLoader
     *
     * @return Icon icon instance or null
     */
    private static final Icon loadResource(String resource, ClassLoader cl)
    {
        try
        {
            return new ImageIcon(cl.getResource(resource));
        }
        catch (Exception e)
        {
            return null;
        }
    }
}
