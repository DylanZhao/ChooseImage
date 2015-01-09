
package com.skymobi.appstore.funnypush;

/**
 * @ClassName: AppInfo
 * @Description: TODO
 * @author dylan.zhao
 * @date 2013-10-30 下午05:44:51
 */
public class AppInfo {

    public String name;
    public String iconKey;

    public String typeName;// 类别
    public String sizeString;
    public int downCount;// 下载量

    public int iconId;// 临时用，内置图标资源id

    public AppInfo(String name) {
        this.name = name;
    }
}
