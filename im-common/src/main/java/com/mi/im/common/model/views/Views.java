package com.mi.im.common.model.views;

/**
 * @className: Views
 * @Description: TODO
 * @author: zhuyt
 * @date: 25/11/10 9:01
 */
public class Views {
    // 公共基础视图
    public interface Public {}

    // 摘要视图（继承Public）
    public interface Summary extends Public {}

    // 详情视图（继承Summary）
    public interface Detail extends Summary {}

    // 管理员视图（继承Detail）
    public interface Admin extends Detail {}
}

