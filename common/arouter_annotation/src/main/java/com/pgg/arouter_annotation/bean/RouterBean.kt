package com.pgg.arouter_annotation.bean

import javax.lang.model.element.Element

/**
 * 最终路由 要 传递 对象
 *
 * 路由路径Path的最终实体封装类
 * 例如：app分组中的MainActivity对象，这个对象有更多的属性
 */
class RouterBean private constructor() {

    enum class TypeEnum {
        ACTIVITY, CALL
    }

    private constructor(typeEnum: TypeEnum, clazz: Class<*>, path: String, group: String) : this() {
        this.clazz = clazz
        this.typeEnum = typeEnum
        this.path = path
        this.group = group
    }

    // 构建者模式相关
    private constructor(builder: Builder) : this() {
        typeEnum = builder.type?:TypeEnum.ACTIVITY
        element = builder.element
        clazz = builder.clazz
        path = builder.path?:""
        group = builder.group?:""
    }

    var typeEnum: TypeEnum = TypeEnum.ACTIVITY
    var element: Element? = null
    var clazz: Class<*>? = null
    var path: String = ""
    var group: String = ""

    companion object {
        @JvmStatic
        fun create(typeEnum: TypeEnum, clazz: Class<*>, path: String, group: String): RouterBean {
            return RouterBean(typeEnum, clazz, path, group)
        }
    }

    /**
     * 构建者模式
     */
    class Builder {
        // 枚举类型：Activity
        internal var type: TypeEnum? = null

        // 类节点
        internal var element: Element? = null

        // 注解使用的类对象
        internal var clazz: Class<*>? = null

        // 路由地址
        internal var path: String? = null

        // 路由组
        internal var group: String? = null
        fun addType(type: TypeEnum): Builder {
            this.type = type
            return this
        }

        fun addElement(element: Element): Builder {
            this.element = element
            return this
        }

        fun addClazz(clazz: Class<*>): Builder {
            this.clazz = clazz
            return this
        }

        fun addPath(path: String): Builder {
            this.path = path
            return this
        }

        fun addGroup(group: String): Builder {
            this.group = group
            return this
        }

        // 最后的build或者create，往往是做参数的校验或者初始化赋值工作
        fun build(): RouterBean {
            require(!(path == null || path!!.isEmpty())) { "path必填项为空，如：/app/MainActivity" }
            return RouterBean(this)
        }
    }

    override fun toString(): String {
        return "RouterBean{" +
                "path='" + path + '\'' +
                ", group='" + group + '\'' +
                '}'
    }
}