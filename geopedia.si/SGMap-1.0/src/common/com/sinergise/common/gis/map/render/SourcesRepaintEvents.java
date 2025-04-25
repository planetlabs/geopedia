/*
 *
 */
package com.sinergise.common.gis.map.render;


public interface SourcesRepaintEvents {
    void addRepaintListener(RepaintListener l);
    void removeRepaintListener(RepaintListener l);
}
