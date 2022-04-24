package com.pgg.arouter_annotation

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class ARouter(
    val path: String,
    val group: String = ""
)